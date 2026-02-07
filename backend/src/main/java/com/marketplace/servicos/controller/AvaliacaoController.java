package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.AvaliacaoDTO;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.service.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/avaliacoes")
@Tag(name = "Avaliações", description = "Gerenciamento de avaliações de prestadores")
@CrossOrigin(origins = "http://localhost:4200")
public class AvaliacaoController {
    
    private final AvaliacaoService avaliacaoService;
    
    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }
    
    @PostMapping
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Criar avaliação")
    public ResponseEntity<AvaliacaoDTO> criar(
            @Valid @RequestBody AvaliacaoDTO dto,
            @AuthenticationPrincipal User user) {
        AvaliacaoDTO criada = avaliacaoService.criar(dto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }
    
    @GetMapping("/prestador/{prestadorId}")
    @Operation(summary = "Listar avaliações de um prestador")
    public ResponseEntity<Page<AvaliacaoDTO>> listarPorPrestador(
            @PathVariable Long prestadorId,
            Pageable pageable) {
        return ResponseEntity.ok(avaliacaoService.listarPorPrestador(prestadorId, pageable));
    }
}
