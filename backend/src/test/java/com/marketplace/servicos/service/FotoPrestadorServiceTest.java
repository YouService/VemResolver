package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.FotoPrestadorDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.FotoPrestador;
import com.marketplace.servicos.model.Prestador;
import com.marketplace.servicos.model.TipoUsuario;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.FotoPrestadorRepository;
import com.marketplace.servicos.repository.PrestadorRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FotoPrestadorServiceTest {

    @TempDir
    Path tempDir;

    @Mock private FotoPrestadorRepository fotoRepository;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private PrestadorService prestadorService;

    @InjectMocks
    private FotoPrestadorService fotoService;

    private Prestador prestador;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fotoService, "uploadDir", tempDir.toString());

        User usuario = new User();
        usuario.setId(1L);
        usuario.setNome("Maria");
        usuario.setTipo(TipoUsuario.PRESTADOR);

        prestador = new Prestador();
        prestador.setId(10L);
        prestador.setUsuario(usuario);
        prestador.setAvaliacaoMedia(0.0);
        prestador.setTotalAvaliacoes(0);
    }

    // ── upload (portfólio) ──────────────────────────────────────────────────

    @Test
    @DisplayName("upload: JPEG válido abaixo de 5 MB salva arquivo e retorna DTO")
    void upload_ArquivoJpegValido_SalvaERetornaDTO() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", new byte[1024]);

        when(prestadorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(prestador));
        when(fotoRepository.countByPrestadorId(10L)).thenReturn(3L);
        FotoPrestador salva = new FotoPrestador();
        salva.setId(100L);
        salva.setPrestador(prestador);
        salva.setUrl("/uploads/prestadores/10/foto.jpg");
        when(fotoRepository.save(any(FotoPrestador.class))).thenReturn(salva);

        FotoPrestadorDTO dto = fotoService.upload(file, 1L);

        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        verify(fotoRepository).save(any(FotoPrestador.class));
        verify(prestadorService).recalcularCompletude(prestador);
    }

    @Test
    @DisplayName("upload: tipo de arquivo não suportado (PDF) lança BusinessException")
    void upload_TipoInvalido_LancaBusinessException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "documento.pdf", "application/pdf", new byte[100]);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> fotoService.upload(file, 1L));
        assertTrue(ex.getMessage().contains("Tipo de arquivo não suportado"));
        verify(fotoRepository, never()).save(any());
    }

    @Test
    @DisplayName("upload: arquivo vazio lança BusinessException")
    void upload_ArquivoVazio_LancaBusinessException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "vazio.jpg", "image/jpeg", new byte[0]);

        assertThrows(BusinessException.class, () -> fotoService.upload(file, 1L));
        verify(fotoRepository, never()).save(any());
    }

    @Test
    @DisplayName("upload: arquivo maior que 5 MB lança BusinessException")
    void upload_TamanhoExcedido_LancaBusinessException() {
        byte[] dadosGrandes = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "grande.jpg", "image/jpeg", dadosGrandes);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> fotoService.upload(file, 1L));
        assertTrue(ex.getMessage().contains("5 MB"));
        verify(fotoRepository, never()).save(any());
    }

    @Test
    @DisplayName("upload: 10 fotos já cadastradas lança BusinessException")
    void upload_LimiteAtingido_LancaBusinessException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", new byte[512]);

        when(prestadorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(prestador));
        when(fotoRepository.countByPrestadorId(10L)).thenReturn(10L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> fotoService.upload(file, 1L));
        assertTrue(ex.getMessage().contains("Limite"));
        verify(fotoRepository, never()).save(any());
    }

    @Test
    @DisplayName("upload: prestador não encontrado lança ResourceNotFoundException")
    void upload_PrestadorNaoEncontrado_LancaException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", new byte[512]);

        when(prestadorRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> fotoService.upload(file, 99L));
    }

    // ── uploadAvatar ────────────────────────────────────────────────────────

    @Test
    @DisplayName("uploadAvatar: JPEG válido atualiza fotoPerfilUrl e recalcula completude")
    void uploadAvatar_ArquivoValido_AtualizaFotoPerfilUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", new byte[1024]);

        when(prestadorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(prestador));
        when(prestadorRepository.save(prestador)).thenReturn(prestador);

        String url = fotoService.uploadAvatar(file, 1L);

        assertNotNull(url);
        assertTrue(url.contains("/uploads/prestadores/10/avatar"));
        assertEquals(url, prestador.getFotoPerfilUrl());
        verify(prestadorService).recalcularCompletude(prestador);
    }

    @Test
    @DisplayName("uploadAvatar: prestador não encontrado lança ResourceNotFoundException")
    void uploadAvatar_PrestadorNaoEncontrado_LancaException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", new byte[256]);

        when(prestadorRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> fotoService.uploadAvatar(file, 99L));
    }

    // ── deletarAvatar ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deletarAvatar: remove url e recalcula completude")
    void deletarAvatar_ComAvatar_RemoveUrlERecalcula() {
        prestador.setFotoPerfilUrl("/uploads/prestadores/10/avatar.jpg");
        when(prestadorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(prestador));
        when(prestadorRepository.save(prestador)).thenReturn(prestador);

        fotoService.deletarAvatar(1L);

        assertNull(prestador.getFotoPerfilUrl());
        verify(prestadorService).recalcularCompletude(prestador);
    }

    @Test
    @DisplayName("deletarAvatar: prestador sem avatar não chama save nem recalcula")
    void deletarAvatar_SemAvatar_NaoFazNada() {
        prestador.setFotoPerfilUrl(null);
        when(prestadorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(prestador));

        fotoService.deletarAvatar(1L);

        verify(prestadorRepository, never()).save(any());
        verify(prestadorService, never()).recalcularCompletude(any());
    }

    // ── deletar (portfólio) ─────────────────────────────────────────────────

    @Test
    @DisplayName("deletar: foto do próprio prestador é removida com sucesso")
    void deletar_FotoDoProprietario_DeletaComSucesso() {
        FotoPrestador foto = new FotoPrestador();
        foto.setId(50L);
        foto.setPrestador(prestador);
        foto.setUrl("/uploads/prestadores/10/foto.jpg");

        when(fotoRepository.findById(50L)).thenReturn(Optional.of(foto));
        when(prestadorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(prestador));

        fotoService.deletar(50L, 1L);

        verify(fotoRepository).delete(foto);
        verify(prestadorService).recalcularCompletude(prestador);
    }

    @Test
    @DisplayName("deletar: foto de outro prestador lança BusinessException")
    void deletar_FotoDeOutroPrestador_LancaBusinessException() {
        Prestador outro = new Prestador();
        outro.setId(99L);

        FotoPrestador foto = new FotoPrestador();
        foto.setId(50L);
        foto.setPrestador(outro);
        foto.setUrl("/uploads/prestadores/99/foto.jpg");

        when(fotoRepository.findById(50L)).thenReturn(Optional.of(foto));
        when(prestadorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(prestador));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> fotoService.deletar(50L, 1L));
        assertTrue(ex.getMessage().contains("permissão"));
        verify(fotoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deletar: foto inexistente lança ResourceNotFoundException")
    void deletar_FotoInexistente_LancaException() {
        when(fotoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> fotoService.deletar(99L, 1L));
    }

    // ── listar ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listar: prestador existente retorna lista de DTOs")
    void listar_PrestadorExistente_RetornaLista() {
        FotoPrestador f1 = new FotoPrestador(); f1.setId(1L); f1.setUrl("/a.jpg"); f1.setPrestador(prestador);
        FotoPrestador f2 = new FotoPrestador(); f2.setId(2L); f2.setUrl("/b.jpg"); f2.setPrestador(prestador);

        when(prestadorRepository.existsById(10L)).thenReturn(true);
        when(fotoRepository.findByPrestadorIdOrderByDataUploadAsc(10L)).thenReturn(List.of(f1, f2));

        List<FotoPrestadorDTO> lista = fotoService.listar(10L);

        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("listar: prestador inexistente lança ResourceNotFoundException")
    void listar_PrestadorInexistente_LancaException() {
        when(prestadorRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> fotoService.listar(99L));
    }
}
