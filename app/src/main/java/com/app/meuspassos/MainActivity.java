package com.app.meuspassos;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity
        implements SensorEventListener, LocationListener {

    private static final int PEDIR_ATIVIDADE = 1;
    private static final int PEDIR_LOCALIZACAO = 2;

    private SensorManager gerenciador;
    private Sensor sensorPassos;
    private Sensor acelerometro;

    private ContadorDiario contador;
    private final DetectorPassos detector = new DetectorPassos();
    private SharedPreferences preferencias;

    private BancoDados banco;

    private LocationManager localizador;

    private TextView txtPassos;
    private TextView txtEstatisticas;
    private TextView txtLocalizacao;
    private TextView txtHistorico;
    private GraficoPassos grafico;

    private double latitude = 0;
    private double longitude = 0;

    private boolean temLocalizacao = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            latitude = savedInstanceState.getDouble("latitude", 0);
            longitude = savedInstanceState.getDouble("longitude", 0);
            temLocalizacao = savedInstanceState.getBoolean("temLocalizacao", false);
        }

        txtPassos = findViewById(R.id.txtPassos);
        txtEstatisticas = findViewById(R.id.txtEstatisticas);
        txtLocalizacao = findViewById(R.id.txtLocalizacao);
        txtHistorico = findViewById(R.id.txtHistorico);
        grafico = findViewById(R.id.grafico);

        TextView txtMeta = findViewById(R.id.txtMeta);
        txtMeta.setText(getString(
                R.string.meta_diaria,
                getResources().getInteger(R.integer.meta_diaria)
        ));

        Button btnLocalizacao =
                findViewById(R.id.btnLocalizacao);

        Button btnSalvar =
                findViewById(R.id.btnSalvar);

        Button btnZerar =
                findViewById(R.id.btnZerar);

        banco = new BancoDados(this);

        preferencias = getSharedPreferences("contador", MODE_PRIVATE);
        contador = new ContadorDiario(
                preferencias.getString("dia", null),
                preferencias.getInt("base", 0),
                preferencias.getInt("ultimoTotal", -1)
        );

        gerenciador = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);

        if (gerenciador != null) {
            sensorPassos = gerenciador.getDefaultSensor(
                    Sensor.TYPE_STEP_COUNTER
            );

            if (sensorPassos == null) {
                acelerometro = gerenciador.getDefaultSensor(
                        Sensor.TYPE_ACCELEROMETER
                );
            }
        }

        localizador = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        TextView txtFonteSensor = findViewById(R.id.txtFonteSensor);

        if (sensorPassos != null) {
            txtFonteSensor.setText(R.string.fonte_sensor_passos);
        } else if (acelerometro != null) {
            txtFonteSensor.setText(R.string.fonte_acelerometro);
        } else {
            txtFonteSensor.setText(R.string.sensor_nao_encontrado);
        }

        btnLocalizacao.setOnClickListener(v ->
                obterLocalizacao()
        );

        btnSalvar.setOnClickListener(v ->
                salvarRegistro()
        );

        btnZerar.setOnClickListener(v ->
                confirmarZerar()
        );

        atualizarTela();

        mostrarHistorico();

        if (sensorPassos != null &&
                android.os.Build.VERSION.SDK_INT >= 29 &&
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

    private void iniciarSensor() {

        if (gerenciador == null) {
            return;
        }

        if (sensorPassos == null) {
            if (acelerometro != null) {
                gerenciador.registerListener(
                        this,
                        acelerometro,
                        SensorManager.SENSOR_DELAY_GAME
                );
            }
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

    @Override
    public void onSensorChanged(SensorEvent evento) {

        if (evento.sensor.getType() ==
                Sensor.TYPE_STEP_COUNTER) {

            contador.registrarTotal((int) evento.values[0], hoje());
            atualizarTela();

        } else if (evento.sensor.getType() ==
                Sensor.TYPE_ACCELEROMETER) {

            boolean deuPasso = detector.processar(
                    evento.values[0],
                    evento.values[1],
                    evento.values[2],
                    evento.timestamp
            );

            if (deuPasso) {
                contador.somarPasso(hoje());
                atualizarTela();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int precisao) {
    }

    private void atualizarTela() {

        int passos = contador.getPassos(hoje());

        txtPassos.setText(
                NumberFormat.getIntegerInstance().format(passos)
        );

        txtEstatisticas.setText(getString(
                R.string.estatisticas,
                String.format(Locale.getDefault(), "%.2f", Estimativas.distanciaKm(passos)),
                Estimativas.calorias(passos)
        ));

        grafico.atualizar(passos);
    }

    private void confirmarZerar() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.zerar_titulo)
                .setMessage(R.string.zerar_mensagem)
                .setPositiveButton(R.string.botao_zerar, (dialogo, botao) -> {
                    contador.zerar(hoje());
                    salvarContador();
                    atualizarTela();
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void salvarContador() {

        preferencias.edit()
                .putString("dia", contador.getDia())
                .putInt("base", contador.getBase())
                .putInt("ultimoTotal", contador.getUltimoTotal())
                .apply();
    }

    private String hoje() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private String formatarCoordenada(double valor) {
        return String.format(Locale.getDefault(), "%.5f", valor);
    }

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
                    R.string.localizacao_indisponivel,
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
                        R.string.ative_localizacao,
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            // Mostra a última posição conhecida enquanto o GPS não responde
            Location ultima = localizador.getLastKnownLocation(
                    provedor
            );

            if (ultima != null) {
                receberLocalizacao(ultima);
            } else {
                txtLocalizacao.setText(R.string.buscando_localizacao);
            }

            localizador.requestLocationUpdates(
                    provedor,
                    1000,
                    10,
                    this
            );

        } catch (SecurityException | IllegalArgumentException erro) {

            Toast.makeText(
                    this,
                    R.string.erro_localizacao,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    public void onLocationChanged(Location local) {

        receberLocalizacao(local);

        if (localizador != null) {
            localizador.removeUpdates(this);
        }
    }

    private void receberLocalizacao(Location local) {

        latitude = local.getLatitude();
        longitude = local.getLongitude();

        temLocalizacao = true;

        txtLocalizacao.setText(getString(
                R.string.coordenadas,
                formatarCoordenada(latitude),
                formatarCoordenada(longitude)
        ));
    }

    private void salvarRegistro() {

        if (sensorPassos == null && acelerometro == null) {

            Toast.makeText(
                    this,
                    R.string.sensor_nao_encontrado,
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (!temLocalizacao) {

            Toast.makeText(
                    this,
                    R.string.obtenha_localizacao,
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        SimpleDateFormat formato =
                new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                );

        String data = formato.format(new Date());

        boolean salvou = banco.salvar(
                contador.getPassos(hoje()),
                latitude,
                longitude,
                data
        );

        if (salvou) {

            Toast.makeText(
                    this,
                    R.string.registro_salvo,
                    Toast.LENGTH_SHORT
            ).show();

            mostrarHistorico();

        } else {

            Toast.makeText(
                    this,
                    R.string.erro_salvar,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

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

                    historico.append(getString(
                            R.string.item_historico,
                            quantidade,
                            formatarCoordenada(lat),
                            formatarCoordenada(lon),
                            data
                    )).append("\n\n");

                } while (cursor.moveToNext());

                txtHistorico.setText(historico.toString());

            } else {

                txtHistorico.setText(R.string.historico_vazio);
            }

        } finally {
            cursor.close();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);

        estado.putDouble("latitude", latitude);
        estado.putDouble("longitude", longitude);
        estado.putBoolean("temLocalizacao", temLocalizacao);
    }

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
                        R.string.permissao_passos_negada,
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
                        R.string.permissao_localizacao_negada,
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        atualizarTela();

        if (android.os.Build.VERSION.SDK_INT < 29 ||
                checkSelfPermission(
                        Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED) {

            iniciarSensor();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        salvarContador();

        if (gerenciador != null) {
            gerenciador.unregisterListener(this);
        }

        if (localizador != null) {
            localizador.removeUpdates(this);
        }
    }

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
