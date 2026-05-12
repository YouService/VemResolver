package com.marketplace.servicos.service;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
}