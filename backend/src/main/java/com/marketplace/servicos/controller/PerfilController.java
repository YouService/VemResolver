package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.AlterarSenhaDTO;
import com.marketplace.servicos.dto.UpdatePerfilDTO;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.service.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
@Tag(name = "Perfil", description = "Endpoints de gerenciamento de perfil do usuário")
@CrossOrigin(origins = "http://localhost:4200")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    @Operation(summary = "Buscar perfil do usuário atual")
    public ResponseEntity<User> buscarPerfil(@AuthenticationPrincipal User user) {
        User perfil = perfilService.buscarPorId(user.getId());
        perfil.setSenha(null);
        return ResponseEntity.ok(perfil);
    }

    @PutMapping
    @Operation(summary = "Atualizar perfil do usuário")
    public ResponseEntity<User> atualizarPerfil(
            @Valid @RequestBody UpdatePerfilDTO updatePerfilDTO,
            @AuthenticationPrincipal User user) {
        User perfilAtualizado = perfilService.atualizarPerfil(user.getId(), updatePerfilDTO);
        perfilAtualizado.setSenha(null);
        return ResponseEntity.ok(perfilAtualizado);
    }

    @PutMapping("/senha")
    @Operation(summary = "Alterar senha do usuário")
    public ResponseEntity<Void> alterarSenha(
            @Valid @RequestBody AlterarSenhaDTO alterarSenhaDTO,
            @AuthenticationPrincipal User user) {
        perfilService.alterarSenha(user.getId(), alterarSenhaDTO);
        return ResponseEntity.ok().build();
    }
}
