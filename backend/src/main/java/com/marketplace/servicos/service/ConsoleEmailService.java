package com.marketplace.servicos.service;

import org.springframework.stereotype.Service;

public class ConsoleEmailService implements EmailService {
    @Override
    public void sendVerificationEmail(String to, String token) {
        // Link para o frontend
        String url = "http://localhost:4200/verificar-conta?token=" + token;
        
        System.out.println("==================================================");
        System.out.println("SIMULAÇÃO DE ENVIO DE EMAIL");
        System.out.println("PARA: " + to);
        System.out.println("ASSUNTO: Confirmação de Cadastro");
        System.out.println("MENSAGEM: Olá! Confirme seu cadastro clicando no link abaixo:");
        System.out.println("LINK DE ATIVAÇÃO: " + url);
        System.out.println("==================================================");
    }
}