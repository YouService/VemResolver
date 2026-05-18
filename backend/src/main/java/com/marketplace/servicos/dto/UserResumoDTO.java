package com.marketplace.servicos.dto;

import com.marketplace.servicos.model.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResumoDTO {
    private Long id;
    private String nome;
    private String email;
    private TipoUsuario tipo;
    private boolean ativo;
    private LocalDateTime dataCadastro;
    private Double avaliacaoMedia;
    private Integer totalAvaliacoes;
    private LocalDateTime ultimoAcesso;
    private boolean online;
}
