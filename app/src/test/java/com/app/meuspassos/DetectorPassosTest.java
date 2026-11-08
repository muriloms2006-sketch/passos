package com.app.meuspassos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class DetectorPassosTest {

    private static final long MS = 1_000_000L;

    private int simularCaminhada(DetectorPassos detector, int passos, long intervaloMs) {
        int detectados = 0;
        long t = 0;
        for (int i = 0; i < passos; i++) {
            for (int j = 0; j < 5; j++) {
                if (detector.processar(0, 0, 14f, t)) detectados++;
                t += 20 * MS;
            }
            for (int j = 0; j < 5; j++) {
                if (detector.processar(0, 0, 7f, t)) detectados++;
                t += 20 * MS;
            }
            t += (intervaloMs - 200) * MS;
        }
        return detectados;
    }

    @Test
    public void celularParadoNaoContaPassos() {
        DetectorPassos detector = new DetectorPassos();
        for (long t = 0; t < 5000 * MS; t += 20 * MS) {
            assertFalse(detector.processar(0, 0, DetectorPassos.GRAVIDADE, t));
        }
    }

    @Test
    public void contaUmPassoPorPisada() {
        assertEquals(10, simularCaminhada(new DetectorPassos(), 10, 500));
    }

    @Test
    public void ignoraPisadasRapidasDemais() {
        DetectorPassos detector = new DetectorPassos();
        int detectados = 0;
        long t = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 3; j++) {
                if (detector.processar(0, 0, 14f, t)) detectados++;
                t += 10 * MS;
            }
            for (int j = 0; j < 3; j++) {
                if (detector.processar(0, 0, 7f, t)) detectados++;
                t += 10 * MS;
            }
        }
        assertEquals(2, detectados);
    }
}
