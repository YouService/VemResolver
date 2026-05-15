package com.marketplace.servicos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RefreshRequestDTO {
    @NotBlank(message = "Refresh token é obrigatório")
    private String refreshToken;
}
