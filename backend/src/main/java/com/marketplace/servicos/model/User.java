package com.marketplace.servicos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nome;
    
    @Column(nullable = false, unique = true)
    private String login;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String senha;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipo;
    
    private String telefone;
    
    private String cpf;
    
    private String rg;
    
    private String dataExpedicao;
    
    @Column(nullable = false)
    private LocalDateTime dataCadastro;
    
    @Column(nullable = false)
    private Boolean ativo = true;
    
    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
        if (ativo == null) {
            ativo = true;
        }
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + tipo.name()));
    }
    
    @Override
    public String getPassword() {
        return senha;
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
