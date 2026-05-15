package com.marketplace.servicos.dto;

import com.marketplace.servicos.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String refreshToken;
    private String tipo = "Bearer";
    private Long userId;
    private String nome;
    private String email;
    private TipoUsuario tipoUsuario;

    public AuthResponseDTO(String token, Long userId, String nome, String email, TipoUsuario tipoUsuario) {
        this.token = token;
        this.userId = userId;
        this.nome = nome;
        this.email = email;
        this.tipoUsuario = tipoUsuario;
    }

    public AuthResponseDTO(String token, String refreshToken, Long userId, String nome, String email, TipoUsuario tipoUsuario) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.nome = nome;
        this.email = email;
        this.tipoUsuario = tipoUsuario;
    }
}
