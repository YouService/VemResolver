package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.Prestador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("""
        SELECT p FROM Prestador p
        JOIN p.categorias c
        JOIN p.usuario u
        WHERE c.id = :categoriaId
          AND u.ativo = true
        """)
    List<Prestador> findAtivosParaRecomendacao(@Param("categoriaId") Long categoriaId);

    @Query(value = """
        SELECT p.*,
               ts_rank(p.ts_perfil, plainto_tsquery('portuguese', :q)) AS rank
        FROM prestadores p
        JOIN usuarios u ON p.usuario_id = u.id
        WHERE p.ts_perfil @@ plainto_tsquery('portuguese', :q)
          AND u.ativo = true
          AND (:categoriaId IS NULL OR EXISTS (
            SELECT 1 FROM prestador_categorias pc WHERE pc.prestador_id = p.id AND pc.categoria_id = :categoriaId
          ))
          AND (:cidade IS NULL OR p.cidade ILIKE :cidade)
          AND (:estado IS NULL OR p.estado = :estado)
          AND (:minAvaliacao IS NULL OR p.media_avaliacao >= :minAvaliacao)
        ORDER BY rank DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM prestadores p
        JOIN usuarios u ON p.usuario_id = u.id
        WHERE p.ts_perfil @@ plainto_tsquery('portuguese', :q)
          AND u.ativo = true
          AND (:categoriaId IS NULL OR EXISTS (
            SELECT 1 FROM prestador_categorias pc WHERE pc.prestador_id = p.id AND pc.categoria_id = :categoriaId
          ))
          AND (:cidade IS NULL OR p.cidade ILIKE :cidade)
          AND (:estado IS NULL OR p.estado = :estado)
          AND (:minAvaliacao IS NULL OR p.media_avaliacao >= :minAvaliacao)
        """,
        nativeQuery = true)
    Page<Prestador> buscarPorTexto(
        @Param("q") String q,
        @Param("categoriaId") Long categoriaId,
        @Param("cidade") String cidade,
        @Param("estado") String estado,
        @Param("minAvaliacao") Double minAvaliacao,
        Pageable pageable);
}
