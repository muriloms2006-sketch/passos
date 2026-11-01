package com.app.meuspassos;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BancoDados extends SQLiteOpenHelper {

    public BancoDados(Context context) {
        super(context, "passos.db", null, 1);
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
        // Implementar migrações se o banco mudar.
    }

    public boolean salvar(
            int passos, double latitude,
            double longitude, String data) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("passos", passos);
        valores.put("latitude", latitude);
        valores.put("longitude", longitude);
        valores.put("data", data);

        long resultado = db.insert(
                "registros", null, valores);

        db.close();
        return resultado != -1;
    }

    public Cursor listar() {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT passos, latitude, longitude, data " +
                        "FROM registros ORDER BY id DESC",
                null
        );
    }
}