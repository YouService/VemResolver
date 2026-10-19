package com.marketplace.servicos.controller;

import com.marketplace.servicos.dto.CompraCreditoDTO;
import com.marketplace.servicos.dto.ComprarCreditosDTO;
import com.marketplace.servicos.dto.PacoteCreditoDTO;
import com.marketplace.servicos.dto.ResumoCreditosDTO;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.service.AdminAuditService;
import com.marketplace.servicos.service.CreditoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Créditos", description = "Créditos pré-pagos e painel financeiro do prestador")
public class CreditoController {

    private final CreditoService creditoService;
    private final AdminAuditService adminAuditService;

    public CreditoController(CreditoService creditoService, AdminAuditService adminAuditService) {
        this.creditoService = creditoService;
        this.adminAuditService = adminAuditService;
    }

    // ── Prestador ────────────────────────────────────────────────────────────

    @GetMapping("/api/prestadores/me/creditos")
    @PreAuthorize("hasRole('PRESTADOR')")
    @Operation(summary = "Saldo de créditos e histórico paginado de transações do prestador logado")
    public ResponseEntity<ResumoCreditosDTO> resumo(@AuthenticationPrincipal User user,
                                                    @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(creditoService.resumo(user.getId(), pageable));
    }

    @GetMapping("/api/prestadores/me/creditos/pacotes")
    @PreAuthorize("hasRole('PRESTADOR')")
    @Operation(summary = "Pacotes de créditos disponíveis para compra")
    public ResponseEntity<List<PacoteCreditoDTO>> pacotesDisponiveis() {
        return ResponseEntity.ok(creditoService.listarPacotesAtivos());
    }

    @PostMapping("/api/prestadores/me/creditos/comprar")
    @PreAuthorize("hasRole('PRESTADOR')")
    @Operation(summary = "Comprar um pacote de créditos")
    public ResponseEntity<CompraCreditoDTO> comprar(@AuthenticationPrincipal User user,
                                                    @RequestBody @Valid ComprarCreditosDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(creditoService.comprar(user.getId(), dto.getPacoteId()));
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @GetMapping("/api/admin/pacotes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os pacotes de créditos (ativos e inativos)")
    public ResponseEntity<List<PacoteCreditoDTO>> listarPacotes() {
        return ResponseEntity.ok(creditoService.listarTodosPacotes());
    }

    @PostMapping("/api/admin/pacotes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar pacote de créditos")
    public ResponseEntity<PacoteCreditoDTO> criarPacote(@RequestBody @Valid PacoteCreditoDTO dto,
                                                        @AuthenticationPrincipal User admin,
                                                        HttpServletRequest request) {
        PacoteCreditoDTO salvo = creditoService.salvarPacote(null, dto);
        adminAuditService.registrar(admin, "CRIAR_PACOTE_CREDITO", "PACOTE_CREDITO", salvo.getId(),
                null, descrever(salvo), resolveIp(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/api/admin/pacotes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar pacote de créditos (inclusive ativar/desativar)")
    public ResponseEntity<PacoteCreditoDTO> atualizarPacote(@PathVariable Long id,
                                                            @RequestBody @Valid PacoteCreditoDTO dto,
                                                            @AuthenticationPrincipal User admin,
                                                            HttpServletRequest request) {
        PacoteCreditoDTO salvo = creditoService.salvarPacote(id, dto);
        adminAuditService.registrar(admin, "ALTERAR_PACOTE_CREDITO", "PACOTE_CREDITO", id,
                null, descrever(salvo), resolveIp(request));
        return ResponseEntity.ok(salvo);
    }

    private String descrever(PacoteCreditoDTO p) {
        return p.getNome() + " | " + p.getQuantidadeCreditos() + " créditos | " + p.getPrecoCentavos()
                + " centavos | ativo=" + p.getAtivo();
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
