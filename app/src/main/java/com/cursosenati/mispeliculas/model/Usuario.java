package com.cursosenati.mispeliculas.model;

public class Usuario {
    private int id;
    private String email;
    private String password;
    private String nombre;

    public Usuario() {
    }

    public Usuario(int id, String email, String password, String nombre) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nombre = nombre;
    }

    public Usuario(String email, String password, String nombre) {
        this(0, email, password, nombre);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}