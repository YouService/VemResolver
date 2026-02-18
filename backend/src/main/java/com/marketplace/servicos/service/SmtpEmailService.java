package com.marketplace.servicos.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        String url = frontendUrl + "/verificar-conta?token=" + token;
        String logoUrl = frontendUrl + "/assets/logo.png"; 

        try {
            MimeMessage message = mailSender.createMimeMessage();
            // O parâmetro 'true' indica que o e-mail terá múltiplas partes (HTML)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Bem-vindo ao VemResolver - Ative sua conta");

            // Template HTML elegante
            String htmlContent = 
                "<html>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;'>" +
                    "<div style='max-width: 600px; margin: auto; background: #ffffff; padding: 30px; border-radius: 12px; border: 1px solid #eeeeee; box-shadow: 0 4px 10px rgba(0,0,0,0.05);'>" +
                        "<div style='text-align: center; margin-bottom: 25px;'>" +
                            "<img src='" + logoUrl + "' alt='VemResolver' style='width: 200px; height: auto;'>" +
                        "</div>" +
                        "<h2 style='color: #2c3e50; text-align: center; font-size: 24px;'>Quase lá!</h2>" +
                        "<p style='color: #555555; font-size: 16px; line-height: 1.6; text-align: center;'>" +
                            "Estamos muito felizes em ter você no <strong>VemResolver</strong>.<br>" +
                            "Clique no botão abaixo para confirmar seu cadastro e ativar sua conta." +
                        "</p>" +
                        "<div style='text-align: center; margin: 35px 0;'>" +
                            "<a href='" + url + "' style='background-color: #007bff; color: #ffffff; padding: 16px 32px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px; display: inline-block; box-shadow: 0 4px 6px rgba(0,123,255,0.2);'>ATIVAR MINHA CONTA</a>" +
                        "</div>" +
                        "<p style='color: #888888; font-size: 13px; text-align: center;'>" +
                            "Este link expira em 24 horas. Se você não realizou este cadastro, pode ignorar este e-mail." +
                        "</p>" +
                        "<hr style='border: 0; border-top: 1px solid #f0f0f0; margin: 30px 0;'>" +
                        "<p style='color: #aaaaaa; font-size: 12px; text-align: center;'>" +
                            "&copy; 2026 VemResolver - Todos os direitos reservados." +
                        "</p>" +
                    "</div>" +
                "</body>" +
                "</html>";

            helper.setText(htmlContent, true); // 'true' para renderizar como HTML
            
            mailSender.send(message);

        } catch (MessagingException e) {
            // Log do erro para depuração
            System.err.println("Erro ao formatar e-mail HTML: " + e.getMessage());
            // Fallback: Se o HTML falhar, poderíamos enviar texto simples, mas aqui vamos deixar estourar a exception
            throw new RuntimeException("Falha ao enviar e-mail de confirmação", e);
        }
    }
}