package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.VerificacaoDocumentoDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.Prestador;
import com.marketplace.servicos.model.StatusVerificacao;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.model.VerificacaoDocumento;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.VerificacaoDocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificacaoDocumentoServiceTest {

    @TempDir
    Path tempDir;

    @Mock private VerificacaoDocumentoRepository verificacaoRepository;
    @Mock private PrestadorRepository prestadorRepository;

    @InjectMocks
    private VerificacaoDocumentoService verificacaoService;

    private Prestador prestador;
    private MockMultipartFile docFile;
    private MockMultipartFile selfieFile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(verificacaoService, "uploadDir", tempDir.toString());

        User usuario = new User();
        usuario.setId(1L);
        usuario.setNome("Maria Prestador");

        prestador = new Prestador();
        prestador.setId(10L);
        prestador.setUsuario(usuario);
        prestador.setIdVerificado(false);

        docFile = new MockMultipartFile("documento", "cpf.jpg", "image/jpeg", "doc-bytes".getBytes());
        selfieFile = new MockMultipartFile("selfie", "selfie.jpg", "image/jpeg", "selfie-bytes".getBytes());
    }

    // ── VD-01: envio bem-sucedido ─────────────────────────────────────────────

    @Test
    @DisplayName("VD-01: enviar documentos válidos cria verificação PENDENTE")
    void enviarDocumentos_ArquivosValidos_CriaVerificacaoPendente() {
        when(prestadorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(prestador));
        when(verificacaoRepository.findTopByPrestadorIdOrderByDataEnvioDesc(10L)).thenReturn(Optional.empty());

        VerificacaoDocumento salvo = new VerificacaoDocumento();
        salvo.setId(1L);
        salvo.setPrestador(prestador);
        salvo.setStatus(StatusVerificacao.PENDENTE);
        salvo.setDataEnvio(LocalDateTime.now());
        when(verificacaoRepository.save(any())).thenReturn(salvo);

        VerificacaoDocumentoDTO resultado = verificacaoService.enviarDocumentos(docFile, selfieFile, 1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(StatusVerificacao.PENDENTE);
        assertThat(resultado.getPrestadorNome()).isEqualTo("Maria Prestador");
        verify(verificacaoRepository).save(any());
    }

    // ── VD-02: reenvio substitui verificação anterior ────────────────────────

    @Test
    @DisplayName("VD-02: reenvio de documentos quando RECUSADO substitui verificação anterior")
    void enviarDocumentos_QuandoRecusado_SubstituiVerificacao() {
        when(prestadorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(prestador));

        VerificacaoDocumento anterior = new VerificacaoDocumento();
        anterior.setId(5L);
        anterior.setPrestador(prestador);
        anterior.setStatus(StatusVerificacao.RECUSADO);
        when(verificacaoRepository.findTopByPrestadorIdOrderByDataEnvioDesc(10L)).thenReturn(Optional.of(anterior));

        VerificacaoDocumento salvo = new VerificacaoDocumento();
        salvo.setId(5L);
        salvo.setPrestador(prestador);
        salvo.setStatus(StatusVerificacao.PENDENTE);
        salvo.setDataEnvio(LocalDateTime.now());
        when(verificacaoRepository.save(any())).thenReturn(salvo);

        VerificacaoDocumentoDTO resultado = verificacaoService.enviarDocumentos(docFile, selfieFile, 1L);

        assertThat(resultado.getStatus()).isEqualTo(StatusVerificacao.PENDENTE);
    }

    // ── VD-03: bloqueio quando já APROVADO ───────────────────────────────────

    @Test
    @DisplayName("VD-03: envio bloqueado quando já APROVADO")
    void enviarDocumentos_QuandoJaAprovado_LancaBusinessException() {
        when(prestadorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(prestador));

        VerificacaoDocumento aprovado = new VerificacaoDocumento();
        aprovado.setStatus(StatusVerificacao.APROVADO);
        when(verificacaoRepository.findTopByPrestadorIdOrderByDataEnvioDesc(10L)).thenReturn(Optional.of(aprovado));

        assertThatThrownBy(() -> verificacaoService.enviarDocumentos(docFile, selfieFile, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já foi verificada");
    }

    // ── VD-04: arquivo muito grande ──────────────────────────────────────────

    @Test
    @DisplayName("VD-04: arquivo maior que 10 MB lança BusinessException")
    void enviarDocumentos_ArquivoGrande_LancaException() {
        byte[] grande = new byte[11 * 1024 * 1024];
        MockMultipartFile docGrande = new MockMultipartFile("doc", "doc.jpg", "image/jpeg", grande);

        assertThatThrownBy(() -> verificacaoService.enviarDocumentos(docGrande, selfieFile, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tamanho máximo");
    }

    // ── VD-05: tipo de arquivo inválido ──────────────────────────────────────

    @Test
    @DisplayName("VD-05: tipo de arquivo não suportado (PDF) lança BusinessException")
    void enviarDocumentos_TipoInvalido_LancaException() {
        MockMultipartFile pdf = new MockMultipartFile("doc", "doc.pdf", "application/pdf", "bytes".getBytes());

        assertThatThrownBy(() -> verificacaoService.enviarDocumentos(pdf, selfieFile, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tipo não suportado");
    }

    // ── VD-06: aprovar verificação PENDENTE ──────────────────────────────────

    @Test
    @DisplayName("VD-06: aprovar verificação PENDENTE seta idVerificado=true no prestador")
    void aprovar_VerificacaoPendente_SetaIdVerificadoTrue() {
        VerificacaoDocumento verificacao = new VerificacaoDocumento();
        verificacao.setId(1L);
        verificacao.setPrestador(prestador);
        verificacao.setStatus(StatusVerificacao.PENDENTE);
        when(verificacaoRepository.findById(1L)).thenReturn(Optional.of(verificacao));
        when(verificacaoRepository.save(any())).thenReturn(verificacao);
        when(prestadorRepository.save(any())).thenReturn(prestador);

        VerificacaoDocumentoDTO resultado = verificacaoService.aprovar(1L, "Documento válido");

        assertThat(resultado.getStatus()).isEqualTo(StatusVerificacao.APROVADO);
        assertThat(prestador.getIdVerificado()).isTrue();
        verify(prestadorRepository).save(prestador);
    }

    // ── VD-07: recusar verificação PENDENTE ──────────────────────────────────

    @Test
    @DisplayName("VD-07: recusar verificação PENDENTE registra observação e não altera idVerificado")
    void recusar_VerificacaoPendente_RegistraObservacao() {
        VerificacaoDocumento verificacao = new VerificacaoDocumento();
        verificacao.setId(1L);
        verificacao.setPrestador(prestador);
        verificacao.setStatus(StatusVerificacao.PENDENTE);
        when(verificacaoRepository.findById(1L)).thenReturn(Optional.of(verificacao));
        when(verificacaoRepository.save(any())).thenReturn(verificacao);

        VerificacaoDocumentoDTO resultado = verificacaoService.recusar(1L, "Documento ilegível");

        assertThat(resultado.getStatus()).isEqualTo(StatusVerificacao.RECUSADO);
        verify(prestadorRepository, never()).save(any());
    }

    // ── VD-08: aprovar verificação já APROVADA lança exceção ─────────────────

    @Test
    @DisplayName("VD-08: tentativa de aprovar verificação já APROVADA lança BusinessException")
    void aprovar_QuandoJaAprovado_LancaException() {
        VerificacaoDocumento verificacao = new VerificacaoDocumento();
        verificacao.setId(1L);
        verificacao.setPrestador(prestador);
        verificacao.setStatus(StatusVerificacao.APROVADO);
        when(verificacaoRepository.findById(1L)).thenReturn(Optional.of(verificacao));

        assertThatThrownBy(() -> verificacaoService.aprovar(1L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDENTE");
    }

    // ── VD-09: prestador sem perfil lança ResourceNotFoundException ──────────

    @Test
    @DisplayName("VD-09: envio sem perfil de prestador lança ResourceNotFoundException")
    void enviarDocumentos_SemPerfil_LancaResourceNotFoundException() {
        when(prestadorRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verificacaoService.enviarDocumentos(docFile, selfieFile, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── VD-10: contar pendentes ──────────────────────────────────────────────

    @Test
    @DisplayName("VD-10: contarPendentes delega para o repositório")
    void contarPendentes_DelegaParaRepositorio() {
        when(verificacaoRepository.countByStatus(StatusVerificacao.PENDENTE)).thenReturn(3L);

        long total = verificacaoService.contarPendentes();

        assertThat(total).isEqualTo(3L);
    }
}
