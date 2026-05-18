package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.FotoPrestadorDTO;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.service.FotoPrestadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/prestadores")
@Tag(name = "Fotos do Prestador", description = "Portfólio de fotos de trabalhos realizados")
public class FotoPrestadorController {

    private final FotoPrestadorService fotoService;

    public FotoPrestadorController(FotoPrestadorService fotoService) {
        this.fotoService = fotoService;
    }

    @PostMapping("/me/fotos")
    @PreAuthorize("hasRole('PRESTADOR')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Adicionar foto ao portfólio (máx. 10, 5 MB, JPEG/PNG/WEBP)")
    public ResponseEntity<FotoPrestadorDTO> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(fotoService.upload(file, user.getId()));
    }

    @DeleteMapping("/me/fotos/{fotoId}")
    @PreAuthorize("hasRole('PRESTADOR')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Remover foto do portfólio")
    public ResponseEntity<Void> deletar(
            @PathVariable Long fotoId,
            @AuthenticationPrincipal User user) {
        fotoService.deletar(fotoId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{prestadorId}/fotos")
    @Operation(summary = "Listar fotos do portfólio de um prestador")
    public ResponseEntity<List<FotoPrestadorDTO>> listar(@PathVariable Long prestadorId) {
        return ResponseEntity.ok(fotoService.listar(prestadorId));
    }
}
