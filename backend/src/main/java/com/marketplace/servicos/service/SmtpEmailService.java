package com.marketplace.servicos.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "spring.mail.host")
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@vemresolver.com}")
    private String fromEmail;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        String url = frontendUrl + "/verificar-conta?token=" + token;
        sendHtml(to, "Bem-vindo ao VemResolver — Ative sua conta",
                "<h2>Quase lá!</h2><p>Clique <a href='" + url + "'>aqui</a> para ativar sua conta.</p>");
    }

    @Override
    public void sendPropostaRecebida(String to, String prestadorNome, String descricaoServico) {
        sendHtml(to, "Nova proposta recebida — VemResolver",
                "<h2>Nova proposta!</h2><p><strong>" + prestadorNome + "</strong> enviou uma proposta para: " + descricaoServico + "</p>");
    }

    @Override
    public void sendPropostaAceita(String to, String clienteNome, String descricaoServico) {
        sendHtml(to, "Sua proposta foi aceita — VemResolver",
                "<h2>Proposta aceita!</h2><p><strong>" + clienteNome + "</strong> aceitou sua proposta para: " + descricaoServico + "</p>");
    }

    @Override
    public void sendContratoFinalizado(String to, String descricaoServico) {
        sendHtml(to, "Serviço concluído — VemResolver",
                "<h2>Serviço concluído!</h2><p>O serviço foi marcado como concluído: " + descricaoServico + "</p>");
    }

    @Override
    public void sendEmailTeste(String to) {
        sendHtml(to, "Teste de e-mail — VemResolver",
                "<h2>Funcionando!</h2><p>Configuração SMTP ativa no VemResolver.</p>");
    }

    @Override
    public void sendNovaSolicitacao(String to, String categoriaNome, String cidade, String estado, String descricao) {
        sendHtml(to, "Nova solicitação de serviço — VemResolver",
                "<h2>Nova oportunidade!</h2><p>Categoria: <strong>" + categoriaNome + "</strong> em " + cidade + "/" + estado + "<br>" + descricao + "</p>");
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Falha ao enviar e-mail", e);
        }
    }
}
