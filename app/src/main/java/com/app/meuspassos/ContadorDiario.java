package com.app.meuspassos;

/**
 * Converte o total acumulado do sensor nos passos do dia.
 *
 * O TYPE_STEP_COUNTER conta desde que o celular ligou, então guardamos uma
 * "base" e os passos do dia são total - base. A base muda quando vira o dia,
 * quando o usuário zera o contador ou quando o celular reinicia.
 */
public class ContadorDiario {

    private String dia;
    private int base;
    private int ultimoTotal;

    public ContadorDiario() {
        this(null, 0, -1);
    }

    public ContadorDiario(String dia, int base, int ultimoTotal) {
        this.dia = dia;
        this.base = base;
        this.ultimoTotal = ultimoTotal;
    }

    public int registrarTotal(int total, String hoje) {
        if (ultimoTotal >= 0 && total < ultimoTotal) {
            // O celular reiniciou e o sensor voltou pro zero: mantém o que já tinha sido contado
            base -= ultimoTotal;
        }

        if (ultimoTotal < 0 || !hoje.equals(dia)) {
            dia = hoje;
            base = total;
        }

        ultimoTotal = total;
        return getPassos(hoje);
    }

    // Usado no modo acelerômetro, onde cada passo detectado soma 1 num total "de mentira"
    public int somarPasso(String hoje) {
        if (ultimoTotal < 0) {
            ultimoTotal = 0;
        }
        if (!hoje.equals(dia)) {
            dia = hoje;
            base = ultimoTotal;
        }
        return registrarTotal(ultimoTotal + 1, hoje);
    }

    public void zerar(String hoje) {
        dia = hoje;
        base = Math.max(ultimoTotal, 0);
    }

    public int getPassos(String hoje) {
        if (ultimoTotal < 0 || !hoje.equals(dia)) {
            return 0;
        }
        return Math.max(0, ultimoTotal - base);
    }

    public String getDia() {
        return dia;
    }

    public int getBase() {
        return base;
    }

    public int getUltimoTotal() {
        return ultimoTotal;
    }
}
