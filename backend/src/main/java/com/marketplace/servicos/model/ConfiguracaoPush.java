package com.marketplace.servicos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracoes_push")
public class ConfiguracaoPush {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vapid_public_key", columnDefinition = "TEXT")
    private String vapidPublicKey;

    @Column(name = "vapid_private_key", columnDefinition = "TEXT")
    private String vapidPrivateKey;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVapidPublicKey() { return vapidPublicKey; }
    public void setVapidPublicKey(String vapidPublicKey) { this.vapidPublicKey = vapidPublicKey; }

    public String getVapidPrivateKey() { return vapidPrivateKey; }
    public void setVapidPrivateKey(String vapidPrivateKey) { this.vapidPrivateKey = vapidPrivateKey; }

    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
