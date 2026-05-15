package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.UserResumoDTO;
import com.marketplace.servicos.exception.ResourceNotFoundException;
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

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Admin", description = "Endpoints administrativos (apenas ADMIN)")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

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
    @Operation(summary = "Retorna totais de usuarios, prestadores, solicitacoes e propostas")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(Map.of(
            "totalUsuarios", userRepository.count(),
            "totalPrestadores", prestadorRepository.count(),
            "totalSolicitacoes", solicitacaoServicoRepository.count(),
            "totalPropostas", propostaRepository.count()
        ));
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Listar todos os usuarios com paginacao")
    public ResponseEntity<Page<UserResumoDTO>> listarUsuarios(Pageable pageable) {
        return ResponseEntity.ok(userRepository.findAll(pageable).map(this::toResumoDTO));
    }

    @GetMapping("/usuarios/{id}")
    @Operation(summary = "Buscar usuario por ID")
    public ResponseEntity<UserResumoDTO> buscarUsuario(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return ResponseEntity.ok(toResumoDTO(user));
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

    private UserResumoDTO toResumoDTO(User user) {
        UserResumoDTO dto = new UserResumoDTO();
        dto.setId(user.getId());
        dto.setNome(user.getNome());
        dto.setEmail(user.getEmail());
        dto.setTipo(user.getTipo());
        dto.setAtivo(user.getAtivo());
        dto.setDataCadastro(user.getDataCadastro());
        dto.setAvaliacaoMedia(user.getAvaliacaoMedia());
        dto.setTotalAvaliacoes(user.getTotalAvaliacoes());
        return dto;
    }
}
