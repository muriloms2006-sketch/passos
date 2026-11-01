package com.app.meuspassos;

// Importações necessárias para desenhar a View.
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.view.View;

// Classe responsável por desenhar o gráfico de progresso dos passos.
public class GraficoPassos extends View {

    // Pincel utilizado para desenhar textos e barras.
    private final Paint pincel =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    // Quantidade atual de passos.
    private int passos = 0;

    // Meta diária de passos.
    private final int meta = 5000;

    // Construtor da View.
    public GraficoPassos(Context context) {
        super(context);
    }

    // Recebe a quantidade de passos da MainActivity.
    // invalidate() solicita que o gráfico seja redesenhado.
    public void atualizar(int quantidade) {
        passos = quantidade;
        invalidate();
    }

    // Método responsável por desenhar o gráfico na tela.
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Define as posições e dimensões da barra.
        float margem = 25;
        float esquerda = margem;
        float direita = getWidth() - margem;
        float topo = getHeight() / 2f - 15;
        float baixo = topo + 30;

        // Desenha a barra de fundo na cor cinza.
        pincel.setColor(Color.LTGRAY);
        canvas.drawRoundRect(
                esquerda, topo, direita, baixo,
                15, 15, pincel);

        // Calcula o progresso em relação à meta.
        // Math.min impede que o progresso ultrapasse 100%.
        float progresso = Math.min(
                1f, passos / (float) meta);

        // Desenha a parte preenchida na cor verde.
        pincel.setColor(Color.rgb(40, 160, 90));
        canvas.drawRoundRect(
                esquerda, topo,
                esquerda + (direita - esquerda) * progresso,
                baixo, 15, 15, pincel);

        // Configura a cor, o tamanho e o alinhamento do texto.
        pincel.setColor(Color.BLACK);
        pincel.setTextSize(32);
        pincel.setTextAlign(Paint.Align.CENTER);

        // Exibe a porcentagem da meta alcançada.
        canvas.drawText(
                (int)(progresso * 100) + "% da meta",
                getWidth() / 2f,
                topo - 20, pincel);
    }
}