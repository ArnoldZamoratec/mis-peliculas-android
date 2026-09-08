package com.cursosenati.mispeliculas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "peliculas.db";
    private static final int DATABASE_VERSION = 5;
    private static final String TABLE_PELICULAS = "peliculas";
    private static final String TABLE_USUARIOS = "usuarios";
    public static final String DEFAULT_VIDEO_URL = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String crearTablaPeliculas =
                "CREATE TABLE " + TABLE_PELICULAS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "titulo TEXT NOT NULL, " +
                        "anio INTEGER NOT NULL, " +
                        "genero TEXT NOT NULL, " +
                        "calificacion INTEGER NOT NULL, " +
                        "imagen_uri TEXT, " +
                        "usuario_email TEXT, " +
                        "video_url TEXT" +
                        ")";

        db.execSQL(crearTablaPeliculas);

        String crearTablaUsuarios =
                "CREATE TABLE " + TABLE_USUARIOS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "email TEXT UNIQUE NOT NULL, " +
                        "password TEXT NOT NULL, " +
                        "nombre TEXT" +
                        ")";

        db.execSQL(crearTablaUsuarios);

        // Pre-cargar catálogo inicial estilo Netflix para el usuario principal
        insertarPeliculaInicial(db, "Inception (Origen)", 2010, "Sci-Fi", 5, "usuario@netflix.com", DEFAULT_VIDEO_URL);
        insertarPeliculaInicial(db, "The Dark Knight", 2008, "Acción", 5, "usuario@netflix.com", DEFAULT_VIDEO_URL);
        insertarPeliculaInicial(db, "Interstellar", 2014, "Sci-Fi", 5, "usuario@netflix.com", DEFAULT_VIDEO_URL);
        insertarPeliculaInicial(db, "Avatar: El Sentido del Agua", 2022, "Aventura", 4, "usuario@netflix.com", DEFAULT_VIDEO_URL);
        insertarPeliculaInicial(db, "Spider-Man: Across the Spider-Verse", 2023, "Animación", 5, "usuario@netflix.com", DEFAULT_VIDEO_URL);

        // Pre-cargar usuarios por defecto
        insertarUsuarioInicial(db, "usuario@netflix.com", "123456", "Usuario Netflix");
        insertarUsuarioInicial(db, "admin@netflix.com", "123456", "Administrador");
    }

    private void insertarUsuarioInicial(SQLiteDatabase db, String email, String password, String nombre) {
        ContentValues valores = new ContentValues();
        valores.put("email", email);
        valores.put("password", password);
        valores.put("nombre", nombre);
        db.insert(TABLE_USUARIOS, null, valores);
    }

    private void insertarPeliculaInicial(SQLiteDatabase db, String titulo, int anio, String genero, int calificacion, String usuarioEmail, String videoUrl) {
        ContentValues valores = new ContentValues();
        valores.put("titulo", titulo);
        valores.put("anio", anio);
        valores.put("genero", genero);
        valores.put("calificacion", calificacion);
        valores.put("imagen_uri", (String) null);
        valores.put("usuario_email", usuarioEmail.toLowerCase());
        valores.put("video_url", videoUrl);
        db.insert(TABLE_PELICULAS, null, valores);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_PELICULAS + " ADD COLUMN imagen_uri TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USUARIOS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "nombre TEXT" +
                    ")");

            ContentValues valores = new ContentValues();
            valores.put("email", "usuario@netflix.com");
            valores.put("password", "123456");
            valores.put("nombre", "Usuario Netflix");
            db.insertWithOnConflict(TABLE_USUARIOS, null, valores, SQLiteDatabase.CONFLICT_IGNORE);

            ContentValues valoresAdmin = new ContentValues();
            valoresAdmin.put("email", "admin@netflix.com");
            valoresAdmin.put("password", "123456");
            valoresAdmin.put("nombre", "Administrador");
            db.insertWithOnConflict(TABLE_USUARIOS, null, valoresAdmin, SQLiteDatabase.CONFLICT_IGNORE);
        }
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_PELICULAS + " ADD COLUMN usuario_email TEXT");
                db.execSQL("UPDATE " + TABLE_PELICULAS + " SET usuario_email = 'usuario@netflix.com' WHERE usuario_email IS NULL");
            } catch (Exception ignored) {
            }
        }
        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_PELICULAS + " ADD COLUMN video_url TEXT");
                db.execSQL("UPDATE " + TABLE_PELICULAS + " SET video_url = '" + DEFAULT_VIDEO_URL + "' WHERE video_url IS NULL OR video_url = ''");
            } catch (Exception ignored) {
            }
        }
    }

    // =========================
    // USUARIOS (AUTH)
    // =========================

    public boolean validarUsuario(String email, String password) {
        if (email == null || password == null) return false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USUARIOS + " WHERE LOWER(email) = LOWER(?) AND password = ?",
                new String[]{email.trim(), password.trim()}
        );
        boolean esValido = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return esValido;
    }

    public boolean existeUsuario(String email) {
        if (email == null) return false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USUARIOS + " WHERE LOWER(email) = LOWER(?)",
                new String[]{email.trim()}
        );
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return existe;
    }

    public boolean registrarUsuario(String email, String password, String nombre) {
        if (email == null || password == null) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("email", email.trim().toLowerCase());
        valores.put("password", password.trim());
        valores.put("nombre", nombre != null ? nombre.trim() : "Usuario");

        long resultado = db.insert(TABLE_USUARIOS, null, valores);
        db.close();
        return resultado != -1;
    }

    // =========================
    // CREATE
    // =========================

    public long insertarPelicula(Pelicula pelicula, String usuarioEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("titulo", pelicula.getTitulo());
        valores.put("anio", pelicula.getAnio());
        valores.put("genero", pelicula.getGenero());
        valores.put("calificacion", pelicula.getCalificacion());
        valores.put("imagen_uri", pelicula.getImagenUri());
        valores.put("usuario_email", usuarioEmail != null ? usuarioEmail.trim().toLowerCase() : "invitado@netflix.com");
        valores.put("video_url", pelicula.getVideoUrl() != null && !pelicula.getVideoUrl().isEmpty() ? pelicula.getVideoUrl() : DEFAULT_VIDEO_URL);

        long resultado = db.insert(TABLE_PELICULAS, null, valores);
        db.close();
        return resultado;
    }

    public long insertarPelicula(Pelicula pelicula) {
        return insertarPelicula(pelicula, "usuario@netflix.com");
    }

    // =========================
    // READ
    // =========================

    public ArrayList<Pelicula> obtenerPeliculas(String usuarioEmail) {
        ArrayList<Pelicula> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String email = usuarioEmail != null ? usuarioEmail.trim().toLowerCase() : "invitado@netflix.com";
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_PELICULAS + " WHERE LOWER(usuario_email) = ? OR usuario_email IS NULL ORDER BY id DESC",
                new String[]{email}
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"));
                int anio = cursor.getInt(cursor.getColumnIndexOrThrow("anio"));
                String genero = cursor.getString(cursor.getColumnIndexOrThrow("genero"));
                int calificacion = cursor.getInt(cursor.getColumnIndexOrThrow("calificacion"));
                int idxImagenUri = cursor.getColumnIndex("imagen_uri");
                String imagenUri = (idxImagenUri != -1) ? cursor.getString(idxImagenUri) : null;
                int idxVideoUrl = cursor.getColumnIndex("video_url");
                String videoUrl = (idxVideoUrl != -1) ? cursor.getString(idxVideoUrl) : DEFAULT_VIDEO_URL;

                Pelicula pelicula = new Pelicula(
                        id,
                        titulo,
                        anio,
                        genero,
                        calificacion,
                        R.drawable.poster_generico,
                        imagenUri,
                        videoUrl
                );

                lista.add(pelicula);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lista;
    }

    public ArrayList<Pelicula> obtenerPeliculas() {
        return obtenerPeliculas("usuario@netflix.com");
    }

    public ArrayList<Pelicula> obtenerTodasLasPeliculasParaEspectador() {
        ArrayList<Pelicula> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PELICULAS + " ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"));
                int anio = cursor.getInt(cursor.getColumnIndexOrThrow("anio"));
                String genero = cursor.getString(cursor.getColumnIndexOrThrow("genero"));
                int calificacion = cursor.getInt(cursor.getColumnIndexOrThrow("calificacion"));
                int idxImagenUri = cursor.getColumnIndex("imagen_uri");
                String imagenUri = (idxImagenUri != -1) ? cursor.getString(idxImagenUri) : null;
                int idxVideoUrl = cursor.getColumnIndex("video_url");
                String videoUrl = (idxVideoUrl != -1) ? cursor.getString(idxVideoUrl) : DEFAULT_VIDEO_URL;

                Pelicula pelicula = new Pelicula(
                        id,
                        titulo,
                        anio,
                        genero,
                        calificacion,
                        R.drawable.poster_generico,
                        imagenUri,
                        videoUrl
                );

                lista.add(pelicula);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lista;
    }

    // =========================
    // UPDATE
    // =========================

    public int actualizarPelicula(Pelicula pelicula) {
        SQLiteDatabase db = this.getWritableDatabase();
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

    // =========================
    // DELETE
    // =========================

    public int eliminarPelicula(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int resultado = db.delete(TABLE_PELICULAS, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return resultado;
    }
}