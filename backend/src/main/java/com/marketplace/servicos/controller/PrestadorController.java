package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.PrestadorDTO;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.service.PrestadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prestadores")
@Tag(name = "Prestadores", description = "Gerenciamento de prestadores de serviços")
public class PrestadorController {
    
    private final PrestadorService prestadorService;
    
    public PrestadorController(PrestadorService prestadorService) {
        this.prestadorService = prestadorService;
    }
    
    @GetMapping
    @Operation(summary = "Buscar prestadores com filtros")
    public ResponseEntity<Page<PrestadorDTO>> buscar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double minAvaliacao,
            Pageable pageable) {
        return ResponseEntity.ok(prestadorService.buscar(categoriaId, cidade, estado, minAvaliacao, pageable));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar prestador por ID")
    public ResponseEntity<PrestadorDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(prestadorService.buscarPorId(id));
    }
    
    @GetMapping("/me")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Buscar perfil do prestador logado")
    public ResponseEntity<PrestadorDTO> buscarMeuPerfil(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(prestadorService.buscarPorUsuarioId(user.getId()));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('PRESTADOR')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Criar ou atualizar perfil de prestador (apenas PRESTADOR)")
    public ResponseEntity<PrestadorDTO> criarOuAtualizar(
            @Valid @RequestBody PrestadorDTO dto,
            @AuthenticationPrincipal User user) {
        dto.setUsuarioId(user.getId());
        return ResponseEntity.ok(prestadorService.criarOuAtualizar(dto));
    }

    @GetMapping("/proximo")
    @Operation(summary = "Buscar prestadores proximos por lat/lng e raio em km")
    public ResponseEntity<Page<PrestadorDTO>> buscarProximos(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double raioKm,
            @RequestParam(required = false) Long categoriaId,
            Pageable pageable) {
        return ResponseEntity.ok(prestadorService.buscarProximos(lat, lng, raioKm, categoriaId, pageable));
    }
}
