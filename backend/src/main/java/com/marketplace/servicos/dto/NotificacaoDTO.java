package com.marketplace.servicos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoDTO {
    private Long id;
    private String mensagem;
    private String tipo;
    private Boolean lida;
    private String linkDestino;
    private LocalDateTime dataCriacao;
}
