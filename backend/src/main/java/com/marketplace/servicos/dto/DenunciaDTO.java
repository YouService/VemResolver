package com.marketplace.servicos.dto;

import com.marketplace.servicos.model.StatusDenuncia;
import com.marketplace.servicos.model.TipoDenuncia;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DenunciaDTO {
    private Long id;
    private Long denuncianteId;
    private String denuncianteNome;
    private Long denunciadoId;
    private String denunciadoNome;
    private TipoDenuncia tipo;
    private String descricao;
    private StatusDenuncia status;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataRevisao;
    private String observacaoAdmin;
}
