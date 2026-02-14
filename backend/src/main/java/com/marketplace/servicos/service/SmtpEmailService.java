package com.marketplace.servicos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Confirmação de Cadastro");
        message.setText("Olá!\n\nConfirme seu cadastro clicando no link abaixo:\n\n" + url + "\n\nEste link expira em 24 horas.");
        
        mailSender.send(message);
    }
}
