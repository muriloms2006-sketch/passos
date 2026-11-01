package com.app.meuspassos;

// Importa a classe Context, que permite acessar recursos do aplicativo.
import android.content.Context;

// Permite armazenar valores nas colunas do banco de dados.
import android.content.ContentValues;

// Permite consultar e percorrer os registros do banco.
import android.database.Cursor;

// Permite realizar operações de leitura e escrita no SQLite.
import android.database.sqlite.SQLiteDatabase;

// Classe responsável por criar, atualizar e gerenciar o banco SQLite.
import android.database.sqlite.SQLiteOpenHelper;

// BancoDados herda SQLiteOpenHelper para facilitar o gerenciamento do banco.
public class BancoDados extends SQLiteOpenHelper {

    // Construtor da classe.
    // Define o nome do banco como passos.db e sua versão como 1.
    public BancoDados(Context context) {
        super(context, "passos.db", null, 1);
    }

    // Método executado automaticamente quando o banco é criado pela primeira vez.
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Cria a tabela registros com suas respectivas colunas.
        db.execSQL(
                "CREATE TABLE registros (" +

                        // Identificador único de cada registro.
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        // Armazena a quantidade de passos.
                        "passos INTEGER, " +

                        // Armazena a latitude da localização.
                        "latitude REAL, " +

                        // Armazena a longitude da localização.
                        "longitude REAL, " +

                        // Armazena a data e o horário do registro.
                        "data TEXT)"
        );
    }

    // Método utilizado quando a estrutura ou versão do banco é alterada.
    @Override
    public void onUpgrade(
            SQLiteDatabase db, int antiga, int nova) {

        // Implementar migrações se o banco mudar.
    }

    // Método responsável por salvar um registro no banco.
    public boolean salvar(
            int passos, double latitude,
            double longitude, String data) {

        // Abre o banco de dados para escrita.
        SQLiteDatabase db = getWritableDatabase();

        // Cria um conjunto de valores para armazenar nas colunas.
        ContentValues valores = new ContentValues();

        // Adiciona a quantidade de passos.
        valores.put("passos", passos);

        // Adiciona a latitude.
        valores.put("latitude", latitude);

        // Adiciona a longitude.
        valores.put("longitude", longitude);

        // Adiciona a data e o horário.
        valores.put("data", data);

        // Insere os valores na tabela registros.
        // Retorna o identificador do registro inserido.
        long resultado = db.insert(
                "registros", null, valores);

        // Fecha a conexão com o banco de dados.
        db.close();

        // Retorna true se salvou corretamente.
        // Retorna false se ocorreu algum erro.
        return resultado != -1;
    }

    // Método responsável por consultar os registros salvos.
    public Cursor listar() {

        // Abre o banco de dados para leitura.
        SQLiteDatabase db = getReadableDatabase();

        // Busca os passos, latitude, longitude e data.
        // ORDER BY id DESC mostra primeiro os registros mais recentes.
        return db.rawQuery(
                "SELECT passos, latitude, longitude, data " +
                        "FROM registros ORDER BY id DESC",
                null
        );
    }
}