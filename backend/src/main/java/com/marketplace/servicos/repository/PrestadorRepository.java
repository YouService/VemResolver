package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.Prestador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrestadorRepository extends JpaRepository<Prestador, Long> {
    Optional<Prestador> findByUsuarioId(Long usuarioId);
    
    @Query("SELECT p FROM Prestador p JOIN p.categorias c WHERE c.id = :categoriaId AND p.cidade = :cidade AND p.estado = :estado")
    Page<Prestador> findByCategoriaAndLocalizacao(
        @Param("categoriaId") Long categoriaId,
        @Param("cidade") String cidade,
        @Param("estado") String estado,
        Pageable pageable
    );
    
    @Query("SELECT p FROM Prestador p JOIN p.categorias c WHERE c.id = :categoriaId")
    Page<Prestador> findByCategoria(@Param("categoriaId") Long categoriaId, Pageable pageable);
    
    @Query("SELECT p FROM Prestador p WHERE p.cidade = :cidade AND p.estado = :estado AND p.avaliacaoMedia >= :minAvaliacao")
    Page<Prestador> findByCidadeAndEstadoAndAvaliacaoMediaGreaterThanEqual(
        String cidade, 
        String estado, 
        Double minAvaliacao, 
        Pageable pageable
    );
}
