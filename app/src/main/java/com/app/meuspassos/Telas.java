package com.app.meuspassos;

import android.graphics.Insets;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;

public final class Telas {

    private Telas() {
    }

    /**
     * A partir do Android 15 o app desenha atrás da barra de status e da barra
     * de navegação, então somamos o tamanho delas ao padding da tela.
     */
    public static void respeitarBarrasDoSistema(View raiz) {
        if (Build.VERSION.SDK_INT < 35) {
            return;
        }

        int esquerda = raiz.getPaddingLeft();
        int topo = raiz.getPaddingTop();
        int direita = raiz.getPaddingRight();
        int baixo = raiz.getPaddingBottom();

        raiz.setOnApplyWindowInsetsListener((view, insets) -> {
            Insets barras = insets.getInsets(WindowInsets.Type.systemBars());
            view.setPadding(
                    esquerda + barras.left,
                    topo + barras.top,
                    direita + barras.right,
                    baixo + barras.bottom);
            return insets;
        });
    }
}
