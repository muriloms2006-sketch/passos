package com.app.meuspassos;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BancoDados extends SQLiteOpenHelper {

    // Nome e versão do banco
    private static final String NOME_BANCO = "passos.db";
    private static final int VERSAO = 1;

    public BancoDados(Context context) {
        super(context, NOME_BANCO, null, VERSAO);
    }

    // Cria a tabela quando o banco é criado
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE registros (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "passos INTEGER, " +
                        "latitude REAL, " +
                        "longitude REAL, " +
                        "data TEXT)"
        );
    }

    // Atualização da estrutura do banco
    @Override
    public void onUpgrade(
            SQLiteDatabase db, int antiga, int nova) {

        // Não há migrações nesta versão.
    }

    // Salva um registro
    public boolean salvar(
            int passos,
            double latitude,
            double longitude,
            String data) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues valores = new ContentValues();

        valores.put("passos", passos);
        valores.put("latitude", latitude);
        valores.put("longitude", longitude);
        valores.put("data", data);

        long resultado = db.insert(
                "registros",
                null,
                valores
        );

        return resultado != -1;
    }

    // Consulta os registros salvos
    public Cursor listar() {

        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT passos, latitude, longitude, data " +
                        "FROM registros ORDER BY id DESC",
                null
        );
    }
}
