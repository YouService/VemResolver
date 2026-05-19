package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.MensagemChatDTO;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.service.ChatService;
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

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Chat", description = "Chat interno entre cliente e prestador")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/proposta/{propostaId}")
    @Operation(summary = "Listar mensagens do chat de uma proposta")
    public ResponseEntity<List<MensagemChatDTO>> listar(
            @PathVariable Long propostaId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatService.listarMensagens(propostaId, user.getId()));
    }

    @GetMapping("/proposta/{propostaId}/paginado")
    @Operation(summary = "Listar mensagens paginadas (mais recentes primeiro)")
    public ResponseEntity<Page<MensagemChatDTO>> listarPaginado(
            @PathVariable Long propostaId,
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        return ResponseEntity.ok(chatService.listarMensagensPaginado(propostaId, user.getId(), pageable));
    }

    @PostMapping("/proposta/{propostaId}")
    @Operation(summary = "Enviar mensagem no chat")
    public ResponseEntity<MensagemChatDTO> enviar(
            @PathVariable Long propostaId,
            @Valid @RequestBody MensagemChatDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.enviarMensagem(propostaId, user.getId(), dto.getTexto()));
    }

    @PatchMapping("/proposta/{propostaId}/ler")
    @Operation(summary = "Marcar mensagens recebidas como lidas")
    public ResponseEntity<Void> marcarComoLidas(
            @PathVariable Long propostaId,
            @AuthenticationPrincipal User user) {
        chatService.marcarComoLidas(propostaId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
