package com.cursosenati.mispeliculas.controller;

import android.content.Context;

import com.cursosenati.mispeliculas.DatabaseHelper;
import com.cursosenati.mispeliculas.Pelicula;
import com.cursosenati.mispeliculas.dao.PeliculaDao;
import com.cursosenati.mispeliculas.dao.PeliculaDaoImpl;

import java.util.ArrayList;

public class PeliculaController {

    private final PeliculaDao peliculaDao;

    public PeliculaController(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        this.peliculaDao = new PeliculaDaoImpl(dbHelper);
    }

    public long agregarPelicula(Pelicula pelicula, String usuarioEmail) {
        return peliculaDao.insertar(pelicula, usuarioEmail);
    }

    public ArrayList<Pelicula> obtenerPeliculasUsuario(String usuarioEmail) {
        return peliculaDao.obtenerPorUsuario(usuarioEmail);
    }

    public ArrayList<Pelicula> obtenerTodasPeliculas() {
        return peliculaDao.obtenerTodas();
    }

    public ArrayList<Pelicula> buscarPeliculas(String query, String usuarioEmail) {
        return peliculaDao.buscarPorTitulo(query, usuarioEmail);
    }

    public boolean actualizarPelicula(Pelicula pelicula) {
        return peliculaDao.actualizar(pelicula) > 0;
    }

    public boolean eliminarPelicula(int id) {
        return peliculaDao.eliminar(id) > 0;
    }
}