package com.marketplace.servicos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fotos_prestador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FotoPrestador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private LocalDateTime dataUpload = LocalDateTime.now();
}
