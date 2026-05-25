package com.marketplace.servicos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestadorRecomendadoDTO {
    private Long id;
    private Long usuarioId;
    private List<Long> categoriasIds;
    private String descricaoPerfil;
    private String cidade;
    private String estado;
    private Double avaliacaoMedia;
    private Integer totalAvaliacoes;
    private Double latitude;
    private Double longitude;
    private String fotoPerfilUrl;
    private Integer percentualCompletude;
    private Boolean verificado;
    private Boolean idVerificado;
    private Double mediaPontualidade;
    private Double mediaQualidade;
    private Double mediaComunicacao;
    private Double score;
    private String justificativa;
}
