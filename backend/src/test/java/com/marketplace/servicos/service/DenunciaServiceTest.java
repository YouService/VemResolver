package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.AtualizarDenunciaDTO;
import com.marketplace.servicos.dto.CriarDenunciaDTO;
import com.marketplace.servicos.dto.DenunciaDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.DenunciaRepository;
import com.marketplace.servicos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DenunciaServiceTest {

    @Mock private DenunciaRepository denunciaRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private DenunciaService denunciaService;

    private User denunciante;
    private User denunciado;

    @BeforeEach
    void setUp() {
        denunciante = new User();
        denunciante.setId(1L);
        denunciante.setNome("João Cliente");
        denunciante.setAtivo(true);

        denunciado = new User();
        denunciado.setId(2L);
        denunciado.setNome("Maria Prestador");
        denunciado.setAtivo(true);
    }

    // ── DN-01: criação bem-sucedida ───────────────────────────────────────────

    @Test
    @DisplayName("DN-01: denúncia válida é criada com status PENDENTE")
    void criar_DenunciaValida_RetornaPendente() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(denunciante));
        when(userRepository.findById(2L)).thenReturn(Optional.of(denunciado));
        when(denunciaRepository.existsByDenuncianteIdAndDenunciadoIdAndTipo(1L, 2L, TipoDenuncia.PERFIL))
                .thenReturn(false);

        Denuncia salva = new Denuncia();
        salva.setId(10L);
        salva.setDenunciante(denunciante);
        salva.setDenunciado(denunciado);
        salva.setTipo(TipoDenuncia.PERFIL);
        salva.setDescricao("Perfil falso com informações enganosas");
        salva.setStatus(StatusDenuncia.PENDENTE);
        when(denunciaRepository.save(any())).thenReturn(salva);

        CriarDenunciaDTO dto = new CriarDenunciaDTO();
        dto.setDenunciadoId(2L);
        dto.setTipo(TipoDenuncia.PERFIL);
        dto.setDescricao("Perfil falso com informações enganosas");

        DenunciaDTO resultado = denunciaService.criar(dto, 1L);

        assertThat(resultado.getStatus()).isEqualTo(StatusDenuncia.PENDENTE);
        assertThat(resultado.getDenuncianteNome()).isEqualTo("João Cliente");
        assertThat(resultado.getDenunciadoNome()).isEqualTo("Maria Prestador");
        verify(denunciaRepository).save(any());
    }

    // ── DN-02: auto-denúncia bloqueada ────────────────────────────────────────

    @Test
    @DisplayName("DN-02: usuário não pode denunciar a si mesmo")
    void criar_AutoDenuncia_LancaBusinessException() {
        CriarDenunciaDTO dto = new CriarDenunciaDTO();
        dto.setDenunciadoId(1L);
        dto.setTipo(TipoDenuncia.COMPORTAMENTO);
        dto.setDescricao("tentativa de auto-denúncia");

        assertThatThrownBy(() -> denunciaService.criar(dto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("a si mesmo");
    }

    // ── DN-03: denúncia duplicada bloqueada ───────────────────────────────────

    @Test
    @DisplayName("DN-03: denúncia duplicada (mesmo tipo) é bloqueada")
    void criar_DenunciaDuplicada_LancaBusinessException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(denunciante));
        when(userRepository.findById(2L)).thenReturn(Optional.of(denunciado));
        when(denunciaRepository.existsByDenuncianteIdAndDenunciadoIdAndTipo(1L, 2L, TipoDenuncia.PERFIL))
                .thenReturn(true);

        CriarDenunciaDTO dto = new CriarDenunciaDTO();
        dto.setDenunciadoId(2L);
        dto.setTipo(TipoDenuncia.PERFIL);
        dto.setDescricao("já denunciei antes e vou de novo");

        assertThatThrownBy(() -> denunciaService.criar(dto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já denunciou");
    }

    // ── DN-04: denunciado suspenso bloqueado ──────────────────────────────────

    @Test
    @DisplayName("DN-04: não é possível denunciar usuário já suspenso")
    void criar_DenunciadoSuspenso_LancaBusinessException() {
        denunciado.setAtivo(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(denunciante));
        when(userRepository.findById(2L)).thenReturn(Optional.of(denunciado));

        CriarDenunciaDTO dto = new CriarDenunciaDTO();
        dto.setDenunciadoId(2L);
        dto.setTipo(TipoDenuncia.PERFIL);
        dto.setDescricao("usuário já suspenso");

        assertThatThrownBy(() -> denunciaService.criar(dto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("suspenso");
    }

    // ── DN-05: denunciado não encontrado ──────────────────────────────────────

    @Test
    @DisplayName("DN-05: denunciado inexistente lança ResourceNotFoundException")
    void criar_DenunciadoInexistente_LancaException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(denunciante));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        CriarDenunciaDTO dto = new CriarDenunciaDTO();
        dto.setDenunciadoId(99L);
        dto.setTipo(TipoDenuncia.COMPORTAMENTO);
        dto.setDescricao("usuário inexistente na base");

        assertThatThrownBy(() -> denunciaService.criar(dto, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── DN-06: aprovar denúncia ───────────────────────────────────────────────

    @Test
    @DisplayName("DN-06: aprovar denúncia PENDENTE atualiza status e data de revisão")
    void atualizarStatus_Aprovar_AtualizaStatusEData() {
        Denuncia denuncia = buildDenuncia(StatusDenuncia.PENDENTE);
        when(denunciaRepository.findById(1L)).thenReturn(Optional.of(denuncia));
        when(denunciaRepository.save(any())).thenReturn(denuncia);
        when(denunciaRepository.countByDenunciadoIdAndStatus(2L, StatusDenuncia.APROVADA)).thenReturn(1L);

        AtualizarDenunciaDTO dto = new AtualizarDenunciaDTO();
        dto.setStatus(StatusDenuncia.APROVADA);
        dto.setObservacaoAdmin("Comportamento comprovado");

        DenunciaDTO resultado = denunciaService.atualizarStatus(1L, dto);

        assertThat(resultado.getStatus()).isEqualTo(StatusDenuncia.APROVADA);
        assertThat(denuncia.getDataRevisao()).isNotNull();
        assertThat(denuncia.getObservacaoAdmin()).isEqualTo("Comportamento comprovado");
    }

    // ── DN-07: rejeitar denúncia ──────────────────────────────────────────────

    @Test
    @DisplayName("DN-07: rejeitar denúncia PENDENTE atualiza status sem suspender usuário")
    void atualizarStatus_Rejeitar_NaoSuspende() {
        Denuncia denuncia = buildDenuncia(StatusDenuncia.PENDENTE);
        when(denunciaRepository.findById(1L)).thenReturn(Optional.of(denuncia));
        when(denunciaRepository.save(any())).thenReturn(denuncia);

        AtualizarDenunciaDTO dto = new AtualizarDenunciaDTO();
        dto.setStatus(StatusDenuncia.REJEITADA);

        denunciaService.atualizarStatus(1L, dto);

        assertThat(denuncia.getStatus()).isEqualTo(StatusDenuncia.REJEITADA);
        verify(userRepository, never()).save(any());
    }

    // ── DN-08: tentar revisar denúncia não-PENDENTE ───────────────────────────

    @Test
    @DisplayName("DN-08: revisar denúncia já APROVADA lança BusinessException")
    void atualizarStatus_JaAprovada_LancaException() {
        Denuncia denuncia = buildDenuncia(StatusDenuncia.APROVADA);
        when(denunciaRepository.findById(1L)).thenReturn(Optional.of(denuncia));

        AtualizarDenunciaDTO dto = new AtualizarDenunciaDTO();
        dto.setStatus(StatusDenuncia.REJEITADA);

        assertThatThrownBy(() -> denunciaService.atualizarStatus(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDENTES");
    }

    // ── DN-09: suspensão automática ao atingir o limite ───────────────────────

    @Test
    @DisplayName("DN-09: suspensão automática ocorre ao atingir " + DenunciaService.LIMITE_SUSPENSAO + " denúncias aprovadas")
    void atualizarStatus_LimiteSuspensao_SuspendeDenunciado() {
        Denuncia denuncia = buildDenuncia(StatusDenuncia.PENDENTE);
        when(denunciaRepository.findById(1L)).thenReturn(Optional.of(denuncia));
        when(denunciaRepository.save(any())).thenReturn(denuncia);
        when(denunciaRepository.countByDenunciadoIdAndStatus(2L, StatusDenuncia.APROVADA))
                .thenReturn((long) DenunciaService.LIMITE_SUSPENSAO);

        AtualizarDenunciaDTO dto = new AtualizarDenunciaDTO();
        dto.setStatus(StatusDenuncia.APROVADA);

        denunciaService.atualizarStatus(1L, dto);

        assertThat(denunciado.getAtivo()).isFalse();
        verify(userRepository).save(denunciado);
    }

    // ── DN-10: contarPendentes ────────────────────────────────────────────────

    @Test
    @DisplayName("DN-10: contarPendentes delega para o repositório")
    void contarPendentes_DelegaParaRepositorio() {
        when(denunciaRepository.countByStatus(StatusDenuncia.PENDENTE)).thenReturn(5L);

        assertThat(denunciaService.contarPendentes()).isEqualTo(5L);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Denuncia buildDenuncia(StatusDenuncia status) {
        Denuncia d = new Denuncia();
        d.setId(1L);
        d.setDenunciante(denunciante);
        d.setDenunciado(denunciado);
        d.setTipo(TipoDenuncia.COMPORTAMENTO);
        d.setDescricao("Comportamento abusivo durante o serviço");
        d.setStatus(status);
        return d;
    }
}
