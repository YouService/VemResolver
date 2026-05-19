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
@Tag(name = "Avaliações", description = "Gerenciamento de avaliações bidirecionais")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Cliente avalia prestador")
    public ResponseEntity<AvaliacaoDTO> criar(
            @Valid @RequestBody AvaliacaoDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacaoService.criar(dto, user.getId()));
    }

    @PostMapping("/para-cliente")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Prestador avalia cliente")
    public ResponseEntity<AvaliacaoDTO> criarParaCliente(
            @Valid @RequestBody AvaliacaoDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacaoService.criarPorPrestador(dto, user.getId()));
    }

    @GetMapping("/prestador/{prestadorId}")
    @Operation(summary = "Listar avaliações recebidas por um prestador (filtro opcional por nota)")
    public ResponseEntity<Page<AvaliacaoDTO>> listarPorPrestador(
            @PathVariable Long prestadorId,
            @RequestParam(required = false) Integer nota,
            Pageable pageable) {
        return ResponseEntity.ok(avaliacaoService.listarPorPrestador(prestadorId, nota, pageable));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar avaliações recebidas por um cliente")
    public ResponseEntity<Page<AvaliacaoDTO>> listarPorCliente(
            @PathVariable Long clienteId,
            Pageable pageable) {
        return ResponseEntity.ok(avaliacaoService.listarPorCliente(clienteId, pageable));
    }

    @PatchMapping("/{id}/resposta")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Prestador responde a uma avaliação")
    public ResponseEntity<AvaliacaoDTO> responder(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(avaliacaoService.responderAvaliacao(id, body.get("resposta"), user.getId()));
    }

    @PatchMapping("/{id}")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Cliente edita sua avaliação (até 7 dias após criação)")
    public ResponseEntity<AvaliacaoDTO> editar(
            @PathVariable Long id,
            @RequestBody AvaliacaoDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(avaliacaoService.editarAvaliacao(id, dto.getNota(), dto.getComentario(), user.getId()));
    }
}
