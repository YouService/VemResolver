package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.AvaliacaoVerificacaoRequestDTO;
import com.marketplace.servicos.dto.VerificacaoDocumentoDTO;
import com.marketplace.servicos.model.StatusVerificacao;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.service.VerificacaoDocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Verificação de Documento", description = "Verificação de identidade do prestador via CPF e selfie")
public class VerificacaoDocumentoController {

    private final VerificacaoDocumentoService verificacaoService;

    public VerificacaoDocumentoController(VerificacaoDocumentoService verificacaoService) {
        this.verificacaoService = verificacaoService;
    }

    // ── Endpoints do Prestador ─────────────────────────────────────────────────

    @PostMapping("/api/prestadores/me/verificacao")
    @PreAuthorize("hasRole('PRESTADOR')")
    @Operation(summary = "Envia documento e selfie para verificação de identidade")
    public ResponseEntity<VerificacaoDocumentoDTO> enviarDocumentos(
            @RequestParam("documento") MultipartFile documento,
            @RequestParam("selfie") MultipartFile selfie,
            @AuthenticationPrincipal User usuario) {
        VerificacaoDocumentoDTO dto = verificacaoService.enviarDocumentos(documento, selfie, usuario.getId());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/api/prestadores/me/verificacao")
    @PreAuthorize("hasRole('PRESTADOR')")
    @Operation(summary = "Consulta o status atual da verificação de identidade do prestador")
    public ResponseEntity<VerificacaoDocumentoDTO> consultarStatus(@AuthenticationPrincipal User usuario) {
        VerificacaoDocumentoDTO dto = verificacaoService.buscarPorPrestador(usuario.getId());
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }

    // ── Endpoints Admin ────────────────────────────────────────────────────────

    @GetMapping("/api/admin/verificacoes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar verificações de documento (todas ou filtradas por status)")
    public ResponseEntity<Page<VerificacaoDocumentoDTO>> listar(
            @RequestParam(required = false) StatusVerificacao status,
            Pageable pageable) {
        return ResponseEntity.ok(verificacaoService.listar(status, pageable));
    }

    @PatchMapping("/api/admin/verificacoes/{id}/aprovar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprovar verificação de identidade — seta idVerificado=true no prestador")
    public ResponseEntity<VerificacaoDocumentoDTO> aprovar(
            @PathVariable Long id,
            @RequestBody(required = false) AvaliacaoVerificacaoRequestDTO body) {
        String obs = body != null ? body.getObservacao() : null;
        return ResponseEntity.ok(verificacaoService.aprovar(id, obs));
    }

    @PatchMapping("/api/admin/verificacoes/{id}/recusar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Recusar verificação de identidade com observação")
    public ResponseEntity<VerificacaoDocumentoDTO> recusar(
            @PathVariable Long id,
            @RequestBody(required = false) AvaliacaoVerificacaoRequestDTO body) {
        String obs = body != null ? body.getObservacao() : null;
        return ResponseEntity.ok(verificacaoService.recusar(id, obs));
    }

    @GetMapping("/api/admin/verificacoes/{id}/documento")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Baixar o documento enviado pelo prestador (acesso restrito ao admin)")
    public ResponseEntity<InputStreamResource> verDocumento(@PathVariable Long id) {
        return verificacaoService.servirArquivo(id, true);
    }

    @GetMapping("/api/admin/verificacoes/{id}/selfie")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Baixar a selfie enviada pelo prestador (acesso restrito ao admin)")
    public ResponseEntity<InputStreamResource> verSelfie(@PathVariable Long id) {
        return verificacaoService.servirArquivo(id, false);
    }
}
