package com.marketplace.servicos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prestadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prestador {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private User usuario;
    
    @ManyToMany
    @JoinTable(
        name = "prestador_categorias",
        joinColumns = @JoinColumn(name = "prestador_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private List<Categoria> categorias = new ArrayList<>();
    
    @Column(columnDefinition = "TEXT")
    private String descricaoPerfil;
    
    private String cidade;
    
    private String estado;
    
    @Column(nullable = false)
    private Double avaliacaoMedia = 0.0;

    @Column(nullable = false)
    private Integer totalAvaliacoes = 0;

    private Double latitude;

    private Double longitude;

    @Column(name = "foto_perfil_url", columnDefinition = "TEXT")
    private String fotoPerfilUrl;

    @Column(name = "percentual_completude", nullable = false)
    private Integer percentualCompletude = 0;
}
