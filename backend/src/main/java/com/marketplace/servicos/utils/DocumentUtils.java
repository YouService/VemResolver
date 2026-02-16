package com.marketplace.servicos.utils;

public final class DocumentUtils {

    private DocumentUtils() {
        // Evita instância
    }

    public static String somenteNumeroCpf(String cpf) {
        if (cpf == null) {
            throw new IllegalArgumentException("CPF não pode ser nulo");
        }

        String cleaned = cpf.replaceAll("\\D", "");

        if (cleaned.length() != 11) {
            throw new IllegalArgumentException("CPF inválido");
        }

        return cleaned;
    }
    
}
