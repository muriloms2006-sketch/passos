package com.app.meuspassos;

import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.view.View;

public class GraficoPassos extends View {

    private Paint pincel;
    private int passos = 0;
    private final int meta = 5000;

    // Construtor utilizado pelo XML
    public GraficoPassos(Context context, AttributeSet attrs) {
        super(context, attrs);
        configurar();
    }

    // Construtor utilizado pelo Java
    public GraficoPassos(Context context) {
        super(context);
        configurar();
    }

    // Configura o pincel
    private void configurar() {
        pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
        pincel.setAntiAlias(true);
    }

    // Atualiza a quantidade de passos
    public void atualizar(int quantidade) {
        passos = Math.max(0, quantidade);
        invalidate();
    }

    // Desenha o gráfico
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float margem = 25;
        float esquerda = margem;
        float direita = getWidth() - margem;

        float topo = getHeight() / 2f - 15;
        float baixo = topo + 30;

        // Barra de fundo
        pincel.setColor(Color.LTGRAY);

        canvas.drawRoundRect(
                esquerda, topo, direita, baixo,
                15, 15, pincel
        );

        // Calcula o progresso da meta
        float progresso = Math.min(
                1f, passos / (float) meta
        );

        // Barra verde
        pincel.setColor(Color.rgb(40, 160, 90));

        canvas.drawRoundRect(
                esquerda,
                topo,
                esquerda + (direita - esquerda) * progresso,
                baixo,
                15,
                15,
                pincel
        );

        // Texto do percentual
        pincel.setColor(Color.BLACK);
        pincel.setTextSize(32);
        pincel.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(
                (int) (progresso * 100) + "% da meta",
                getWidth() / 2f,
                topo - 20,
                pincel
        );
    }
}
