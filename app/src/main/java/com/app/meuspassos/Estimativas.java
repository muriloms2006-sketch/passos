package com.app.meuspassos;

/**
 * Estimativas a partir da quantidade de passos, usando médias de um adulto.
 */
public final class Estimativas {

    static final double METROS_POR_PASSO = 0.78;
    static final double KCAL_POR_PASSO = 0.04;

    private Estimativas() {
    }

    public static double distanciaKm(int passos) {
        return passos * METROS_POR_PASSO / 1000.0;
    }

    public static int calorias(int passos) {
        return (int) Math.round(passos * KCAL_POR_PASSO);
    }

    public static float progresso(int passos, int meta) {
        if (meta <= 0) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, passos / (float) meta));
    }
}
