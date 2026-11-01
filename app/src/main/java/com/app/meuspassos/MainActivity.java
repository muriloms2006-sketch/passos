package com.app.meuspassos;

import android.app.Activity;
import android.Manifest;
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

public class MainActivity extends Activity
        implements SensorEventListener, LocationListener {

    private static final int PEDIR_ATIVIDADE = 1;
    private static final int PEDIR_LOCALIZACAO = 2;
    private static final int META = 5000;

    private SensorManager gerenciador;
    private Sensor sensorPassos;
    private BancoDados banco;
    private LocationManager localizador;

    private TextView txtPassos;
    private TextView txtLocalizacao;
    private TextView txtHistorico;
    private GraficoPassos grafico;

    private int passosIniciais = -1;
    private int passos = 0;

    private double latitude = 0;
    private double longitude = 0;
    private boolean temLocalizacao = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtPassos = findViewById(R.id.txtPassos);
        txtLocalizacao = findViewById(R.id.txtLocalizacao);
        txtHistorico = findViewById(R.id.txtHistorico);
        grafico = findViewById(R.id.grafico);

        Button btnLocalizacao =
                findViewById(R.id.btnLocalizacao);
        Button btnSalvar =
                findViewById(R.id.btnSalvar);

        banco = new BancoDados(this);

        gerenciador = (SensorManager)
                getSystemService(SENSOR_SERVICE);

        localizador = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        sensorPassos = gerenciador.getDefaultSensor(
                Sensor.TYPE_STEP_COUNTER);

        if (sensorPassos == null) {
            txtPassos.setText(
                    "Sensor de passos indisponível");
        }

        btnLocalizacao.setOnClickListener(v ->
                obterLocalizacao());

        btnSalvar.setOnClickListener(v ->
                salvarRegistro());

        atualizarTela();
        mostrarHistorico();

        if (checkSelfPermission(
                Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED) {
            iniciarSensor();
        } else {
            requestPermissions(
                    new String[]{
                            Manifest.permission.ACTIVITY_RECOGNITION
                    }, PEDIR_ATIVIDADE);
        }
    }

    private void iniciarSensor() {
        if (sensorPassos != null) {
            gerenciador.registerListener(
                    this, sensorPassos,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent evento) {
        if (evento.sensor.getType()
                == Sensor.TYPE_STEP_COUNTER) {

            int total = (int) evento.values[0];

            if (passosIniciais == -1) {
                passosIniciais = total;
            }

            passos = Math.max(0, total - passosIniciais);
            atualizarTela();
        }
    }

    private void atualizarTela() {
        txtPassos.setText("Passos: " + passos);
        grafico.atualizar(passos);
    }

    private void obterLocalizacao() {
        boolean precisa = checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        boolean aproximada = checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!precisa && !aproximada) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, PEDIR_LOCALIZACAO);
            return;
        }

        try {
            Location ultima = null;

            if (precisa && localizador.isProviderEnabled(
                    LocationManager.GPS_PROVIDER)) {
                ultima = localizador.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER);
            }

            if (ultima == null && localizador.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER)) {
                ultima = localizador.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER);
            }

            if (ultima != null) {
                receberLocalizacao(ultima);
            } else {
                localizador.requestLocationUpdates(
                        precisa ? LocationManager.GPS_PROVIDER
                                : LocationManager.NETWORK_PROVIDER,
                        0, 0, this, Looper.getMainLooper());

                Toast.makeText(this,
                        "Aguardando localização...",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (SecurityException e) {
            Toast.makeText(this,
                    "Permissão de localização necessária",
                    Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this,
                    "Provedor de localização indisponível",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location local) {
        receberLocalizacao(local);

        try {
            localizador.removeUpdates(this);
        } catch (SecurityException e) {
            // Permissão não disponível.
        }
    }

    private void receberLocalizacao(Location local) {
        latitude = local.getLatitude();
        longitude = local.getLongitude();
        temLocalizacao = true;

        txtLocalizacao.setText(
                "Latitude: " + latitude +
                        "\nLongitude: " + longitude);
    }

    private void salvarRegistro() {
        if (passosIniciais == -1 || sensorPassos == null) {
            Toast.makeText(this,
                    "Aguardando dados do sensor",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!temLocalizacao) {
            Toast.makeText(this,
                    "Obtenha a localização antes de salvar",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String data = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()).format(new Date());

        boolean salvo = banco.salvar(
                passos, latitude, longitude, data);

        if (salvo) {
            Toast.makeText(this,
                    "Registro salvo!",
                    Toast.LENGTH_SHORT).show();
            mostrarHistorico();
        } else {
            Toast.makeText(this,
                    "Erro ao salvar registro",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarHistorico() {
        Cursor cursor = banco.listar();
        StringBuilder texto = new StringBuilder();

        try {
            while (cursor.moveToNext()) {
                int quantidade = cursor.getInt(0);
                double lat = cursor.getDouble(1);
                double lon = cursor.getDouble(2);
                String data = cursor.getString(3);

                texto.append(data)
                        .append("\nPassos: ")
                        .append(quantidade)
                        .append("\nLocal: ")
                        .append(lat).append(", ").append(lon)
                        .append("\n\n");
            }
        } finally {
            cursor.close();
        }

        if (texto.length() == 0) {
            texto.append("Nenhum registro salvo");
        }

        txtHistorico.setText(texto.toString());
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == PEDIR_ATIVIDADE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarSensor();
        }

        if (requestCode == PEDIR_LOCALIZACAO
                && grantResults.length > 0) {
            boolean autorizado = false;

            for (int resultado : grantResults) {
                if (resultado == PackageManager.PERMISSION_GRANTED) {
                    autorizado = true;
                    break;
                }
            }

            if (autorizado) {
                obterLocalizacao();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sensorPassos != null &&
                checkSelfPermission(
                        Manifest.permission.ACTIVITY_RECOGNITION)
                        == PackageManager.PERMISSION_GRANTED) {
            iniciarSensor();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        gerenciador.unregisterListener(this);

        try {
            localizador.removeUpdates(this);
        } catch (SecurityException e) {
            // Sem permissão de localização.
        }
    }

    @Override
    public void onAccuracyChanged(
            Sensor sensor, int precisao) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onStatusChanged(
            String provider, int status, Bundle extras) {
    }
}