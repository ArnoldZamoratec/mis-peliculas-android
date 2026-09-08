package com.cursosenati.mispeliculas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Splash / pantalla de bienvenida que reproduce un video de transición
 * y navega automáticamente a LoginActivity al terminar (o si falla,
 * o si el usuario toca la pantalla, o tras un tiempo máximo de espera).
 */
public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";

    // Tiempo máximo de espera por si el video nunca dispara onPrepared/onCompletion
    private static final long TIEMPO_MAXIMO_ESPERA_MS = 8000L;

    private VideoView videoViewTransition;
    private boolean yaNavego = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable navegarPorTimeout = this::irALogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activarPantallaCompleta();

        setContentView(R.layout.activity_welcome);

        videoViewTransition = findViewById(R.id.videoViewTransition);
        View rootLayout = findViewById(android.R.id.content);

        reproducirVideo();

        // Tocar la pantalla -> Login (salto manual)
        if (rootLayout != null) {
            rootLayout.setOnClickListener(v -> irALogin());
        }

        // Red de seguridad: si algo falla silenciosamente, no dejar al usuario atrapado
        handler.postDelayed(navegarPorTimeout, TIEMPO_MAXIMO_ESPERA_MS);
    }

    /**
     * Pantalla completa usando la API moderna (WindowCompat),
     * reemplaza el uso de FEATURE_NO_TITLE + FLAG_FULLSCREEN, que está obsoleto.
     */
    private void activarPantallaCompleta() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());

        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
    }

    private void reproducirVideo() {

        if (videoViewTransition == null) {
            irALogin();
            return;
        }

        try {
            Uri videoUri = Uri.parse(
                    "android.resource://" + getPackageName() + "/" + R.raw.apu_transition
            );

            videoViewTransition.setVideoURI(videoUri);

            videoViewTransition.setOnPreparedListener(mp -> {
                mp.setLooping(false); // no repetir
                videoViewTransition.start();
            });

            // Si el video termina -> Login
            videoViewTransition.setOnCompletionListener(mp -> irALogin());

            // Si el video falla (formato no soportado, archivo corrupto, etc.) -> Login
            videoViewTransition.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Error reproduciendo video de transición: what=" + what + " extra=" + extra);
                irALogin();
                return true; // evitamos que el sistema muestre su propio diálogo de error
            });

        } catch (Exception e) {
            Log.e(TAG, "Excepción al preparar el video de transición", e);
            irALogin();
        }
    }

    private synchronized void irALogin() {

        if (yaNavego) {
            return;
        }
        yaNavego = true;

        handler.removeCallbacks(navegarPorTimeout);

        if (videoViewTransition != null) {
            videoViewTransition.stopPlayback();
        }

        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (videoViewTransition != null && !videoViewTransition.isPlaying() && !yaNavego) {
            videoViewTransition.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (videoViewTransition != null && videoViewTransition.isPlaying()) {
            videoViewTransition.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);

        if (videoViewTransition != null) {
            videoViewTransition.stopPlayback();
            videoViewTransition.setOnPreparedListener(null);
            videoViewTransition.setOnCompletionListener(null);
            videoViewTransition.setOnErrorListener(null);
        }
    }
}