package com.cursosenati.mispeliculas;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_URL = "extra_video_url";
    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";
    public static final String DEFAULT_STREAM_URL = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";

    private ExoPlayer player;
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playerView = findViewById(R.id.playerView);
        TextView txtTituloPlayer = findViewById(R.id.txtTituloPlayer);
        ImageButton btnVolverPlayer = findViewById(R.id.btnVolverPlayer);

        String videoUrl = getIntent().getStringExtra(EXTRA_VIDEO_URL);
        if (videoUrl == null || videoUrl.isEmpty()) {
            videoUrl = DEFAULT_STREAM_URL;
        }

        String titulo = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        if (titulo == null || titulo.isEmpty()) {
            titulo = "Big Buck Bunny";
        }

        if (txtTituloPlayer != null) {
            txtTituloPlayer.setText(titulo);
        }

        if (btnVolverPlayer != null) {
            btnVolverPlayer.setOnClickListener(v -> finish());
        }

        inicializarReproductor(videoUrl);
    }

    private void inicializarReproductor(String videoUrl) {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}