package com.app.meuspassos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RegistroAdapter extends BaseAdapter {

    private final Context contexto;
    private final List<Registro> registros = new ArrayList<>();
    private final Set<Long> selecionados = new HashSet<>();
    private boolean modoSelecao = false;

    public RegistroAdapter(Context contexto) {
        this.contexto = contexto;
    }

    public void setRegistros(List<Registro> novos) {
        registros.clear();
        registros.addAll(novos);
        selecionados.clear();
        notifyDataSetChanged();
    }

    public boolean isModoSelecao() {
        return modoSelecao;
    }

    public void setModoSelecao(boolean ativo) {
        modoSelecao = ativo;
        if (!ativo) {
            selecionados.clear();
        }
        notifyDataSetChanged();
    }

    public void alternarSelecao(long id) {
        if (!selecionados.remove(id)) {
            selecionados.add(id);
        }
        notifyDataSetChanged();
    }

    public Set<Long> getSelecionados() {
        return new HashSet<>(selecionados);
    }

    @Override
    public int getCount() {
        return registros.size();
    }

    @Override
    public Registro getItem(int posicao) {
        return registros.get(posicao);
    }

    @Override
    public long getItemId(int posicao) {
        return registros.get(posicao).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int posicao, View reaproveitada, ViewGroup pai) {
        View item = reaproveitada;
        if (item == null) {
            item = LayoutInflater.from(contexto).inflate(R.layout.item_registro, pai, false);
        }

        Registro registro = getItem(posicao);
        boolean selecionado = selecionados.contains(registro.getId());

        TextView txtPassos = item.findViewById(R.id.txtPassos);
        TextView txtData = item.findViewById(R.id.txtData);
        TextView txtCoordenadas = item.findViewById(R.id.txtCoordenadas);
        CheckBox chkSelecionado = item.findViewById(R.id.chkSelecionado);

        txtPassos.setText(contexto.getString(
                R.string.item_passos,
                NumberFormat.getIntegerInstance().format(registro.getPassos())));
        txtData.setText(registro.getData());
        txtCoordenadas.setText(String.format(
                Locale.getDefault(), "%.5f, %.5f",
                registro.getLatitude(), registro.getLongitude()));

        chkSelecionado.setVisibility(modoSelecao ? View.VISIBLE : View.GONE);
        chkSelecionado.setChecked(selecionado);
        item.findViewById(R.id.cartao).setBackgroundResource(
                selecionado ? R.drawable.bg_card_selecionado : R.drawable.bg_card);

        return item;
    }
}
