package com.app.meuspassos;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity
        implements SensorEventListener, LocationListener {

    private static final int PEDIR_ATIVIDADE = 1;
    private static final int PEDIR_LOCALIZACAO = 2;

    private static final int META = 5000;

    // Sensor de passos
    private SensorManager gerenciador;
    private Sensor sensorPassos;

    // Banco de dados
    private BancoDados banco;

    // Localização
    private LocationManager localizador;

    // Componentes da tela
    private TextView txtPassos;
    private TextView txtLocalizacao;
    private TextView txtHistorico;
    private GraficoPassos grafico;

    // Dados dos passos
    private int passosIniciais = -1;
    private int passos = 0;

    // Coordenadas
    private double latitude = 0;
    private double longitude = 0;

    private boolean temLocalizacao = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // Recupera os dados após a rotação da tela
        if (savedInstanceState != null) {
            passos = savedInstanceState.getInt("passos", 0);
            passosIniciais = savedInstanceState.getInt("passosIniciais", -1);

            latitude = savedInstanceState.getDouble("latitude", 0);
            longitude = savedInstanceState.getDouble("longitude", 0);
            temLocalizacao = savedInstanceState.getBoolean("temLocalizacao", false);
        }

        // Relaciona os componentes do XML
        txtPassos = findViewById(R.id.txtPassos);
        txtLocalizacao = findViewById(R.id.txtLocalizacao);
        txtHistorico = findViewById(R.id.txtHistorico);
        grafico = findViewById(R.id.grafico);

        Button btnLocalizacao =
                findViewById(R.id.btnLocalizacao);

        Button btnSalvar =
                findViewById(R.id.btnSalvar);

        // Inicializa o banco de dados
        banco = new BancoDados(this);

        // Inicializa o gerenciador de sensores
        gerenciador = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);

        if (gerenciador != null) {
            sensorPassos = gerenciador.getDefaultSensor(
                    Sensor.TYPE_STEP_COUNTER
            );
        }

        // Inicializa o serviço de localização
        localizador = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        // Verifica se o aparelho possui sensor de passos
        if (sensorPassos == null) {
            Toast.makeText(
                    this,
                    "Sensor de passos não encontrado",
                    Toast.LENGTH_LONG
            ).show();
        }

        // Botão para obter localização
        btnLocalizacao.setOnClickListener(v ->
                obterLocalizacao()
        );

        // Botão para salvar registro
        btnSalvar.setOnClickListener(v ->
                salvarRegistro()
        );

        // Atualiza a tela
        atualizarTela();

        // Mostra os registros salvos
        mostrarHistorico();

        // Solicita permissão para contar passos
        if (android.os.Build.VERSION.SDK_INT >= 29 &&
                checkSelfPermission(
                        Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACTIVITY_RECOGNITION
                    },
                    PEDIR_ATIVIDADE
            );
        }
    }

    // Inicia o sensor de passos
    private void iniciarSensor() {

        if (gerenciador == null || sensorPassos == null) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= 29 &&
                checkSelfPermission(
                        Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        gerenciador.registerListener(
                this,
                sensorPassos,
                SensorManager.SENSOR_DELAY_NORMAL
        );
    }

    // Recebe os dados do sensor
    @Override
    public void onSensorChanged(SensorEvent evento) {

        if (evento.sensor.getType() ==
                Sensor.TYPE_STEP_COUNTER) {

            int totalPassos = (int) evento.values[0];

            // Define o valor inicial do sensor
            if (passosIniciais == -1) {
                passosIniciais = totalPassos;
            }

            // Calcula os passos desde o início da medição
            passos = Math.max(
                    0,
                    totalPassos - passosIniciais
            );

            atualizarTela();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int precisao) {
        // Não é necessário tratar a precisão neste projeto.
    }

    // Atualiza os textos e o gráfico
    private void atualizarTela() {

        if (txtPassos != null) {
            txtPassos.setText("Passos: " + passos);
        }

        if (grafico != null) {
            grafico.atualizar(passos);
        }
    }

    // Solicita a localização
    private void obterLocalizacao() {

        boolean temFina = checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        boolean temAproximada = checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        if (!temFina && !temAproximada) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    PEDIR_LOCALIZACAO
            );

            return;
        }

        if (localizador == null) {
            Toast.makeText(
                    this,
                    "Serviço de localização indisponível",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        try {

            String provedor;

            if (temFina &&
                    localizador.isProviderEnabled(
                            LocationManager.GPS_PROVIDER)) {

                provedor = LocationManager.GPS_PROVIDER;

            } else if (localizador.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER)) {

                provedor = LocationManager.NETWORK_PROVIDER;

            } else {
                Toast.makeText(
                        this,
                        "Ative a localização do celular",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            // Tenta obter a última localização conhecida
            Location ultima = localizador.getLastKnownLocation(
                    provedor
            );

            if (ultima != null) {
                receberLocalizacao(ultima);
            } else {
                txtLocalizacao.setText(
                        "Buscando localização..."
                );
            }

            // Solicita atualização da localização
            localizador.requestLocationUpdates(
                    provedor,
                    1000,
                    10,
                    this
            );

        } catch (SecurityException | IllegalArgumentException erro) {

            Toast.makeText(
                    this,
                    "Não foi possível obter a localização",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // Recebe a localização atualizada
    @Override
    public void onLocationChanged(Location local) {

        receberLocalizacao(local);

        if (localizador != null) {
            localizador.removeUpdates(this);
        }
    }

    // Guarda latitude e longitude
    private void receberLocalizacao(Location local) {

        latitude = local.getLatitude();
        longitude = local.getLongitude();

        temLocalizacao = true;

        txtLocalizacao.setText(
                "Latitude: " + latitude +
                        "\nLongitude: " + longitude
        );
    }

    // Salva o registro no banco
    private void salvarRegistro() {

        if (sensorPassos == null || passosIniciais == -1) {

            Toast.makeText(
                    this,
                    "Aguardando leitura do sensor de passos",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (!temLocalizacao) {

            Toast.makeText(
                    this,
                    "Obtenha a localização antes de salvar",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Obtém a data e o horário
        SimpleDateFormat formato =
                new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                );

        String data = formato.format(new Date());

        // Salva os dados
        boolean salvou = banco.salvar(
                passos,
                latitude,
                longitude,
                data
        );

        if (salvou) {

            Toast.makeText(
                    this,
                    "Registro salvo com sucesso!",
                    Toast.LENGTH_SHORT
            ).show();

            mostrarHistorico();

        } else {

            Toast.makeText(
                    this,
                    "Erro ao salvar registro",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Consulta e mostra o histórico
    private void mostrarHistorico() {

        Cursor cursor = banco.listar();

        StringBuilder historico = new StringBuilder();

        try {

            if (cursor.moveToFirst()) {

                do {

                    int quantidade = cursor.getInt(0);
                    double lat = cursor.getDouble(1);
                    double lon = cursor.getDouble(2);
                    String data = cursor.getString(3);

                    historico.append("Passos: ")
                            .append(quantidade)
                            .append("\nLatitude: ")
                            .append(lat)
                            .append("\nLongitude: ")
                            .append(lon)
                            .append("\nData: ")
                            .append(data)
                            .append("\n\n");

                } while (cursor.moveToNext());

                txtHistorico.setText(historico.toString());

            } else {

                txtHistorico.setText(
                        "Nenhum registro salvo"
                );
            }

        } finally {
            cursor.close();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);

        // Salva a quantidade de passos
        estado.putInt("passos", passos);

        // Salva a leitura inicial do sensor
        estado.putInt("passosIniciais", passosIniciais);

        // Salva a localização
        estado.putDouble("latitude", latitude);
        estado.putDouble("longitude", longitude);
        estado.putBoolean("temLocalizacao", temLocalizacao);
    }

    // Trata as permissões solicitadas
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

        if (codigo == PEDIR_ATIVIDADE) {

            if (resultados.length > 0 &&
                    resultados[0] == PackageManager.PERMISSION_GRANTED) {

                iniciarSensor();

            } else {

                Toast.makeText(
                        this,
                        "Permissão para contar passos negada",
                        Toast.LENGTH_LONG
                ).show();
            }
        }

        if (codigo == PEDIR_LOCALIZACAO) {

            if (resultados.length > 0 &&
                    (checkSelfPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED)) {

                obterLocalizacao();

            } else {

                Toast.makeText(
                        this,
                        "Permissão de localização negada",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    // Inicia o sensor quando a tela volta a ficar ativa
    @Override
    protected void onResume() {
        super.onResume();

        if (android.os.Build.VERSION.SDK_INT < 29 ||
                checkSelfPermission(
                        Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED) {

            iniciarSensor();
        }
    }

    // Para o sensor e as atualizações ao sair da tela
    @Override
    protected void onPause() {
        super.onPause();

        if (gerenciador != null) {
            gerenciador.unregisterListener(this);
        }

        if (localizador != null) {
            localizador.removeUpdates(this);
        }
    }

    @Override
    public void onProviderEnabled(String provedor) {
        // Provedor ativado.
    }

    @Override
    public void onProviderDisabled(String provedor) {
        // Provedor desativado.
    }

    @Override
    public void onStatusChanged(
            String provedor, int status, Bundle extras) {
        // Método mantido para compatibilidade.
    }
}
