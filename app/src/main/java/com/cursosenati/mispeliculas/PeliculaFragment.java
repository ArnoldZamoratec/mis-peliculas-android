package com.cursosenati.mispeliculas;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class PeliculaFragment extends Fragment {

    public interface OnPeliculaGuardadaListener {
        void onPeliculaGuardada(Pelicula pelicula);
    }

    private OnPeliculaGuardadaListener listener;

    private TextInputLayout tilTitulo;
    private TextInputLayout tilAnio;
    private TextInputLayout tilVideoUrl;
    private EditText etTitulo;
    private EditText etAnio;
    private EditText etVideoUrl;
    private Spinner spinnerGenero;
    private RatingBar ratingCalificacion;
    private MaterialButton btnGuardar;
    private MaterialButton btnCancelar;
    private TextView txtTituloFormulario;
    private ImageButton btnEditarImagen;
    private ImageView imgPreview;
    private View containerSeleccionarImagen;

    private Pelicula peliculaEditar;
    private String imagenSeleccionadaUri;

    private ActivityResultLauncher<String> seleccionarImagenLauncher;

    public PeliculaFragment() {
    }

    public static PeliculaFragment nuevaPelicula() {
        return new PeliculaFragment();
    }

    public static PeliculaFragment editarPelicula(Pelicula pelicula) {
        PeliculaFragment fragment = new PeliculaFragment();
        fragment.peliculaEditar = pelicula;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        seleccionarImagenLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            requireContext().getContentResolver().takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (Exception ignored) {
                        }

                        imagenSeleccionadaUri = uri.toString();

                        if (imgPreview != null) {
                            imgPreview.setImageURI(uri);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pelicula, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilTitulo = view.findViewById(R.id.tilTitulo);
        tilAnio = view.findViewById(R.id.tilAnio);
        tilVideoUrl = view.findViewById(R.id.tilVideoUrl);

        etTitulo = view.findViewById(R.id.etTitulo);
        etAnio = view.findViewById(R.id.etAnio);
        spinnerGenero = view.findViewById(R.id.spinnerGenero);
        etVideoUrl = view.findViewById(R.id.etVideoUrl);
        ratingCalificacion = view.findViewById(R.id.ratingCalificacion);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        btnCancelar = view.findViewById(R.id.btnCancelar);
        txtTituloFormulario = view.findViewById(R.id.txtTituloFormulario);
        btnEditarImagen = view.findViewById(R.id.btnEditarImagen);
        imgPreview = view.findViewById(R.id.imgPreview);
        containerSeleccionarImagen = view.findViewById(R.id.containerSeleccionarImagen);

        View.OnClickListener seleccionarImagenListener = v -> seleccionarImagenLauncher.launch("image/*");

        if (btnEditarImagen != null) {
            btnEditarImagen.setOnClickListener(seleccionarImagenListener);
        }

        if (containerSeleccionarImagen != null) {
            containerSeleccionarImagen.setOnClickListener(seleccionarImagenListener);
        }

        if (peliculaEditar != null) {
            txtTituloFormulario.setText(R.string.titulo_editar_pelicula);
            btnGuardar.setText(R.string.btn_actualizar_pelicula);

            etTitulo.setText(peliculaEditar.getTitulo());
            etAnio.setText(String.valueOf(peliculaEditar.getAnio()));

            // Seleccionar el género en el Spinner
            if (spinnerGenero != null && peliculaEditar.getGenero() != null) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.array_generos,
                        android.R.layout.simple_spinner_item
                );
                int pos = adapter.getPosition(peliculaEditar.getGenero());
                if (pos >= 0) {
                    spinnerGenero.setSelection(pos);
                }
            }

            if (etVideoUrl != null) {
                etVideoUrl.setText(peliculaEditar.getVideoUrl() != null ? peliculaEditar.getVideoUrl() : DatabaseHelper.DEFAULT_VIDEO_URL);
            }
            ratingCalificacion.setRating(peliculaEditar.getCalificacion());

            if (peliculaEditar.getImagenUri() != null && !peliculaEditar.getImagenUri().isEmpty()) {
                imagenSeleccionadaUri = peliculaEditar.getImagenUri();
                try {
                    imgPreview.setImageURI(Uri.parse(imagenSeleccionadaUri));
                } catch (Exception ignored) {
                }
            }
        } else {
            if (etVideoUrl != null) {
                etVideoUrl.setText(DatabaseHelper.DEFAULT_VIDEO_URL);
            }
        }

        btnGuardar.setOnClickListener(v -> guardarPelicula());

        btnCancelar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPeliculaGuardada(null);
            }
        });
    }

    private void guardarPelicula() {
        if (tilTitulo != null) tilTitulo.setError(null);
        if (tilAnio != null) tilAnio.setError(null);
        if (tilVideoUrl != null) tilVideoUrl.setError(null);

        String titulo = etTitulo.getText() != null ? etTitulo.getText().toString().trim() : "";
        String anioTexto = etAnio.getText() != null ? etAnio.getText().toString().trim() : "";
        String genero = spinnerGenero != null && spinnerGenero.getSelectedItem() != null ? spinnerGenero.getSelectedItem().toString() : "Acción";
        String videoUrl = etVideoUrl != null && etVideoUrl.getText() != null ? etVideoUrl.getText().toString().trim() : DatabaseHelper.DEFAULT_VIDEO_URL;
        if (videoUrl.isEmpty()) {
            videoUrl = DatabaseHelper.DEFAULT_VIDEO_URL;
        }

        int calificacion = (int) ratingCalificacion.getRating();

        if (titulo.isEmpty()) {
            if (tilTitulo != null) tilTitulo.setError("Ingresa el título");
            else etTitulo.setError("Ingresa el título");
            etTitulo.requestFocus();
            return;
        }

        if (titulo.length() > 50) {
            if (tilTitulo != null) tilTitulo.setError("El título no puede superar los 50 caracteres");
            else etTitulo.setError("El título no puede superar los 50 caracteres");
            etTitulo.requestFocus();
            return;
        }

        if (anioTexto.isEmpty()) {
            if (tilAnio != null) tilAnio.setError("Ingresa el año");
            else etAnio.setError("Ingresa el año");
            etAnio.requestFocus();
            return;
        }

        if (calificacion < 1) {
            Toast.makeText(requireContext(), "Selecciona al menos 1 estrella de calificación", Toast.LENGTH_SHORT).show();
            return;
        }

        int anio;
        try {
            anio = Integer.parseInt(anioTexto);
        } catch (NumberFormatException e) {
            etAnio.setError("El año no es válido");
            etAnio.requestFocus();
            return;
        }

        if (peliculaEditar == null) {
            Pelicula pelicula = new Pelicula(
                    0,
                    titulo,
                    anio,
                    genero,
                    calificacion,
                    R.drawable.poster_generico,
                    imagenSeleccionadaUri,
                    videoUrl
            );

            if (listener != null) {
                listener.onPeliculaGuardada(pelicula);
            }
        } else {
            peliculaEditar.setTitulo(titulo);
            peliculaEditar.setAnio(anio);
            peliculaEditar.setGenero(genero);
            peliculaEditar.setCalificacion(calificacion);
            peliculaEditar.setImagenUri(imagenSeleccionadaUri);
            peliculaEditar.setVideoUrl(videoUrl);

            if (listener != null) {
                listener.onPeliculaGuardada(peliculaEditar);
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnPeliculaGuardadaListener) {
            listener = (OnPeliculaGuardadaListener) context;
        }
    }
}