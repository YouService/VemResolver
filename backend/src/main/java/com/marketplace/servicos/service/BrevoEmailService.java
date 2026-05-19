package com.marketplace.servicos.service;

import com.marketplace.servicos.model.ConfiguracaoEmail;
import com.marketplace.servicos.repository.ConfiguracaoEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Service
public class BrevoEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailService.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final ConfiguracaoEmailRepository configRepo;
    private final HttpClient httpClient;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public BrevoEmailService(ConfiguracaoEmailRepository configRepo) {
        this.configRepo = configRepo;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        String url = frontendUrl + "/verificar-conta?token=" + token;
        String html = buildHtml(
            "Ative sua conta no VemResolver",
            "Quase lá!",
            "Estamos muito felizes em ter você no <strong>VemResolver</strong>.<br>" +
            "Clique no botão abaixo para confirmar seu cadastro e ativar sua conta.",
            url,
            "ATIVAR MINHA CONTA",
            "Este link expira em 24 horas. Se você não realizou este cadastro, pode ignorar este e-mail."
        );
        enviar(to, "Bem-vindo ao VemResolver — Ative sua conta", html);
    }

    @Override
    public void sendPropostaRecebida(String to, String prestadorNome, String descricaoServico) {
        String url = frontendUrl + "/cliente/dashboard";
        String descricao = descricaoServico.length() > 80
                ? descricaoServico.substring(0, 80) + "..." : descricaoServico;
        String html = buildHtml(
            "Nova proposta recebida",
            "Você recebeu uma nova proposta!",
            "<strong>" + escapeHtml(prestadorNome) + "</strong> enviou uma proposta para sua solicitação:<br>" +
            "<em>\"" + escapeHtml(descricao) + "\"</em><br><br>" +
            "Acesse o painel para visualizar os detalhes e aceitar ou recusar.",
            url,
            "VER PROPOSTA",
            null
        );
        enviar(to, "Nova proposta recebida — VemResolver", html);
    }

    @Override
    public void sendPropostaAceita(String to, String clienteNome, String descricaoServico) {
        String url = frontendUrl + "/prestador/dashboard";
        String descricao = descricaoServico.length() > 80
                ? descricaoServico.substring(0, 80) + "..." : descricaoServico;
        String html = buildHtml(
            "Sua proposta foi aceita!",
            "Parabéns! Proposta aceita.",
            "<strong>" + escapeHtml(clienteNome) + "</strong> aceitou sua proposta para o serviço:<br>" +
            "<em>\"" + escapeHtml(descricao) + "\"</em><br><br>" +
            "Entre em contato com o cliente pelo chat para combinar os detalhes.",
            url,
            "ACESSAR PAINEL",
            null
        );
        enviar(to, "Sua proposta foi aceita — VemResolver", html);
    }

    @Override
    public void sendContratoFinalizado(String to, String descricaoServico) {
        String url = frontendUrl + "/prestador/dashboard";
        String descricao = descricaoServico.length() > 80
                ? descricaoServico.substring(0, 80) + "..." : descricaoServico;
        String html = buildHtml(
            "Serviço concluído",
            "Serviço marcado como concluído!",
            "O cliente marcou como concluído o serviço:<br>" +
            "<em>\"" + escapeHtml(descricao) + "\"</em><br><br>" +
            "Acesse seu painel para verificar avaliações e novos contratos.",
            url,
            "VER MEU PAINEL",
            null
        );
        enviar(to, "Serviço concluído — VemResolver", html);
    }

    @Override
    public void sendNovaSolicitacao(String to, String categoriaNome, String cidade, String estado, String descricao) {
        Optional<ConfiguracaoEmail> configOpt = configRepo.findFirstByOrderByIdAsc();
        if (configOpt.isEmpty() || !Boolean.TRUE.equals(configOpt.get().getEmailNovaSolicitacaoAtivo())) return;

        String url = frontendUrl + "/prestador/dashboard";
        String desc = descricao != null && descricao.length() > 80 ? descricao.substring(0, 80) + "..." : descricao;
        String html = buildHtml(
            "Nova solicitação de serviço",
            "Nova solicitação disponível!",
            "Uma nova solicitação de <strong>" + escapeHtml(categoriaNome) + "</strong> foi publicada em " +
            "<strong>" + escapeHtml(cidade) + "/" + escapeHtml(estado) + "</strong>:<br>" +
            "<em>\"" + escapeHtml(desc) + "\"</em><br><br>" +
            "Acesse o painel para enviar uma proposta.",
            url,
            "VER OPORTUNIDADES",
            null
        );
        enviar(to, "Nova solicitação de serviço — VemResolver", html);
    }

    @Override
    public void sendEmailTeste(String to) {
        String html = buildHtml(
            "E-mail de teste",
            "Configuração de e-mail funcionando!",
            "Este é um e-mail de teste enviado pelo painel administrativo do <strong>VemResolver</strong>.<br>" +
            "Se você recebeu esta mensagem, a integração com o Brevo está configurada corretamente.",
            frontendUrl,
            "ACESSAR VEMRESOLVER",
            null
        );
        enviar(to, "Teste de configuração de e-mail — VemResolver", html);
    }

    private void enviar(String to, String subject, String html) {
        Optional<ConfiguracaoEmail> configOpt = configRepo.findFirstByOrderByIdAsc();
        if (configOpt.isEmpty()) {
            log.warn("Configuração de e-mail não encontrada — e-mail não enviado para {}", to);
            return;
        }
        ConfiguracaoEmail config = configOpt.get();
        if (!Boolean.TRUE.equals(config.getAtivo()) || config.getBrevoApiKey() == null || config.getBrevoApiKey().isBlank()) {
            log.info("Envio de e-mail desativado ou sem API key — e-mail não enviado para {}", to);
            return;
        }

        String senderEmail = config.getEmailRemetente() != null ? config.getEmailRemetente() : "noreply@vemresolver.com";
        String senderName  = config.getNomeRemetente()  != null ? config.getNomeRemetente()  : "VemResolver";

        String body = """
                {
                  "sender":      {"name": "%s", "email": "%s"},
                  "to":          [{"email": "%s"}],
                  "subject":     "%s",
                  "htmlContent": "%s"
                }
                """.formatted(
                escapeJson(senderName),
                escapeJson(senderEmail),
                escapeJson(to),
                escapeJson(subject),
                escapeJson(html)
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_API_URL))
                    .header("accept", "application/json")
                    .header("api-key", config.getBrevoApiKey())
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("E-mail enviado via Brevo para {} — status {}", to, response.statusCode());
            } else {
                log.error("Falha ao enviar e-mail via Brevo para {} — status {} body: {}", to, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Erro ao chamar API Brevo para {}: {}", to, e.getMessage(), e);
        }
    }

    private String buildHtml(String title, String heading, String body, String ctaUrl, String ctaLabel, String footer) {
        String footerHtml = footer != null
                ? "<p style='color:#888;font-size:13px;text-align:center;'>" + footer + "</p>"
                : "";
        return "<html><body style='font-family:Arial,sans-serif;background:#f9f9f9;padding:20px;'>" +
               "<div style='max-width:600px;margin:auto;background:#fff;padding:30px;border-radius:12px;border:1px solid #eee;box-shadow:0 4px 10px rgba(0,0,0,0.05);'>" +
               "<h2 style='color:#2c3e50;text-align:center;font-size:22px;'>" + heading + "</h2>" +
               "<p style='color:#555;font-size:15px;line-height:1.6;text-align:center;'>" + body + "</p>" +
               "<div style='text-align:center;margin:30px 0;'>" +
               "<a href='" + ctaUrl + "' style='background:#007bff;color:#fff;padding:14px 28px;text-decoration:none;border-radius:8px;font-weight:bold;font-size:15px;display:inline-block;'>" + ctaLabel + "</a>" +
               "</div>" +
               footerHtml +
               "<hr style='border:0;border-top:1px solid #f0f0f0;margin:25px 0;'>" +
               "<p style='color:#aaa;font-size:12px;text-align:center;'>&copy; 2026 VemResolver — Todos os direitos reservados.</p>" +
               "</div></body></html>";
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
