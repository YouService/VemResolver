package com.marketplace.servicos.dto;

import com.marketplace.servicos.model.AdminAuditLog;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminAuditLogDTO {

    private Long id;
    private Long adminId;
    private String adminNome;
    private String acao;
    private String recursoTipo;
    private Long recursoId;
    private String dadosAntes;
    private String dadosDepois;
    private String ipAddress;
    private String status;
    private LocalDateTime criadoEm;

    public static AdminAuditLogDTO from(AdminAuditLog log) {
        AdminAuditLogDTO dto = new AdminAuditLogDTO();
        dto.setId(log.getId());
        dto.setAdminId(log.getAdmin().getId());
        dto.setAdminNome(log.getAdmin().getNome());
        dto.setAcao(log.getAcao());
        dto.setRecursoTipo(log.getRecursoTipo());
        dto.setRecursoId(log.getRecursoId());
        dto.setDadosAntes(log.getDadosAntes());
        dto.setDadosDepois(log.getDadosDepois());
        dto.setIpAddress(log.getIpAddress());
        dto.setStatus(log.getStatus());
        dto.setCriadoEm(log.getCriadoEm());
        return dto;
    }
}
