package com.cursosenati.mispeliculas.controller;

import android.content.Context;

import com.cursosenati.mispeliculas.DatabaseHelper;
import com.cursosenati.mispeliculas.dao.UsuarioDao;
import com.cursosenati.mispeliculas.dao.UsuarioDaoImpl;
import com.cursosenati.mispeliculas.model.Usuario;

public class UsuarioController {

    private final UsuarioDao usuarioDao;

    public UsuarioController(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        this.usuarioDao = new UsuarioDaoImpl(dbHelper);
    }

    public boolean iniciarSesion(String email, String password) {
        return usuarioDao.validar(email, password);
    }

    public boolean usuarioExiste(String email) {
        return usuarioDao.existe(email);
    }

    public boolean registrarNuevoUsuario(String email, String password, String nombre) {
        Usuario usuario = new Usuario(email, password, nombre);
        return usuarioDao.registrar(usuario);
    }
}