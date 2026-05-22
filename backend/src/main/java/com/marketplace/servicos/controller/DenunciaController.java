package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.AtualizarDenunciaDTO;
import com.marketplace.servicos.dto.CriarDenunciaDTO;
import com.marketplace.servicos.dto.DenunciaDTO;
import com.marketplace.servicos.model.StatusDenuncia;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.service.DenunciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Denúncias", description = "Denúncia de usuários e conteúdo inadequado")
public class DenunciaController {

    private final DenunciaService denunciaService;

    public DenunciaController(DenunciaService denunciaService) {
        this.denunciaService = denunciaService;
    }

    @PostMapping("/api/denuncias")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Registrar denúncia contra um usuário")
    public ResponseEntity<DenunciaDTO> criar(
            @RequestBody @Valid CriarDenunciaDTO dto,
            @AuthenticationPrincipal User usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(denunciaService.criar(dto, usuario.getId()));
    }

    @GetMapping("/api/admin/denuncias")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar denúncias (todas ou filtradas por status)")
    public ResponseEntity<Page<DenunciaDTO>> listar(
            @RequestParam(required = false) StatusDenuncia status,
            Pageable pageable) {
        return ResponseEntity.ok(denunciaService.listar(status, pageable));
    }

    @PatchMapping("/api/admin/denuncias/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar status de uma denúncia (APROVADA ou REJEITADA)")
    public ResponseEntity<DenunciaDTO> atualizarStatus(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarDenunciaDTO dto) {
        return ResponseEntity.ok(denunciaService.atualizarStatus(id, dto));
    }
}
