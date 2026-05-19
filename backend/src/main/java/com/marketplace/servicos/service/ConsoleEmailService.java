package com.marketplace.servicos.service;

public class ConsoleEmailService implements EmailService {

    @Override
    public void sendVerificationEmail(String to, String token) {
        System.out.println("[EMAIL] VERIFICAÇÃO → " + to + " | token=" + token);
    }

    @Override
    public void sendPropostaRecebida(String to, String prestadorNome, String descricaoServico) {
        System.out.println("[EMAIL] PROPOSTA_RECEBIDA → " + to + " | prestador=" + prestadorNome);
    }

    @Override
    public void sendPropostaAceita(String to, String clienteNome, String descricaoServico) {
        System.out.println("[EMAIL] PROPOSTA_ACEITA → " + to + " | cliente=" + clienteNome);
    }

    @Override
    public void sendContratoFinalizado(String to, String descricaoServico) {
        System.out.println("[EMAIL] CONTRATO_FINALIZADO → " + to);
    }

    @Override
    public void sendEmailTeste(String to) {
        System.out.println("[EMAIL] TESTE → " + to);
    }

    @Override
    public void sendNovaSolicitacao(String to, String categoriaNome, String cidade, String estado, String descricao) {
        System.out.println("[EMAIL] NOVA_SOLICITACAO → " + to + " | categoria=" + categoriaNome + " | cidade=" + cidade + "/" + estado);
    }
}
