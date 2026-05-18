package com.marketplace.servicos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompletudePrestadorDTO {

    private int percentual;
    private List<ItemChecklist> itens;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemChecklist {
        private String id;
        private String descricao;
        private boolean concluido;
        private int peso;
        private String linkDestino;
    }
}
