package com.app.meuspassos;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ContadorDiarioTest {

    private static final String HOJE = "2026-11-08";
    private static final String AMANHA = "2026-11-09";

    @Test
    public void primeiraLeituraViraBase() {
        ContadorDiario contador = new ContadorDiario();

        assertEquals(0, contador.registrarTotal(12000, HOJE));
        assertEquals(150, contador.registrarTotal(12150, HOJE));
    }

    @Test
    public void continuaDepoisDeRecriarComEstadoSalvo() {
        ContadorDiario contador = new ContadorDiario();
        contador.registrarTotal(1000, HOJE);
        contador.registrarTotal(1300, HOJE);

        ContadorDiario recriado = new ContadorDiario(
                contador.getDia(), contador.getBase(), contador.getUltimoTotal());

        assertEquals(300, recriado.getPassos(HOJE));
        assertEquals(350, recriado.registrarTotal(1350, HOJE));
    }

    @Test
    public void zeraQuandoViraODia() {
        ContadorDiario contador = new ContadorDiario();
        contador.registrarTotal(1000, HOJE);
        contador.registrarTotal(1800, HOJE);

        assertEquals(0, contador.getPassos(AMANHA));
        assertEquals(0, contador.registrarTotal(1900, AMANHA));
        assertEquals(50, contador.registrarTotal(1950, AMANHA));
    }

    @Test
    public void mantemPassosQuandoOCelularReinicia() {
        ContadorDiario contador = new ContadorDiario();
        contador.registrarTotal(1000, HOJE);
        contador.registrarTotal(1500, HOJE);

        assertEquals(520, contador.registrarTotal(20, HOJE));
        assertEquals(530, contador.registrarTotal(30, HOJE));
    }

    @Test
    public void zerarRecomecaDoUltimoTotal() {
        ContadorDiario contador = new ContadorDiario();
        contador.registrarTotal(1000, HOJE);
        contador.registrarTotal(1400, HOJE);

        contador.zerar(HOJE);

        assertEquals(0, contador.getPassos(HOJE));
        assertEquals(1, contador.registrarTotal(1401, HOJE));
    }

    @Test
    public void somarPassoContaDesdeOPrimeiro() {
        ContadorDiario contador = new ContadorDiario();

        assertEquals(1, contador.somarPasso(HOJE));
        assertEquals(2, contador.somarPasso(HOJE));
        assertEquals(1, contador.somarPasso(AMANHA));
    }
}
