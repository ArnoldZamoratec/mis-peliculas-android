package com.cursosenati.mispeliculas;

public class Pelicula {

    private int id;
    private String titulo;
    private int anio;
    private String genero;
    private int calificacion;
    private int imagen;
    private String imagenUri;
    private String videoUrl;

    public Pelicula(
            int id,
            String titulo,
            int anio,
            String genero,
            int calificacion,
            int imagen) {

        this(id, titulo, anio, genero, calificacion, imagen, null, null);
    }

    public Pelicula(
            int id,
            String titulo,
            int anio,
            String genero,
            int calificacion,
            int imagen,
            String imagenUri) {

        this(id, titulo, anio, genero, calificacion, imagen, imagenUri, null);
    }

    public Pelicula(
            int id,
            String titulo,
            int anio,
            String genero,
            int calificacion,
            int imagen,
            String imagenUri,
            String videoUrl) {

        this.id = id;
        this.titulo = titulo;
        this.anio = anio;
        this.genero = genero;
        this.calificacion = calificacion;
        this.imagen = imagen;
        this.imagenUri = imagenUri;
        this.videoUrl = videoUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(int calificacion) {
        this.calificacion = calificacion;
    }

    public int getImagen() {
        return imagen;
    }

    public void setImagen(int imagen) {
        this.imagen = imagen;
    }

    public String getImagenUri() {
        return imagenUri;
    }

    public void setImagenUri(String imagenUri) {
        this.imagenUri = imagenUri;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}