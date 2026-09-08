package com.cursosenati.mispeliculas.network;

import com.google.gson.annotations.SerializedName;

public class MovieApiModel {

    @SerializedName("id")
    private int id;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("anio")
    private int anio;

    @SerializedName("genero")
    private String genero;

    @SerializedName("calificacion")
    private int calificacion;

    @SerializedName("imagen_uri")
    private String imagenUri;

    public MovieApiModel() {
    }

    public MovieApiModel(int id, String titulo, int anio, String genero, int calificacion, String imagenUri) {
        this.id = id;
        this.titulo = titulo;
        this.anio = anio;
        this.genero = genero;
        this.calificacion = calificacion;
        this.imagenUri = imagenUri;
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getAnio() {
        return anio;
    }

    public String getGenero() {
        return genero;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public String getImagenUri() {
        return imagenUri;
    }
}