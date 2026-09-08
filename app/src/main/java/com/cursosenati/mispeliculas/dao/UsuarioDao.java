package com.cursosenati.mispeliculas.dao;

import com.cursosenati.mispeliculas.model.Usuario;

public interface UsuarioDao {
    boolean validar(String email, String password);
    boolean existe(String email);
    boolean registrar(Usuario usuario);
}