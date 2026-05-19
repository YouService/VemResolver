package com.marketplace.servicos.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestadorDTO {
    private Long id;
    
    @NotNull(message = "ID do usuário é obrigatório")
    private Long usuarioId;
    
    @NotEmpty(message = "Pelo menos uma categoria é obrigatória")
    private List<Long> categoriasIds;
    
    private String descricaoPerfil;
    
    @NotNull(message = "Cidade é obrigatória")
    private String cidade;
    
    @NotNull(message = "Estado é obrigatório")
    private String estado;
    
    private Double avaliacaoMedia;
    private Integer totalAvaliacoes;
    private Double latitude;
    private Double longitude;
    private String fotoPerfilUrl;
    private Integer percentualCompletude;
    private Boolean verificado;
    private Double mediaPontualidade;
    private Double mediaQualidade;
    private Double mediaComunicacao;
}
