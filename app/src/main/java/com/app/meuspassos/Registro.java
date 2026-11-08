package com.app.meuspassos;

public class Registro {

    private final long id;
    private final int passos;
    private final double latitude;
    private final double longitude;
    private final String data;

    public Registro(long id, int passos, double latitude, double longitude, String data) {
        this.id = id;
        this.passos = passos;
        this.latitude = latitude;
        this.longitude = longitude;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public int getPassos() {
        return passos;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getData() {
        return data;
    }
}
