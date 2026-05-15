package com.marketplace.servicos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensagemChatDTO {

    private Long id;
    private Long propostaId;
    private Long remetenteId;
    private String remetenteNome;

    @NotBlank(message = "Texto é obrigatório")
    @Size(max = 2000, message = "Texto deve ter no máximo 2000 caracteres")
    private String texto;

    private LocalDateTime dataEnvio;
    private Boolean lida;
}
