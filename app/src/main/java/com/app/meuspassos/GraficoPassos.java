package com.app.meuspassos;

import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class GraficoPassos extends View {

    private Paint pincel;
    private int passos = 0;
    private int meta;

    public GraficoPassos(Context context, AttributeSet attrs) {
        super(context, attrs);
        configurar();
    }

    public GraficoPassos(Context context) {
        super(context);
        configurar();
    }

    private void configurar() {
        pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
        meta = getResources().getInteger(R.integer.meta_diaria);
    }

    public void atualizar(int quantidade) {
        passos = Math.max(0, quantidade);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float margem = 25;
        float esquerda = margem;
        float direita = getWidth() - margem;

        float topo = getHeight() / 2f - 15;
        float baixo = topo + 30;

        pincel.setColor(getContext().getColor(R.color.progresso_fundo));

        canvas.drawRoundRect(
                esquerda, topo,
                direita, baixo,
                15, 15, pincel
        );

        float progresso = Math.min(
                1f, passos / (float) meta
        );

        pincel.setColor(getContext().getColor(R.color.progresso));

        canvas.drawRoundRect(
                esquerda, topo,
                esquerda + (direita - esquerda) * progresso,
                baixo,
                15, 15, pincel
        );

        pincel.setColor(getContext().getColor(R.color.texto));
        pincel.setTextSize(32);
        pincel.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(
                getContext().getString(R.string.percentual_meta, (int) (progresso * 100)),
                getWidth() / 2f,
                topo - 20,
                pincel
        );
    }
}
