package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.PrestadorDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.Categoria;
import com.marketplace.servicos.model.Prestador;
import com.marketplace.servicos.model.TipoUsuario;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.CategoriaRepository;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrestadorService {
    
    private final PrestadorRepository prestadorRepository;
    private final UserRepository userRepository;
    private final CategoriaRepository categoriaRepository;
    
    public PrestadorService(PrestadorRepository prestadorRepository, 
                           UserRepository userRepository,
                           CategoriaRepository categoriaRepository) {
        this.prestadorRepository = prestadorRepository;
        this.userRepository = userRepository;
        this.categoriaRepository = categoriaRepository;
    }
    
    @Transactional
    public PrestadorDTO criarOuAtualizar(PrestadorDTO dto) {
        User usuario = userRepository.findByIdAndTipo(dto.getUsuarioId(), TipoUsuario.PRESTADOR)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário prestador não encontrado"));
        
        Prestador prestador = prestadorRepository.findByUsuarioId(dto.getUsuarioId())
                .orElse(new Prestador());
        
        List<Categoria> categorias = categoriaRepository.findAllById(dto.getCategoriasIds());
        if (categorias.size() != dto.getCategoriasIds().size()) {
            throw new BusinessException("Uma ou mais categorias não foram encontradas");
        }
        
        prestador.setUsuario(usuario);
        prestador.setCategorias(categorias);
        prestador.setDescricaoPerfil(dto.getDescricaoPerfil());
        prestador.setCidade(dto.getCidade());
        prestador.setEstado(dto.getEstado());
        prestador.setLatitude(dto.getLatitude());
        prestador.setLongitude(dto.getLongitude());

        prestador = prestadorRepository.save(prestador);
        return toDTO(prestador);
    }
    
    public PrestadorDTO buscarPorId(Long id) {
        Prestador prestador = prestadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestador não encontrado"));
        return toDTO(prestador);
    }
    
    public PrestadorDTO buscarPorUsuarioId(Long usuarioId) {
        Prestador prestador = prestadorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de prestador não encontrado"));
        return toDTO(prestador);
    }
    
    public Page<PrestadorDTO> buscar(Long categoriaId, String cidade, String estado, Double minAvaliacao, Pageable pageable) {
        Page<Prestador> prestadores;
        
        if (categoriaId != null && cidade != null && estado != null) {
            prestadores = prestadorRepository.findByCategoriaAndLocalizacao(categoriaId, cidade, estado, pageable);
        } else if (categoriaId != null) {
            prestadores = prestadorRepository.findByCategoria(categoriaId, pageable);
        } else if (cidade != null && estado != null && minAvaliacao != null) {
            prestadores = prestadorRepository.findByCidadeAndEstadoAndAvaliacaoMediaGreaterThanEqual(
                    cidade, estado, minAvaliacao, pageable);
        } else {
            prestadores = prestadorRepository.findAll(pageable);
        }
        
        return prestadores.map(this::toDTO);
    }
    
    public Page<PrestadorDTO> buscarProximos(double lat, double lng, double raioKm, Long categoriaId, Pageable pageable) {
        List<PrestadorDTO> resultado = prestadorRepository.findAll().stream()
                .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                .filter(p -> haversine(lat, lng, p.getLatitude(), p.getLongitude()) <= raioKm)
                .filter(p -> categoriaId == null || p.getCategorias().stream().anyMatch(c -> c.getId().equals(categoriaId)))
                .map(this::toDTO)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), resultado.size());
        List<PrestadorDTO> pagina = start > resultado.size() ? List.of() : resultado.subList(start, end);
        return new PageImpl<>(pagina, pageable, resultado.size());
    }

    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private PrestadorDTO toDTO(Prestador prestador) {
        PrestadorDTO dto = new PrestadorDTO();
        dto.setId(prestador.getId());
        dto.setUsuarioId(prestador.getUsuario().getId());
        dto.setCategoriasIds(prestador.getCategorias().stream().map(Categoria::getId).toList());
        dto.setDescricaoPerfil(prestador.getDescricaoPerfil());
        dto.setCidade(prestador.getCidade());
        dto.setEstado(prestador.getEstado());
        dto.setAvaliacaoMedia(prestador.getAvaliacaoMedia());
        dto.setTotalAvaliacoes(prestador.getTotalAvaliacoes());
        dto.setLatitude(prestador.getLatitude());
        dto.setLongitude(prestador.getLongitude());
        return dto;
    }
}
