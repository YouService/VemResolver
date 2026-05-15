package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.NotificacaoDTO;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.service.NotificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
@Tag(name = "Notificacoes", description = "Gerenciamento de notificacoes do usuario")
@SecurityRequirement(name = "bearer-jwt")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    @Operation(summary = "Listar notificacoes nao lidas do usuario autenticado")
    public ResponseEntity<List<NotificacaoDTO>> listarNaoLidas(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificacaoService.listarNaoLidas(user.getId()));
    }

    @GetMapping("/todas")
    @Operation(summary = "Listar todas as notificacoes do usuario autenticado com paginacao")
    public ResponseEntity<Page<NotificacaoDTO>> listarTodas(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        return ResponseEntity.ok(notificacaoService.listarTodas(user.getId(), pageable));
    }

    @GetMapping("/count")
    @Operation(summary = "Contar notificacoes nao lidas do usuario autenticado")
    public ResponseEntity<Map<String, Long>> contarNaoLidas(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of("count", notificacaoService.contarNaoLidas(user.getId())));
    }

    @PatchMapping("/{id}/lida")
    @Operation(summary = "Marcar uma notificacao como lida")
    public ResponseEntity<Void> marcarComoLida(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        notificacaoService.marcarComoLida(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/ler-todas")
    @Operation(summary = "Marcar todas as notificacoes como lidas")
    public ResponseEntity<Void> marcarTodasComoLidas(@AuthenticationPrincipal User user) {
        notificacaoService.marcarTodasComoLidas(user.getId());
        return ResponseEntity.noContent().build();
    }
}
