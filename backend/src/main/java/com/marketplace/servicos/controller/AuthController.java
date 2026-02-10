package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.AuthResponseDTO;
import com.marketplace.servicos.dto.LoginDTO;
import com.marketplace.servicos.dto.RegisterDTO;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação e registro")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        AuthResponseDTO response = authService.register(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Fazer login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        AuthResponseDTO response = authService.login(loginDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/conta")
    @Operation(summary = "Excluir conta do usuário")
    public ResponseEntity<Void> deletarConta(@AuthenticationPrincipal User user) {
        authService.deletarConta(user.getId());
        return ResponseEntity.noContent().build();
    }
}
