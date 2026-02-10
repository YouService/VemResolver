package com.marketplace.servicos.dto;

import com.marketplace.servicos.model.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    private String senha;
    
    @NotNull(message = "Tipo de usuário é obrigatório")
    private TipoUsuario tipo;

    private String telefone;

    private String cpf;

    private String rg;

    private String orgaoExpedidor;

    private String dataExpedicaoRg;
}
