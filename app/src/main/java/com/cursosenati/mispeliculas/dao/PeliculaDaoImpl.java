package com.cursosenati.mispeliculas.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cursosenati.mispeliculas.DatabaseHelper;
import com.cursosenati.mispeliculas.Pelicula;
import com.cursosenati.mispeliculas.R;

import java.util.ArrayList;

public class PeliculaDaoImpl implements PeliculaDao {

    private final DatabaseHelper dbHelper;
    private static final String TABLE_PELICULAS = "peliculas";

    public PeliculaDaoImpl(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public long insertar(Pelicula pelicula, String usuarioEmail) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("titulo", pelicula.getTitulo());
        valores.put("anio", pelicula.getAnio());
        valores.put("genero", pelicula.getGenero());
        valores.put("calificacion", pelicula.getCalificacion());
        valores.put("imagen_uri", pelicula.getImagenUri());
        valores.put("usuario_email", usuarioEmail != null ? usuarioEmail.trim().toLowerCase() : "invitado@netflix.com");
        valores.put("video_url", pelicula.getVideoUrl() != null && !pelicula.getVideoUrl().isEmpty() ? pelicula.getVideoUrl() : DatabaseHelper.DEFAULT_VIDEO_URL);

        long resultado = db.insert(TABLE_PELICULAS, null, valores);
        db.close();
        return resultado;
    }

    @Override
    public ArrayList<Pelicula> obtenerPorUsuario(String usuarioEmail) {
        ArrayList<Pelicula> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String email = usuarioEmail != null ? usuarioEmail.trim().toLowerCase() : "invitado@netflix.com";
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_PELICULAS + " WHERE LOWER(usuario_email) = ? OR usuario_email IS NULL ORDER BY id DESC",
                new String[]{email}
        );

        if (cursor.moveToFirst()) {
            do {
                lista.add(extraerPeliculaDeCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lista;
    }

    @Override
    public ArrayList<Pelicula> obtenerTodas() {
        ArrayList<Pelicula> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PELICULAS + " ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                lista.add(extraerPeliculaDeCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lista;
    }

    @Override
    public ArrayList<Pelicula> buscarPorTitulo(String query, String usuarioEmail) {
        ArrayList<Pelicula> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String busqueda = "%" + (query != null ? query.trim().toLowerCase() : "") + "%";
        
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_PELICULAS + " WHERE LOWER(titulo) LIKE ? ORDER BY id DESC",
                new String[]{busqueda}
        );

        if (cursor.moveToFirst()) {
            do {
                lista.add(extraerPeliculaDeCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lista;
    }

    @Override
    public int actualizar(Pelicula pelicula) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("titulo", pelicula.getTitulo());
        valores.put("anio", pelicula.getAnio());
        valores.put("genero", pelicula.getGenero());
        valores.put("calificacion", pelicula.getCalificacion());
        valores.put("imagen_uri", pelicula.getImagenUri());
        valores.put("video_url", pelicula.getVideoUrl());

        int resultado = db.update(TABLE_PELICULAS, valores, "id = ?", new String[]{String.valueOf(pelicula.getId())});
        db.close();
        return resultado;
    }

    @Override
    public int eliminar(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int resultado = db.delete(TABLE_PELICULAS, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return resultado;
    }

    private Pelicula extraerPeliculaDeCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        String titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"));
        int anio = cursor.getInt(cursor.getColumnIndexOrThrow("anio"));
        String genero = cursor.getString(cursor.getColumnIndexOrThrow("genero"));
        int calificacion = cursor.getInt(cursor.getColumnIndexOrThrow("calificacion"));
        int idxImagenUri = cursor.getColumnIndex("imagen_uri");
        String imagenUri = (idxImagenUri != -1) ? cursor.getString(idxImagenUri) : null;
        int idxVideoUrl = cursor.getColumnIndex("video_url");
        String videoUrl = (idxVideoUrl != -1) ? cursor.getString(idxVideoUrl) : DatabaseHelper.DEFAULT_VIDEO_URL;

        return new Pelicula(
                id,
                titulo,
                anio,
                genero,
                calificacion,
                R.drawable.poster_generico,
                imagenUri,
                videoUrl
        );
    }
}