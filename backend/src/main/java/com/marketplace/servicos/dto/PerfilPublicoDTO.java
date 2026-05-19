package com.marketplace.servicos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilPublicoDTO {

    private Long id;
    private Long usuarioId;
    private String nome;
    private String fotoPerfilUrl;
    private List<String> categorias;
    private String cidade;
    private String estado;
    private Double avaliacaoMedia;
    private Integer totalAvaliacoes;
    private boolean verificado;
    private Double mediaPontualidade;
    private Double mediaQualidade;
    private Double mediaComunicacao;
    private List<String> portafolioUrls;
    private List<AvaliacaoResumo> avaliacoes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvaliacaoResumo {
        private Long avaliacaoId;
        private String nomeAvaliador;
        private Integer nota;
        private String comentario;
        private String dataCriacao;
        private String respostaPrestador;
        private String dataResposta;
    }
}
