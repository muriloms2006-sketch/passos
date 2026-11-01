package com.app.meuspassos;

// Importações necessárias para o funcionamento do aplicativo
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.os.Looper;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Classe principal do aplicativo.
// Implementa as interfaces para receber informações dos sensores
// e atualizações da localização do aparelho.
public class MainActivity extends Activity
        implements SensorEventListener, LocationListener {

    // Códigos utilizados para identificar as solicitações de permissão
    private static final int PEDIR_ATIVIDADE = 1;
    private static final int PEDIR_LOCALIZACAO = 2;

    // Meta diária de passos
    private static final int META = 5000;

    // Gerenciador responsável pelos sensores do celular
    private SensorManager gerenciador;

    // Sensor responsável por contar os passos
    private Sensor sensorPassos;

    // Classe responsável pelo banco de dados
    private BancoDados banco;

    // Gerenciador responsável por obter a localização
    private LocationManager localizador;

    // Componentes visuais da tela
    private TextView txtPassos;
    private TextView txtLocalizacao;
    private TextView txtHistorico;

    // Gráfico personalizado que mostra o progresso da meta
    private GraficoPassos grafico;

    // Guarda a primeira leitura do sensor para calcular os passos
    private int passosIniciais = -1;

    // Quantidade atual de passos
    private int passos = 0;

    // Coordenadas geográficas do aparelho
    private double latitude;
    private double longitude;

    // Indica se a localização já foi obtida
    private boolean temLocalizacao = false;


    // Método executado quando o aplicativo é iniciado
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Define o arquivo XML que representa a tela do aplicativo
        setContentView(R.layout.activity_main);

        // Relaciona as variáveis aos componentes do XML
        txtPassos = findViewById(R.id.txtPassos);
        txtLocalizacao = findViewById(R.id.txtLocalizacao);
        txtHistorico = findViewById(R.id.txtHistorico);
        grafico = findViewById(R.id.grafico);

        // Localiza os botões da tela
        Button btnLocalizacao = findViewById(R.id.btnLocalizacao);
        Button btnSalvar = findViewById(R.id.btnSalvar);

        // Cria uma instância do banco de dados
        banco = new BancoDados(this);

        // Obtém o serviço responsável pelos sensores
        gerenciador = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);

        // Obtém o sensor de contagem de passos
        sensorPassos = gerenciador.getDefaultSensor(
                Sensor.TYPE_STEP_COUNTER);

        // Obtém o serviço responsável pela localização
        localizador = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        // Verifica se o aparelho possui o sensor de passos
        if (sensorPassos == null) {
            txtPassos.setText("Sensor de passos não disponível");
        }

        // Define o que acontece quando o botão de localização é clicado
        btnLocalizacao.setOnClickListener(v -> obterLocalizacao());

        // Define o que acontece quando o botão de salvar é clicado
        btnSalvar.setOnClickListener(v -> salvarRegistro());

        // Atualiza as informações exibidas na tela
        atualizarTela();

        // Mostra os registros que já estão salvos no banco de dados
        mostrarHistorico();

        // Verifica se o aplicativo possui permissão para reconhecer atividades
        if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {

            // Solicita a permissão ao usuário
            requestPermissions(
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    PEDIR_ATIVIDADE
            );

        } else if (sensorPassos != null) {

            // Se a permissão já existe, inicia o sensor
            iniciarSensor();
        }
    }


    // Registra o aplicativo para receber informações do sensor de passos
    private void iniciarSensor() {

        if (sensorPassos != null) {

            // Define que o sensor enviará atualizações ao aplicativo
            gerenciador.registerListener(
                    this,
                    sensorPassos,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }
    }


    // Método executado sempre que o sensor envia uma nova informação
    @Override
    public void onSensorChanged(SensorEvent evento) {

        // Obtém a quantidade total de passos registrada pelo sensor
        int total = (int) evento.values[0];

        // Na primeira leitura, guarda o valor inicial do sensor
        if (passosIniciais == -1) {
            passosIniciais = total;
        }

        // Calcula a quantidade de passos desde a primeira leitura
        passos = Math.max(0, total - passosIniciais);

        // Atualiza o número de passos e o gráfico na tela
        atualizarTela();
    }


    // Método chamado quando a precisão do sensor muda
    // Não é utilizado diretamente neste aplicativo
    @Override
    public void onAccuracyChanged(Sensor sensor, int precisao) {
    }


    // Atualiza as informações de passos e o gráfico
    private void atualizarTela() {

        // Exibe a quantidade atual de passos
        txtPassos.setText("Passos: " + passos);

        // Atualiza a barra de progresso do gráfico
        grafico.atualizar(passos);
    }


    // Método responsável por obter a localização do aparelho
    private void obterLocalizacao() {

        // Verifica se existe permissão para acessar a localização
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Solicita as permissões de localização
            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    PEDIR_LOCALIZACAO
            );

            return;
        }

        try {

            // Tenta obter a última localização conhecida pelo GPS
            Location ultima = null;

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    && localizador.isProviderEnabled(
                    LocationManager.GPS_PROVIDER)) {

                ultima = localizador.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER);
            }

            // Se não encontrou pelo GPS, tenta pela rede
            if (ultima == null
                    && localizador.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER)) {

                ultima = localizador.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER);
            }

            // Se encontrou uma localização, utiliza os dados
            if (ultima != null) {

                receberLocalizacao(ultima);

            } else {

                // Se não encontrou, solicita uma nova atualização
                localizador.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000,
                        1,
                        this,
                        Looper.getMainLooper()
                );

                // Informa que está procurando a localização
                Toast.makeText(
                        this,
                        "Buscando localização...",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } catch (SecurityException | IllegalArgumentException erro) {

            // Exibe uma mensagem caso ocorra algum problema
            Toast.makeText(
                    this,
                    "Não foi possível obter a localização",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // Método chamado quando uma nova localização é recebida
    @Override
    public void onLocationChanged(Location local) {

        // Processa as informações recebidas
        receberLocalizacao(local);

        // Para de solicitar atualizações depois de obter a localização
        if (localizador != null) {
            localizador.removeUpdates(this);
        }
    }


    // Recebe e armazena latitude e longitude
    private void receberLocalizacao(Location local) {

        // Obtém a latitude e a longitude do aparelho
        latitude = local.getLatitude();
        longitude = local.getLongitude();

        // Informa que a localização foi encontrada
        temLocalizacao = true;

        // Exibe as coordenadas na tela
        txtLocalizacao.setText(
                "Latitude: " + latitude +
                        "\nLongitude: " + longitude
        );
    }


    // Salva a quantidade de passos e a localização no banco de dados
    private void salvarRegistro() {

        // Verifica se o sensor já realizou uma leitura
        if (passosIniciais == -1 || sensorPassos == null) {

            Toast.makeText(
                    this,
                    "Sensor de passos ainda não está disponível",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Verifica se a localização foi obtida
        if (!temLocalizacao) {

            Toast.makeText(
                    this,
                    "Obtenha a localização antes de salvar",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Obtém a data e o horário atuais
        String data = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
        ).format(new Date());

        // Salva os passos, coordenadas e data no banco de dados
        boolean salvo = banco.salvar(
                passos,
                latitude,
                longitude,
                data
        );

        // Verifica se o registro foi salvo corretamente
        if (salvo) {

            Toast.makeText(
                    this,
                    "Registro salvo com sucesso!",
                    Toast.LENGTH_SHORT
            ).show();

            // Atualiza o histórico exibido na tela
            mostrarHistorico();

        } else {

            // Informa que ocorreu um erro ao salvar
            Toast.makeText(
                    this,
                    "Erro ao salvar registro",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // Consulta os registros armazenados no banco de dados
    private void mostrarHistorico() {

        // Busca os dados salvos
        Cursor cursor = banco.listar();

        // Cria uma variável para montar o texto do histórico
        StringBuilder historico = new StringBuilder();

        try {

            // Verifica se existem registros
            if (cursor != null && cursor.moveToFirst()) {

                // Percorre todos os registros encontrados
                do {

                    // Obtém os dados de cada coluna
                    int quantidade = cursor.getInt(0);
                    double lat = cursor.getDouble(1);
                    double lon = cursor.getDouble(2);
                    String data = cursor.getString(3);

                    // Adiciona as informações ao histórico
                    historico.append("Passos: ")
                            .append(quantidade)
                            .append("\nLocalização: ")
                            .append(lat)
                            .append(", ")
                            .append(lon)
                            .append("\nData: ")
                            .append(data)
                            .append("\n\n");

                } while (cursor.moveToNext());

            } else {

                // Mensagem exibida quando não existem registros
                historico.append("Nenhum registro salvo.");
            }

        } finally {

            // Fecha o cursor após terminar a consulta
            if (cursor != null) {
                cursor.close();
            }
        }

        // Exibe o histórico na tela
        txtHistorico.setText(historico.toString());
    }


    // Recebe a resposta do usuário sobre as permissões solicitadas
    @Override
    public void onRequestPermissionsResult(
            int codigo,
            String[] permissoes,
            int[] resultados) {

        super.onRequestPermissionsResult(
                codigo,
                permissoes,
                resultados
        );

        // Verifica a resposta da permissão de atividade física
        if (codigo == PEDIR_ATIVIDADE) {

            if (resultados.length > 0
                    && resultados[0] == PackageManager.PERMISSION_GRANTED) {

                // Se autorizou, inicia o sensor de passos
                iniciarSensor();

            } else {

                // Informa que a permissão foi negada
                Toast.makeText(
                        this,
                        "Permissão de atividade física necessária",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        // Verifica a resposta das permissões de localização
        if (codigo == PEDIR_LOCALIZACAO) {

            if (resultados.length > 0
                    && (resultados[0] == PackageManager.PERMISSION_GRANTED
                    || (resultados.length > 1
                    && resultados[1] == PackageManager.PERMISSION_GRANTED))) {

                // Se autorizou, tenta obter a localização novamente
                obterLocalizacao();
            }
        }
    }


    // Método executado quando a Activity volta a ficar visível
    @Override
    protected void onResume() {
        super.onResume();

        // Verifica se a permissão existe antes de iniciar o sensor
        if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED
                && sensorPassos != null) {

            iniciarSensor();
        }
    }


    // Método executado quando a Activity deixa de estar em primeiro plano
    @Override
    protected void onPause() {
        super.onPause();

        // Para de receber atualizações do sensor
        if (gerenciador != null) {
            gerenciador.unregisterListener(this);
        }

        // Para de receber atualizações de localização
        if (localizador != null) {
            localizador.removeUpdates(this);
        }
    }


    // Métodos da interface LocationListener.
    // São mantidos para atender à interface, mas não são utilizados
    // diretamente nesta versão do aplicativo.

    @Override
    public void onProviderEnabled(String provedor) {
    }

    @Override
    public void onProviderDisabled(String provedor) {
    }

    @Override
    public void onStatusChanged(
            String provedor, int status, Bundle extras) {
    }
}