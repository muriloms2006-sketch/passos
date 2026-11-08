package com.app.meuspassos;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EstimativasTest {

    @Test
    public void distanciaEmKm() {
        assertEquals(3.9, Estimativas.distanciaKm(5000), 0.0001);
        assertEquals(0.0, Estimativas.distanciaKm(0), 0.0001);
    }

    @Test
    public void caloriasArredondadas() {
        assertEquals(200, Estimativas.calorias(5000));
        assertEquals(1, Estimativas.calorias(30));
    }

    @Test
    public void progressoLimitadoEntreZeroEUm() {
        assertEquals(0.5f, Estimativas.progresso(2500, 5000), 0.0001f);
        assertEquals(1f, Estimativas.progresso(8000, 5000), 0.0001f);
        assertEquals(0f, Estimativas.progresso(100, 0), 0.0001f);
    }
}
