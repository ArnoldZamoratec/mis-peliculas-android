package com.cursosenati.mispeliculas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cursosenati.mispeliculas.controller.UsuarioController;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "NetflixAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    public static final String KEY_CURRENT_USER_EMAIL = "current_user_email";

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private CheckBox cbRecuerdame;

    private UsuarioController usuarioController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usuarioController = new UsuarioController(this);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLogout = getIntent() != null && getIntent().getBooleanExtra("EXTRA_LOGOUT", false);

        if (isLogout) {
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).commit();
        } else {
            boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
            if (isLoggedIn) {
                irAMainActivity();
                return;
            }
        }

        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        cbRecuerdame = findViewById(R.id.cbRecuerdame);
        TextView txtSubscribete = findViewById(R.id.txtSubscribete);

        String emailGuardado = prefs.getString(KEY_SAVED_EMAIL, "usuario@netflix.com");
        etEmail.setText(emailGuardado);

        btnLogin.setOnClickListener(v -> intentarLogin());

        if (txtSubscribete != null) {
            txtSubscribete.setOnClickListener(v -> mostrarDialogoRegistro());
        }
    }

    private void intentarLogin() {
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Ingresa tu correo o usuario");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Ingresa un correo electrónico válido");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Ingresa tu contraseña");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 4) {
            tilPassword.setError("La contraseña debe tener al menos 4 caracteres");
            etPassword.requestFocus();
            return;
        }

        boolean esValido = usuarioController.iniciarSesion(email, password);
        if (esValido) {
            boolean recordar = cbRecuerdame != null && cbRecuerdame.isChecked();
            guardarSesion(true, recordar ? email : null, email);
            Toast.makeText(this, "¡Bienvenido a APUPE!", Toast.LENGTH_SHORT).show();
            irAMainActivity();
        } else {
            if (!usuarioController.usuarioExiste(email)) {
                tilEmail.setError("Usuario no encontrado");
                Toast.makeText(this, "El correo no está registrado. Toca Suscríbete para registrarte.", Toast.LENGTH_LONG).show();
            } else {
                tilPassword.setError("Contraseña incorrecta");
            }
        }
    }

    private void mostrarDialogoRegistro() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_registro, null);
        TextInputEditText etRegNombre = dialogView.findViewById(R.id.etRegNombre);
        TextInputEditText etRegEmail = dialogView.findViewById(R.id.etRegEmail);
        TextInputEditText etRegPassword = dialogView.findViewById(R.id.etRegPassword);

        new AlertDialog.Builder(this)
                .setTitle("Registro de Usuario")
                .setView(dialogView)
                .setPositiveButton("Registrarse", (dialog, which) -> {
                    String nombre = etRegNombre.getText() != null ? etRegNombre.getText().toString().trim() : "";
                    String email = etRegEmail.getText() != null ? etRegEmail.getText().toString().trim() : "";
                    String password = etRegPassword.getText() != null ? etRegPassword.getText().toString().trim() : "";

                    if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(password) || password.length() < 4) {
                        Toast.makeText(this, "Contraseña debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (usuarioController.usuarioExiste(email)) {
                        Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean registrado = usuarioController.registrarNuevoUsuario(email, password, nombre.isEmpty() ? "Usuario" : nombre);
                    if (registrado) {
                        etEmail.setText(email);
                        etPassword.setText(password);
                        Toast.makeText(this, "¡Cuenta creada exitosamente! Ahora puedes iniciar sesión.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarSesion(boolean isLoggedIn, String emailParaRecordar, String emailUsuarioActual) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        if (emailParaRecordar != null) {
            editor.putString(KEY_SAVED_EMAIL, emailParaRecordar);
        }
        if (emailUsuarioActual != null) {
            editor.putString(KEY_CURRENT_USER_EMAIL, emailUsuarioActual);
        }
        editor.commit();
    }

    public static String getUsuarioActual(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_CURRENT_USER_EMAIL, "usuario@netflix.com");
    }

    private void irAMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public static void cerrarSesion(AppCompatActivity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).commit();

        Intent intent = new Intent(activity, LoginActivity.class);
        intent.putExtra("EXTRA_LOGOUT", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}