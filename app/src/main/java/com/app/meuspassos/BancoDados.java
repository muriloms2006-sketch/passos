package com.app.meuspassos;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BancoDados extends SQLiteOpenHelper {

    private static final String NOME_BANCO = "passos.db";
    private static final int VERSAO = 1;

    public BancoDados(Context context) {
        super(context, NOME_BANCO, null, VERSAO);
    }

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

    @Override
    public void onUpgrade(
            SQLiteDatabase db, int antiga, int nova) {
    }

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

    public List<Registro> listar() {

        SQLiteDatabase db = getReadableDatabase();

        List<Registro> registros = new ArrayList<>();

        try (Cursor cursor = db.rawQuery(
                "SELECT id, passos, latitude, longitude, data " +
                        "FROM registros ORDER BY id DESC",
                null
        )) {
            while (cursor.moveToNext()) {
                registros.add(new Registro(
                        cursor.getLong(0),
                        cursor.getInt(1),
                        cursor.getDouble(2),
                        cursor.getDouble(3),
                        cursor.getString(4)
                ));
            }
        }

        return registros;
    }

    public int contar() {

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM registros",
                null
        )) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    public int apagar(Collection<Long> ids) {

        SQLiteDatabase db = getWritableDatabase();

        int apagados = 0;

        db.beginTransaction();
        try {
            for (long id : ids) {
                apagados += db.delete(
                        "registros",
                        "id = ?",
                        new String[]{String.valueOf(id)}
                );
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return apagados;
    }

    public int apagarTodos() {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete("registros", null, null);
    }
}
