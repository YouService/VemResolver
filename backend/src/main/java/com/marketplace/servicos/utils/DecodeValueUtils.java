package com.marketplace.servicos.utils;


public class DecodeValueUtils {

    public static String decodeBase64(String encodedValue) {
        try {
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(encodedValue);
            return new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Valor codificado em Base64 inválido: " + encodedValue, e);
        }
    }

}
