package com.marketplace.servicos.service;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
    void sendPropostaRecebida(String to, String prestadorNome, String descricaoServico);
    void sendPropostaAceita(String to, String clienteNome, String descricaoServico);
    void sendContratoFinalizado(String to, String descricaoServico);
    void sendEmailTeste(String to);
}
