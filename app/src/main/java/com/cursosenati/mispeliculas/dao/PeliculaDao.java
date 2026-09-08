package com.cursosenati.mispeliculas.dao;

import com.cursosenati.mispeliculas.Pelicula;

import java.util.ArrayList;

public interface PeliculaDao {
    long insertar(Pelicula pelicula, String usuarioEmail);
    ArrayList<Pelicula> obtenerPorUsuario(String usuarioEmail);
    ArrayList<Pelicula> obtenerTodas();
    ArrayList<Pelicula> buscarPorTitulo(String query, String usuarioEmail);
    int actualizar(Pelicula pelicula);
    int eliminar(int id);
}