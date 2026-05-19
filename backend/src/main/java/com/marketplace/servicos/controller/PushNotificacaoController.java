package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.PushSubscriptionDTO;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.service.WebPushService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes/push")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Push Notifications", description = "Assinaturas de notificações push via PWA")
public class PushNotificacaoController {

    private final WebPushService webPushService;

    public PushNotificacaoController(WebPushService webPushService) {
        this.webPushService = webPushService;
    }

    @GetMapping("/public-key")
    @Operation(summary = "Retorna a chave pública VAPID para uso no frontend")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        String key = webPushService.getPublicKey();
        if (key == null) return ResponseEntity.ok(Map.of("publicKey", ""));
        return ResponseEntity.ok(Map.of("publicKey", key));
    }

    @PostMapping("/subscribe")
    @Operation(summary = "Registra assinatura push do navegador")
    public ResponseEntity<Void> subscribe(
            @RequestBody PushSubscriptionDTO dto,
            @AuthenticationPrincipal User user) {
        webPushService.subscribe(user.getId(), dto.getEndpoint(), dto.getP256dh(), dto.getAuth());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/unsubscribe")
    @Operation(summary = "Remove assinatura push")
    public ResponseEntity<Void> unsubscribe(
            @RequestBody PushSubscriptionDTO dto,
            @AuthenticationPrincipal User user) {
        webPushService.unsubscribe(user.getId(), dto.getEndpoint());
        return ResponseEntity.noContent().build();
    }
}
