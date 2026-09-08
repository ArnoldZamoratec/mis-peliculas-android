package com.cursosenati.mispeliculas.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cursosenati.mispeliculas.DatabaseHelper;
import com.cursosenati.mispeliculas.model.Usuario;

public class UsuarioDaoImpl implements UsuarioDao {

    private final DatabaseHelper dbHelper;
    private static final String TABLE_USUARIOS = "usuarios";

    public UsuarioDaoImpl(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public boolean validar(String email, String password) {
        if (email == null || password == null) return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USUARIOS + " WHERE LOWER(email) = LOWER(?) AND password = ?",
                new String[]{email.trim(), password.trim()}
        );
        boolean esValido = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return esValido;
    }

    @Override
    public boolean existe(String email) {
        if (email == null) return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USUARIOS + " WHERE LOWER(email) = LOWER(?)",
                new String[]{email.trim()}
        );
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return existe;
    }

    @Override
    public boolean registrar(Usuario usuario) {
        if (usuario == null || usuario.getEmail() == null || usuario.getPassword() == null) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("email", usuario.getEmail().trim().toLowerCase());
        valores.put("password", usuario.getPassword().trim());
        valores.put("nombre", usuario.getNombre() != null ? usuario.getNombre().trim() : "Usuario");

        long resultado = db.insert(TABLE_USUARIOS, null, valores);
        db.close();
        return resultado != -1;
    }
}