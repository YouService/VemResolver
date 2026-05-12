---
name: service-matching-rules
description: Ativar quando implementar busca de profissionais, listagem de serviços para profissionais, geolocalização, filtros de categoria, ou qualquer lógica de matching entre clientes e profissionais no VemResolver.
---

# Regras de Matching — VemResolver

## Lógica de matching

O matching conecta um Servico (necessidade do cliente) a Profissionais
elegíveis com base em dois critérios combinados: categoria e proximidade.

### Critérios de elegibilidade

Um Profissional é elegível para um Servico quando:
1. Possui a Categoria do Servico na sua lista de categorias
2. Está na mesma cidade OU dentro do raioAtendimentoKm do Servico

### Query de matching — JPA

```java
@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    // Busca por cidade exata + categoria
    @Query("""
        SELECT p FROM Profissional p
        JOIN p.categorias c
        WHERE c.id = :categoriaId
        AND p.cidade = :cidade
        AND p.estado = :estado
        AND p.status = 'ATIVO'
        ORDER BY p.avaliacaoMedia DESC
        """)
    List<Profissional> findElegiveisNaCidade(
        @Param("categoriaId") Long categoriaId,
        @Param("cidade") String cidade,
        @Param("estado") String estado
    );
}
```

### Service de matching

```java
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;

    public List<Profissional> encontrarProfissionaisElegiveis(Long servicoId) {
        Servico servico = servicoRepository.findById(servicoId)
            .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado"));

        if (servico.getStatus() != StatusServico.ABERTO) {
            throw new BusinessException("Serviço não está aberto para propostas");
        }

        // Busca por cidade primeiro (mais preciso)
        List<Profissional> porCidade = profissionalRepository.findElegiveisNaCidade(
            servico.getCategoria().getId(),
            servico.getCidade(),
            servico.getEstado()
        );

        // Se poucos resultados, expande a busca
        if (porCidade.size() < 5) {
            // Futuramente: busca por raio via CEP/coordenadas
            // Por ora retorna o que encontrou
        }

        return porCidade;
    }

    public boolean profissionalPodeEnviarProposta(Long profissionalId, Long servicoId) {
        // Verifica se já enviou proposta
        boolean jaEnviou = propostaRepository
            .existsByProfissionalIdAndServicoId(profissionalId, servicoId);
        if (jaEnviou) return false;

        // Verifica se é elegível
        Servico servico = servicoRepository.findById(servicoId).orElseThrow();
        Profissional profissional = profissionalRepository.findById(profissionalId).orElseThrow();

        return profissional.getCategorias()
            .stream()
            .anyMatch(c -> c.getId().equals(servico.getCategoria().getId()))
            && servico.getStatus() == StatusServico.ABERTO;
    }
}
```

## Fluxo de aceite de proposta

```java
@Service
@RequiredArgsConstructor
@Transactional
public class PropostaService {

    private final PropostaRepository propostaRepository;
    private final ServicoRepository servicoRepository;
    private final ContratoRepository contratoRepository;

    public Contrato aceitarProposta(Long propostaId, Long clienteId) {
        Proposta proposta = propostaRepository.findById(propostaId)
            .orElseThrow(() -> new EntityNotFoundException("Proposta não encontrada"));

        // Valida que o cliente é dono do serviço
        if (!proposta.getServico().getCliente().getId().equals(clienteId)) {
            throw new AccessDeniedException("Você não tem permissão para aceitar esta proposta");
        }

        // Valida status
        if (proposta.getStatus() != StatusProposta.PENDENTE) {
            throw new BusinessException("Proposta não está pendente");
        }

        // Aceita esta proposta
        proposta.setStatus(StatusProposta.ACEITA);
        propostaRepository.save(proposta);

        // Recusa TODAS as outras propostas do mesmo serviço
        propostaRepository.recusarDemaisPropostas(
            proposta.getServico().getId(),
            propostaId
        );

        // Muda status do serviço
        Servico servico = proposta.getServico();
        servico.setStatus(StatusServico.EM_ANDAMENTO);
        servicoRepository.save(servico);

        // Cria o contrato
        Contrato contrato = new Contrato();
        contrato.setProposta(proposta);
        contrato.setValorFinal(proposta.getValorProposto());
        contrato.setPrazoEntrega(LocalDate.now().plusDays(proposta.getPrazoEmDias()));
        contrato.setStatus(StatusContrato.ATIVO);
        contrato.setCriadoEm(LocalDateTime.now());

        return contratoRepository.save(contrato);
    }
}
```

## Cálculo de avaliação média

```java
@Service
public class AvaliacaoService {

    @Transactional
    public Avaliacao avaliar(Long contratoId, TipoAvaliador tipo, Integer nota, String comentario) {
        // ... validações ...

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setContrato(contrato);
        avaliacao.setTipoAvaliador(tipo);
        avaliacao.setNota(nota);
        avaliacao.setComentario(comentario);
        avaliacao.setCriadoEm(LocalDateTime.now());
        avaliacaoRepository.save(avaliacao);

        // Recalcula média do profissional após avaliação do cliente
        if (tipo == TipoAvaliador.CLIENTE) {
            Profissional profissional = contrato.getProposta().getProfissional();
            recalcularMedia(profissional);
        }

        return avaliacao;
    }

    private void recalcularMedia(Profissional profissional) {
        Double media = avaliacaoRepository.calcularMediaPorProfissional(profissional.getId());
        Long total = avaliacaoRepository.countByProfissional(profissional.getId());
        profissional.setAvaliacaoMedia(media != null ? media : 0.0);
        profissional.setTotalAvaliacoes(total.intValue());
        profissionalRepository.save(profissional);
    }
}
```

## Índices recomendados para performance

```sql
-- V9__add_indexes.sql
CREATE INDEX idx_servicos_status ON servicos(status);
CREATE INDEX idx_servicos_categoria_id ON servicos(categoria_id);
CREATE INDEX idx_servicos_cidade_estado ON servicos(cidade, estado);
CREATE INDEX idx_propostas_servico_id ON propostas(servico_id);
CREATE INDEX idx_propostas_profissional_id ON propostas(profissional_id);
CREATE INDEX idx_propostas_status ON propostas(status);
CREATE INDEX idx_profissional_categorias ON profissional_categorias(profissional_id, categoria_id);
CREATE INDEX idx_avaliacoes_contrato_id ON avaliacoes(contrato_id);
```
