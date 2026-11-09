package com.app.meuspassos;

import android.app.Activity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HistoricoActivity extends Activity {

    private BancoDados banco;
    private RegistroAdapter adapter;

    private TextView txtTitulo;
    private TextView txtResumo;
    private TextView txtDica;
    private TextView txtVazio;
    private Button btnApagarTudo;
    private Button btnApagarSelecionados;
    private View barraSelecao;

    private OnBackInvokedCallback sairDaSelecao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_historico);
        Telas.respeitarBarrasDoSistema(findViewById(R.id.raiz));

        banco = new BancoDados(this);
        adapter = new RegistroAdapter(this);

        txtTitulo = findViewById(R.id.txtTitulo);
        txtResumo = findViewById(R.id.txtResumo);
        txtDica = findViewById(R.id.txtDica);
        txtVazio = findViewById(R.id.txtVazio);
        btnApagarTudo = findViewById(R.id.btnApagarTudo);
        btnApagarSelecionados = findViewById(R.id.btnApagarSelecionados);
        barraSelecao = findViewById(R.id.barraSelecao);

        ListView lista = findViewById(R.id.lista);
        lista.setAdapter(adapter);

        lista.setOnItemClickListener((pai, item, posicao, id) -> {
            if (adapter.isModoSelecao()) {
                adapter.alternarSelecao(id);
                atualizarSelecao();
            } else {
                abrirNoMapa(adapter.getItem(posicao));
            }
        });

        lista.setOnItemLongClickListener((pai, item, posicao, id) -> {
            if (!adapter.isModoSelecao()) {
                entrarNaSelecao();
            }
            adapter.alternarSelecao(id);
            atualizarSelecao();
            return true;
        });

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancelarSelecao).setOnClickListener(v -> sairDaSelecao());
        btnApagarTudo.setOnClickListener(v -> confirmarApagarTudo());
        btnApagarSelecionados.setOnClickListener(v -> confirmarApagarSelecionados());

        carregar();
    }

    private void carregar() {
        List<Registro> registros = banco.listar();
        adapter.setRegistros(registros);

        int totalPassos = 0;
        for (Registro registro : registros) {
            totalPassos += registro.getPassos();
        }

        boolean vazio = registros.isEmpty();
        txtVazio.setVisibility(vazio ? View.VISIBLE : View.GONE);
        txtResumo.setVisibility(vazio ? View.GONE : View.VISIBLE);
        txtDica.setVisibility(vazio ? View.GONE : View.VISIBLE);
        btnApagarTudo.setVisibility(vazio ? View.GONE : View.VISIBLE);

        txtResumo.setText(getResources().getQuantityString(
                R.plurals.resumo_historico,
                registros.size(),
                registros.size(),
                NumberFormat.getIntegerInstance().format(totalPassos)));
    }

    private void entrarNaSelecao() {
        adapter.setModoSelecao(true);
        barraSelecao.setVisibility(View.VISIBLE);
        btnApagarTudo.setVisibility(View.GONE);

        // No Android 13+ o "voltar" é tratado por callback; aqui ele só sai da seleção
        if (Build.VERSION.SDK_INT >= 33) {
            sairDaSelecao = this::sairDaSelecao;
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, sairDaSelecao);
        }
    }

    private void sairDaSelecao() {
        adapter.setModoSelecao(false);
        barraSelecao.setVisibility(View.GONE);
        txtTitulo.setText(R.string.titulo_historico);
        btnApagarTudo.setVisibility(adapter.getCount() > 0 ? View.VISIBLE : View.GONE);

        if (Build.VERSION.SDK_INT >= 33 && sairDaSelecao != null) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(sairDaSelecao);
            sairDaSelecao = null;
        }
    }

    private void atualizarSelecao() {
        int quantidade = adapter.getSelecionados().size();

        if (quantidade == 0) {
            sairDaSelecao();
            return;
        }

        txtTitulo.setText(getResources().getQuantityString(
                R.plurals.selecionados, quantidade, quantidade));
        btnApagarSelecionados.setText(getString(R.string.botao_apagar_selecionados, quantidade));
    }

    // Só é chamado abaixo do Android 13; do 13 em diante quem trata é o callback de entrarNaSelecao()
    @Override
    @SuppressWarnings("deprecation")
    @SuppressLint("GestureBackNavigation")
    public void onBackPressed() {
        if (adapter.isModoSelecao()) {
            sairDaSelecao();
        } else {
            super.onBackPressed();
        }
    }

    private void confirmarApagarSelecionados() {
        Set<Long> ids = adapter.getSelecionados();

        new AlertDialog.Builder(this)
                .setTitle(R.string.apagar_selecionados_titulo)
                .setMessage(getResources().getQuantityString(
                        R.plurals.apagar_selecionados_mensagem, ids.size(), ids.size()))
                .setPositiveButton(R.string.botao_apagar, (dialogo, botao) -> {
                    int apagados = banco.apagar(ids);
                    sairDaSelecao();
                    carregar();
                    avisarApagados(apagados);
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void confirmarApagarTudo() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.apagar_tudo_titulo)
                .setMessage(getResources().getQuantityString(
                        R.plurals.apagar_tudo_mensagem, adapter.getCount(), adapter.getCount()))
                .setPositiveButton(R.string.botao_apagar, (dialogo, botao) -> {
                    int apagados = banco.apagarTodos();
                    carregar();
                    avisarApagados(apagados);
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void avisarApagados(int quantidade) {
        Toast.makeText(
                this,
                getResources().getQuantityString(
                        R.plurals.registros_apagados, quantidade, quantidade),
                Toast.LENGTH_SHORT
        ).show();
    }

    private void abrirNoMapa(Registro registro) {
        // Coordenadas na URI sempre com ponto, independente do idioma do celular
        String coordenadas = String.format(
                Locale.US, "%.6f,%.6f", registro.getLatitude(), registro.getLongitude());
        String rotulo = Uri.encode(getString(R.string.local_registro, registro.getData()));

        Intent mapa = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("geo:" + coordenadas + "?q=" + coordenadas + "(" + rotulo + ")"));

        try {
            startActivity(mapa);
        } catch (ActivityNotFoundException semAppDeMapa) {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=" + coordenadas)));
        }
    }
}
