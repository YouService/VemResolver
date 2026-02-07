package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.PropostaDTO;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.service.PropostaService;
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
@RequestMapping("/api/propostas")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Propostas", description = "Gerenciamento de propostas de serviço")
@CrossOrigin(origins = "http://localhost:4200")
public class PropostaController {
    
    private final PropostaService propostaService;
    private final PrestadorRepository prestadorRepository;
    
    public PropostaController(PropostaService propostaService, PrestadorRepository prestadorRepository) {
        this.propostaService = propostaService;
        this.prestadorRepository = prestadorRepository;
    }
    
    @PostMapping
    @Operation(summary = "Enviar proposta para uma solicitação")
    public ResponseEntity<PropostaDTO> criar(
            @Valid @RequestBody PropostaDTO dto,
            @AuthenticationPrincipal User user) {
        Long prestadorId = prestadorRepository.findByUsuarioId(user.getId())
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado"))
                .getId();
        PropostaDTO criada = propostaService.criar(dto, prestadorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }
    
    @GetMapping("/solicitacao/{solicitacaoId}")
    @Operation(summary = "Listar propostas de uma solicitação")
    public ResponseEntity<Page<PropostaDTO>> listarPorSolicitacao(
            @PathVariable Long solicitacaoId,
            Pageable pageable) {
        return ResponseEntity.ok(propostaService.listarPorSolicitacao(solicitacaoId, pageable));
    }
    
    @GetMapping("/minhas")
    @Operation(summary = "Listar minhas propostas")
    public ResponseEntity<Page<PropostaDTO>> listarMinhas(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        Long prestadorId = prestadorRepository.findByUsuarioId(user.getId())
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado"))
                .getId();
        return ResponseEntity.ok(propostaService.listarPorPrestador(prestadorId, pageable));
    }
    
    @PatchMapping("/{id}/aceitar")
    @Operation(summary = "Aceitar proposta")
    public ResponseEntity<PropostaDTO> aceitar(@PathVariable Long id) {
        return ResponseEntity.ok(propostaService.aceitarProposta(id));
    }
    
    @PatchMapping("/{id}/recusar")
    @Operation(summary = "Recusar proposta")
    public ResponseEntity<PropostaDTO> recusar(@PathVariable Long id) {
        return ResponseEntity.ok(propostaService.recusarProposta(id));
    }
}
