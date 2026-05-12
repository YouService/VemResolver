package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.SolicitacaoServicoDTO;
import com.marketplace.servicos.model.StatusSolicitacao;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.service.SolicitacaoServicoService;
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
@RequestMapping("/api/solicitacoes")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Solicitações de Serviço", description = "Gerenciamento de solicitações de serviço")
@CrossOrigin(origins = "http://localhost:4200")
public class SolicitacaoServicoController {
    
    private final SolicitacaoServicoService solicitacaoService;
    
    public SolicitacaoServicoController(SolicitacaoServicoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }
    
    @PostMapping
    @Operation(summary = "Criar nova solicitação de serviço")
    public ResponseEntity<SolicitacaoServicoDTO> criar(
            @Valid @RequestBody SolicitacaoServicoDTO dto,
            @AuthenticationPrincipal User user) {
        SolicitacaoServicoDTO criada = solicitacaoService.criar(dto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar solicitação por ID")
    public ResponseEntity<SolicitacaoServicoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(solicitacaoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar solicitação de serviço")
    public ResponseEntity<SolicitacaoServicoDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitacaoServicoDTO dto,
            @AuthenticationPrincipal User user) {
        SolicitacaoServicoDTO atualizada = solicitacaoService.atualizar(id, dto, user.getId());
        return ResponseEntity.ok(atualizada);
    }
    
    @GetMapping("/minhas")
    @Operation(summary = "Listar minhas solicitações")
    public ResponseEntity<Page<SolicitacaoServicoDTO>> listarMinhas(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        return ResponseEntity.ok(solicitacaoService.listarPorCliente(user.getId(), pageable));
    }
    
    @GetMapping("/abertas")
    @Operation(summary = "Buscar solicitações abertas")
    public ResponseEntity<Page<SolicitacaoServicoDTO>> buscarAbertas(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado,
            Pageable pageable) {
        return ResponseEntity.ok(solicitacaoService.buscarAbertas(categoriaId, cidade, estado, pageable));
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status da solicitação")
    public ResponseEntity<SolicitacaoServicoDTO> atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusSolicitacao status) {
        return ResponseEntity.ok(solicitacaoService.atualizarStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir solicitação")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        solicitacaoService.deletar(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
