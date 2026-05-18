package com.marketplace.servicos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FotoPrestadorDTO {
    private Long id;
    private String url;
    private LocalDateTime dataUpload;
}
