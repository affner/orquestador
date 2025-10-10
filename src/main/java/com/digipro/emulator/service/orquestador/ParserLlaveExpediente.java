package com.digipro.emulator.service.orquestador;

import java.util.Objects;

/** Parsea llaves MMYYYYNNCONTRATO y MYYYYNNCONTRATO (mes 1 dígito). */
public final class ParserLlaveExpediente {

    public static final class Llave {
        public final int mes, anio;
        public final String negocio, contrato;
        public Llave(int mes, int anio, String negocio, String contrato) {
            this.mes = mes; this.anio = anio; this.negocio = negocio; this.contrato = contrato;
        }
        public int yyyyMM() { return anio * 100 + mes; }
    }

    private ParserLlaveExpediente() {}

    public static Llave parseFlexible(String llave) {
        Objects.requireNonNull(llave, "llave requerida");
        String digits = llave.replaceAll("\\D", "");
        if (digits.length() < 8) throw new IllegalArgumentException("Llave inválida o muy corta: " + llave);

        // ---- Intento A: mes con 2 dígitos (MMYYYYNNCONTRATO) ----
        // Solo es válido si MES ∈ [1..12] **y** AÑO ∈ [1900..2100].
        if (digits.length() >= 8) {
            try {
                int mes2  = Integer.parseInt(digits.substring(0, 2));
                int anio4 = Integer.parseInt(digits.substring(2, 6));
                if (isValidMonth(mes2) && isValidYear(anio4)) {
                    String negocio = sub(digits, 6, 8);
                    String contrato = sub(digits, 8, digits.length());
                    return new Llave(mes2, anio4, negocio, contrato);
                }
            } catch (Exception ignored) {}
        }

        // ---- Intento B: mes con 1 dígito (MYYYYNNCONTRATO) ----
        // Ej.: 2202501456 -> mes=2, anio=2025, negocio=01, contrato=456
        try {
            int mes1  = Integer.parseInt(digits.substring(0, 1));
            int anio4 = Integer.parseInt(digits.substring(1, 5));
            if (!isValidMonth(mes1) || !isValidYear(anio4)) {
                throw new IllegalArgumentException("Mes/Año fuera de rango en llave: " + llave);
            }
            String negocio = sub(digits, 5, 7);
            String contrato = sub(digits, 7, digits.length());
            return new Llave(mes1, anio4, negocio, contrato);
        } catch (Exception ex) {
            throw new IllegalArgumentException("No se pudo parsear la llave: " + llave, ex);
        }
    }

    private static boolean isValidMonth(int m) { return m >= 1 && m <= 12; }
    private static boolean isValidYear(int y) { return y >= 1900 && y <= 2100; }

    private static String sub(String s, int a, int b) {
        int end = Math.min(s.length(), b);
        return (a >= end) ? "" : s.substring(a, end);
    }
}
