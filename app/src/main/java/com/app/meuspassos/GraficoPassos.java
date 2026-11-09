package com.app.meuspassos;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.text.NumberFormat;

/**
 * Anel de progresso dos passos em relação à meta, com o número de passos no
 * centro. Meta, cores e espessura podem ser configuradas pelo XML
 * (app:meta, app:corProgresso, ...), veja res/values/attrs.xml.
 */
public class GraficoPassos extends View {

    private final Paint pincelAnel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelNumero = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelLegenda = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF areaAnel = new RectF();

    private int meta;
    private float espessura;
    private int corProgresso;
    private int corTrilha;
    private int corMetaAtingida;

    private int passos = 0;
    private float progressoDesenhado = 0f;
    private ValueAnimator animacao;

    public GraficoPassos(Context context) {
        this(context, null);
    }

    public GraficoPassos(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray valores = context.obtainStyledAttributes(attrs, R.styleable.GraficoPassos);
        try {
            meta = valores.getInt(
                    R.styleable.GraficoPassos_meta,
                    getResources().getInteger(R.integer.meta_diaria));
            espessura = valores.getDimension(
                    R.styleable.GraficoPassos_espessura, dp(16));
            corProgresso = valores.getColor(
                    R.styleable.GraficoPassos_corProgresso, context.getColor(R.color.primaria));
            corTrilha = valores.getColor(
                    R.styleable.GraficoPassos_corTrilha, context.getColor(R.color.primaria_clara));
            corMetaAtingida = valores.getColor(
                    R.styleable.GraficoPassos_corMetaAtingida, context.getColor(R.color.sucesso));

            int corTexto = valores.getColor(
                    R.styleable.GraficoPassos_corTexto, context.getColor(R.color.texto));
            pincelNumero.setColor(corTexto);
            pincelLegenda.setColor(corTexto);
        } finally {
            valores.recycle();
        }

        pincelAnel.setStyle(Paint.Style.STROKE);
        pincelAnel.setStrokeCap(Paint.Cap.ROUND);

        pincelNumero.setTextAlign(Paint.Align.CENTER);
        pincelNumero.setTypeface(Typeface.DEFAULT_BOLD);

        pincelLegenda.setTextAlign(Paint.Align.CENTER);
        pincelLegenda.setAlpha(170);
    }

    public void atualizar(int quantidade) {
        passos = Math.max(0, quantidade);

        float alvo = Estimativas.progresso(passos, meta);
        if (animacao != null) {
            animacao.cancel();
        }
        animacao = ValueAnimator.ofFloat(progressoDesenhado, alvo);
        animacao.setDuration(600);
        animacao.setInterpolator(new DecelerateInterpolator());
        animacao.addUpdateListener(a -> {
            progressoDesenhado = (float) a.getAnimatedValue();
            invalidate();
        });
        animacao.start();
    }

    public void setMeta(int novaMeta) {
        meta = novaMeta;
        atualizar(passos);
    }

    public int getMeta() {
        return meta;
    }

    @Override
    protected void onSizeChanged(int largura, int altura, int antigaLargura, int antigaAltura) {
        super.onSizeChanged(largura, altura, antigaLargura, antigaAltura);

        float livreX = largura - getPaddingLeft() - getPaddingRight();
        float livreY = altura - getPaddingTop() - getPaddingBottom();
        float raio = (Math.min(livreX, livreY) - espessura) / 2f;
        float centroX = getPaddingLeft() + livreX / 2f;
        float centroY = getPaddingTop() + livreY / 2f;

        areaAnel.set(centroX - raio, centroY - raio, centroX + raio, centroY + raio);
        pincelAnel.setStrokeWidth(espessura);
        pincelNumero.setTextSize(raio * 0.48f);
        pincelLegenda.setTextSize(raio * 0.16f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        pincelAnel.setColor(corTrilha);
        canvas.drawArc(areaAnel, 0, 360, false, pincelAnel);

        if (progressoDesenhado > 0f) {
            pincelAnel.setColor(progressoDesenhado >= 1f ? corMetaAtingida : corProgresso);
            // Começa em -90° pra sair do topo do anel, como um relógio
            canvas.drawArc(areaAnel, -90, 360 * progressoDesenhado, false, pincelAnel);
        }

        float centroX = areaAnel.centerX();
        float centroY = areaAnel.centerY();

        canvas.drawText(
                NumberFormat.getIntegerInstance().format(passos),
                centroX,
                centroY + pincelNumero.getTextSize() * 0.3f,
                pincelNumero);

        int percentual = Math.round(Estimativas.progresso(passos, meta) * 100);
        canvas.drawText(
                getContext().getString(R.string.percentual_meta, percentual),
                centroX,
                centroY + pincelNumero.getTextSize() * 0.3f + pincelLegenda.getTextSize() * 1.8f,
                pincelLegenda);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animacao != null) {
            animacao.cancel();
        }
        super.onDetachedFromWindow();
    }

    private float dp(float valor) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, valor, getResources().getDisplayMetrics());
    }
}
