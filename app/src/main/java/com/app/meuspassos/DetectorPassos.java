package com.app.meuspassos;

/**
 * Detecta passos a partir do acelerômetro, para aparelhos sem sensor de passos.
 *
 * A cada passo a aceleração total sobe acima da gravidade e depois desce.
 * Contamos um passo quando o sinal passa do limiar de subida, e só liberamos o
 * próximo depois que ele cair abaixo do limiar de descida (histerese), com um
 * intervalo mínimo entre passos pra não contar a mesma pisada duas vezes.
 */
public class DetectorPassos {

    static final float GRAVIDADE = 9.81f;
    static final float LIMIAR_SUBIDA = 11.0f;
    static final float LIMIAR_DESCIDA = 10.0f;
    static final long INTERVALO_MINIMO_NS = 300_000_000L;
    private static final float SUAVIZACAO = 0.3f;

    private float filtrada = GRAVIDADE;
    private boolean acima = false;
    private long ultimoPassoNs = -INTERVALO_MINIMO_NS;

    public boolean processar(float x, float y, float z, long instanteNs) {
        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);
        filtrada += SUAVIZACAO * (magnitude - filtrada);

        if (acima) {
            if (filtrada < LIMIAR_DESCIDA) {
                acima = false;
            }
            return false;
        }

        if (filtrada <= LIMIAR_SUBIDA) {
            return false;
        }

        acima = true;
        if (instanteNs - ultimoPassoNs < INTERVALO_MINIMO_NS) {
            return false;
        }

        ultimoPassoNs = instanteNs;
        return true;
    }
}
