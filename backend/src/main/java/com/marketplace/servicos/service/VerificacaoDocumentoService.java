package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.VerificacaoDocumentoDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.Prestador;
import com.marketplace.servicos.model.StatusVerificacao;
import com.marketplace.servicos.model.VerificacaoDocumento;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.VerificacaoDocumentoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class VerificacaoDocumentoService {

    private static final long MAX_BYTES = 10L * 1024 * 1024; // 10 MB
    private static final Set<String> TIPOS_ACEITOS = Set.of("image/jpeg", "image/png", "image/webp");

    private final VerificacaoDocumentoRepository verificacaoRepository;
    private final PrestadorRepository prestadorRepository;

    @Value("${upload.dir:/uploads}")
    private String uploadDir;

    public VerificacaoDocumentoService(VerificacaoDocumentoRepository verificacaoRepository,
                                       PrestadorRepository prestadorRepository) {
        this.verificacaoRepository = verificacaoRepository;
        this.prestadorRepository = prestadorRepository;
    }

    @Transactional
    public VerificacaoDocumentoDTO enviarDocumentos(MultipartFile documento, MultipartFile selfie, Long usuarioId) {
        validarArquivo(documento, "Documento");
        validarArquivo(selfie, "Selfie");

        Prestador prestador = prestadorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de prestador não encontrado"));

        VerificacaoDocumento verificacao = verificacaoRepository
                .findTopByPrestadorIdOrderByDataEnvioDesc(prestador.getId())
                .orElse(new VerificacaoDocumento());

        if (StatusVerificacao.APROVADO.equals(verificacao.getStatus())) {
            throw new BusinessException("Sua identidade já foi verificada e aprovada.");
        }

        Path diretorio = Paths.get(uploadDir, "verificacoes", prestador.getId().toString());
        try {
            Files.createDirectories(diretorio);
        } catch (IOException e) {
            throw new BusinessException("Erro ao criar diretório de armazenamento.");
        }

        String extDoc = extensao(documento.getContentType());
        String extSelfie = extensao(selfie.getContentType());
        String nomeDoc = UUID.randomUUID() + extDoc;
        String nomeSelfie = UUID.randomUUID() + extSelfie;

        salvarArquivo(documento, diretorio.resolve(nomeDoc));
        salvarArquivo(selfie, diretorio.resolve(nomeSelfie));

        verificacao.setPrestador(prestador);
        verificacao.setCaminhoDoc(diretorio.resolve(nomeDoc).toString());
        verificacao.setCaminhoSelfie(diretorio.resolve(nomeSelfie).toString());
        verificacao.setStatus(StatusVerificacao.PENDENTE);
        verificacao.setDataEnvio(LocalDateTime.now());
        verificacao.setDataRevisao(null);
        verificacao.setObservacaoAdmin(null);

        verificacao = verificacaoRepository.save(verificacao);
        return toDTO(verificacao);
    }

    @Transactional(readOnly = true)
    public VerificacaoDocumentoDTO buscarPorPrestador(Long usuarioId) {
        Prestador prestador = prestadorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de prestador não encontrado"));

        return verificacaoRepository
                .findTopByPrestadorIdOrderByDataEnvioDesc(prestador.getId())
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<VerificacaoDocumentoDTO> listar(StatusVerificacao status, Pageable pageable) {
        if (status != null) {
            return verificacaoRepository.findByStatusOrderByDataEnvioAsc(status, pageable).map(this::toDTO);
        }
        return verificacaoRepository.findAllByOrderByDataEnvioDesc(pageable).map(this::toDTO);
    }

    @Transactional
    public VerificacaoDocumentoDTO aprovar(Long id, String observacao) {
        VerificacaoDocumento verificacao = buscarEntidade(id);

        if (!StatusVerificacao.PENDENTE.equals(verificacao.getStatus())) {
            throw new BusinessException("Apenas verificações PENDENTE podem ser aprovadas.");
        }

        verificacao.setStatus(StatusVerificacao.APROVADO);
        verificacao.setObservacaoAdmin(observacao);
        verificacao.setDataRevisao(LocalDateTime.now());
        verificacaoRepository.save(verificacao);

        Prestador prestador = verificacao.getPrestador();
        prestador.setIdVerificado(true);
        prestadorRepository.save(prestador);

        return toDTO(verificacao);
    }

    @Transactional
    public VerificacaoDocumentoDTO recusar(Long id, String observacao) {
        VerificacaoDocumento verificacao = buscarEntidade(id);

        if (!StatusVerificacao.PENDENTE.equals(verificacao.getStatus())) {
            throw new BusinessException("Apenas verificações PENDENTE podem ser recusadas.");
        }

        verificacao.setStatus(StatusVerificacao.RECUSADO);
        verificacao.setObservacaoAdmin(observacao);
        verificacao.setDataRevisao(LocalDateTime.now());
        verificacaoRepository.save(verificacao);

        return toDTO(verificacao);
    }

    public ResponseEntity<InputStreamResource> servirArquivo(Long id, boolean documento) {
        VerificacaoDocumento verificacao = buscarEntidade(id);
        String caminho = documento ? verificacao.getCaminhoDoc() : verificacao.getCaminhoSelfie();
        Path path = Paths.get(caminho);

        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Arquivo não encontrado no servidor.");
        }

        try {
            String contentType = Files.probeContentType(path);
            if (contentType == null) contentType = "application/octet-stream";
            InputStream is = Files.newInputStream(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new InputStreamResource(is));
        } catch (IOException e) {
            throw new BusinessException("Erro ao ler arquivo: " + e.getMessage());
        }
    }

    public long contarPendentes() {
        return verificacaoRepository.countByStatus(StatusVerificacao.PENDENTE);
    }

    private VerificacaoDocumento buscarEntidade(Long id) {
        return verificacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Verificação não encontrada"));
    }

    private void validarArquivo(MultipartFile file, String nome) {
        if (file == null || file.isEmpty())
            throw new BusinessException(nome + " não pode ser vazio.");
        if (!TIPOS_ACEITOS.contains(file.getContentType()))
            throw new BusinessException(nome + ": tipo não suportado. Use JPEG, PNG ou WEBP.");
        if (file.getSize() > MAX_BYTES)
            throw new BusinessException(nome + ": tamanho máximo é 10 MB.");
    }

    private void salvarArquivo(MultipartFile file, Path destino) {
        try {
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    private String extensao(String contentType) {
        if (contentType == null) return ".jpg";
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private VerificacaoDocumentoDTO toDTO(VerificacaoDocumento v) {
        VerificacaoDocumentoDTO dto = new VerificacaoDocumentoDTO();
        dto.setId(v.getId());
        dto.setPrestadorId(v.getPrestador().getId());
        dto.setPrestadorNome(v.getPrestador().getUsuario().getNome());
        dto.setStatus(v.getStatus());
        dto.setDataEnvio(v.getDataEnvio() != null ? v.getDataEnvio().toLocalDate().toString() : null);
        dto.setDataRevisao(v.getDataRevisao() != null ? v.getDataRevisao().toLocalDate().toString() : null);
        dto.setObservacaoAdmin(v.getObservacaoAdmin());
        return dto;
    }
}
