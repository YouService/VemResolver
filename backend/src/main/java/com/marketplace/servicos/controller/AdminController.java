package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.SolicitacaoAdminDTO;
import com.marketplace.servicos.dto.UserResumoDTO;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.SolicitacaoServico;
import com.marketplace.servicos.model.StatusSolicitacao;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.PropostaRepository;
import com.marketplace.servicos.repository.SolicitacaoServicoRepository;
import com.marketplace.servicos.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Admin", description = "Endpoints administrativos (apenas ADMIN)")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private static final int MINUTOS_ONLINE = 5;

    private final UserRepository userRepository;
    private final PrestadorRepository prestadorRepository;
    private final SolicitacaoServicoRepository solicitacaoServicoRepository;
    private final PropostaRepository propostaRepository;

    public AdminController(UserRepository userRepository,
                           PrestadorRepository prestadorRepository,
                           SolicitacaoServicoRepository solicitacaoServicoRepository,
                           PropostaRepository propostaRepository) {
        this.userRepository = userRepository;
        this.prestadorRepository = prestadorRepository;
        this.solicitacaoServicoRepository = solicitacaoServicoRepository;
        this.propostaRepository = propostaRepository;
    }

    @GetMapping("/stats")
    @Operation(summary = "Retorna totais de usuarios, prestadores, solicitacoes, propostas e usuarios online")
    public ResponseEntity<Map<String, Long>> stats() {
        LocalDateTime limiar = LocalDateTime.now().minusMinutes(MINUTOS_ONLINE);
        return ResponseEntity.ok(Map.of(
            "totalUsuarios", userRepository.count(),
            "totalPrestadores", prestadorRepository.count(),
            "totalSolicitacoes", solicitacaoServicoRepository.count(),
            "totalPropostas", propostaRepository.count(),
            "usuariosOnline", userRepository.countByUltimoAcessoAfterAndAtivoTrue(limiar)
        ));
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Listar todos os usuarios com paginacao")
    public ResponseEntity<Page<UserResumoDTO>> listarUsuarios(Pageable pageable) {
        LocalDateTime limiar = LocalDateTime.now().minusMinutes(MINUTOS_ONLINE);
        return ResponseEntity.ok(userRepository.findAll(pageable).map(u -> toResumoDTO(u, limiar)));
    }

    @GetMapping("/usuarios/{id}")
    @Operation(summary = "Buscar usuario por ID")
    public ResponseEntity<UserResumoDTO> buscarUsuario(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        LocalDateTime limiar = LocalDateTime.now().minusMinutes(MINUTOS_ONLINE);
        return ResponseEntity.ok(toResumoDTO(user, limiar));
    }

    @PatchMapping("/usuarios/{id}/ativar")
    @Transactional
    @Operation(summary = "Ativar ou desativar um usuario")
    public ResponseEntity<Void> ativarUsuario(@PathVariable Long id, @RequestParam boolean ativo) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        user.setAtivo(ativo);
        userRepository.save(user);
        log.info("Usuario id={} ativo={} atualizado pelo admin", id, ativo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuarios/online")
    @Operation(summary = "Listar usuarios ativos nos ultimos 5 minutos")
    public ResponseEntity<List<UserResumoDTO>> listarOnline() {
        LocalDateTime limiar = LocalDateTime.now().minusMinutes(MINUTOS_ONLINE);
        List<UserResumoDTO> online = userRepository.findOnline(limiar)
                .stream().map(u -> toResumoDTO(u, limiar)).toList();
        return ResponseEntity.ok(online);
    }

    @GetMapping("/solicitacoes")
    @Operation(summary = "Listar todas as solicitacoes (com filtro opcional por status)")
    public ResponseEntity<Page<SolicitacaoAdminDTO>> listarSolicitacoes(
            @RequestParam(required = false) StatusSolicitacao status,
            Pageable pageable) {
        Page<SolicitacaoServico> page = (status != null)
                ? solicitacaoServicoRepository.findByStatus(status, pageable)
                : solicitacaoServicoRepository.findAll(pageable);
        return ResponseEntity.ok(page.map(this::toAdminDTO));
    }

    @PatchMapping("/solicitacoes/{id}/status")
    @Transactional
    @Operation(summary = "Alterar status de uma solicitacao (bypass de ownership)")
    public ResponseEntity<Void> alterarStatusSolicitacao(
            @PathVariable Long id, @RequestParam StatusSolicitacao status) {
        SolicitacaoServico sol = solicitacaoServicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
        StatusSolicitacao statusAnterior = sol.getStatus();
        sol.setStatus(status);
        solicitacaoServicoRepository.save(sol);
        log.info("Admin alterou status da solicitacao id={} de {} para {}", id, statusAnterior, status);
        return ResponseEntity.noContent().build();
    }

    private UserResumoDTO toResumoDTO(User user, LocalDateTime limiar) {
        UserResumoDTO dto = new UserResumoDTO();
        dto.setId(user.getId());
        dto.setNome(user.getNome());
        dto.setEmail(user.getEmail());
        dto.setTipo(user.getTipo());
        dto.setAtivo(user.getAtivo());
        dto.setDataCadastro(user.getDataCadastro());
        dto.setAvaliacaoMedia(user.getAvaliacaoMedia());
        dto.setTotalAvaliacoes(user.getTotalAvaliacoes());
        dto.setUltimoAcesso(user.getUltimoAcesso());
        dto.setOnline(user.getUltimoAcesso() != null && user.getUltimoAcesso().isAfter(limiar));
        return dto;
    }

    private SolicitacaoAdminDTO toAdminDTO(SolicitacaoServico sol) {
        SolicitacaoAdminDTO dto = new SolicitacaoAdminDTO();
        dto.setId(sol.getId());
        dto.setClienteNome(sol.getCliente().getNome());
        dto.setClienteEmail(sol.getCliente().getEmail());
        dto.setCategoriaNome(sol.getCategoria().getNome());
        dto.setDescricao(sol.getDescricao());
        dto.setCidade(sol.getCidade());
        dto.setEstado(sol.getEstado());
        dto.setStatus(sol.getStatus());
        dto.setDataCriacao(sol.getDataCriacao());
        dto.setTotalPropostas(propostaRepository.countBySolicitacaoId(sol.getId()));
        return dto;
    }
}
