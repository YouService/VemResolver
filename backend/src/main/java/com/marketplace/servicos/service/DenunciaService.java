package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.AtualizarDenunciaDTO;
import com.marketplace.servicos.dto.CriarDenunciaDTO;
import com.marketplace.servicos.dto.DenunciaDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.DenunciaRepository;
import com.marketplace.servicos.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DenunciaService {

    // Número de denúncias aprovadas necessárias para suspensão automática
    static final int LIMITE_SUSPENSAO = 3;

    private final DenunciaRepository denunciaRepository;
    private final UserRepository userRepository;

    public DenunciaService(DenunciaRepository denunciaRepository, UserRepository userRepository) {
        this.denunciaRepository = denunciaRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DenunciaDTO criar(CriarDenunciaDTO dto, Long denuncianteId) {
        if (denuncianteId.equals(dto.getDenunciadoId())) {
            throw new BusinessException("Você não pode denunciar a si mesmo");
        }

        User denunciante = userRepository.findById(denuncianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário denunciante não encontrado"));

        User denunciado = userRepository.findById(dto.getDenunciadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário denunciado não encontrado"));

        if (!denunciado.getAtivo()) {
            throw new BusinessException("Este usuário já está suspenso");
        }

        if (denunciaRepository.existsByDenuncianteIdAndDenunciadoIdAndTipo(
                denuncianteId, dto.getDenunciadoId(), dto.getTipo())) {
            throw new BusinessException("Você já denunciou este usuário por este motivo");
        }

        Denuncia denuncia = new Denuncia();
        denuncia.setDenunciante(denunciante);
        denuncia.setDenunciado(denunciado);
        denuncia.setTipo(dto.getTipo());
        denuncia.setDescricao(dto.getDescricao());

        return toDTO(denunciaRepository.save(denuncia));
    }

    @Transactional(readOnly = true)
    public Page<DenunciaDTO> listar(StatusDenuncia status, Pageable pageable) {
        return denunciaRepository.filtrar(status, pageable).map(this::toDTO);
    }

    @Transactional
    public DenunciaDTO atualizarStatus(Long id, AtualizarDenunciaDTO dto) {
        Denuncia denuncia = denunciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Denúncia não encontrada"));

        if (denuncia.getStatus() != StatusDenuncia.PENDENTE) {
            throw new BusinessException("Somente denúncias PENDENTES podem ser revisadas");
        }

        denuncia.setStatus(dto.getStatus());
        denuncia.setDataRevisao(LocalDateTime.now().withNano(0));
        denuncia.setObservacaoAdmin(dto.getObservacaoAdmin());
        denunciaRepository.save(denuncia);

        if (dto.getStatus() == StatusDenuncia.APROVADA) {
            verificarSuspensaoAutomatica(denuncia.getDenunciado());
        }

        return toDTO(denuncia);
    }

    @Transactional(readOnly = true)
    public long contarPendentes() {
        return denunciaRepository.countByStatus(StatusDenuncia.PENDENTE);
    }

    private void verificarSuspensaoAutomatica(User denunciado) {
        long aprovadas = denunciaRepository.countByDenunciadoIdAndStatus(
                denunciado.getId(), StatusDenuncia.APROVADA);
        if (aprovadas >= LIMITE_SUSPENSAO && denunciado.getAtivo()) {
            denunciado.setAtivo(false);
            userRepository.save(denunciado);
        }
    }

    private DenunciaDTO toDTO(Denuncia d) {
        DenunciaDTO dto = new DenunciaDTO();
        dto.setId(d.getId());
        dto.setDenuncianteId(d.getDenunciante().getId());
        dto.setDenuncianteNome(d.getDenunciante().getNome());
        dto.setDenunciadoId(d.getDenunciado().getId());
        dto.setDenunciadoNome(d.getDenunciado().getNome());
        dto.setTipo(d.getTipo());
        dto.setDescricao(d.getDescricao());
        dto.setStatus(d.getStatus());
        dto.setDataCriacao(d.getDataCriacao());
        dto.setDataRevisao(d.getDataRevisao());
        dto.setObservacaoAdmin(d.getObservacaoAdmin());
        return dto;
    }
}
