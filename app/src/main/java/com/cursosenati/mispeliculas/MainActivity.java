package com.cursosenati.mispeliculas;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cursosenati.mispeliculas.controller.PeliculaController;
import com.cursosenati.mispeliculas.network.MovieApiModel;
import com.cursosenati.mispeliculas.network.MovieApiService;
import com.cursosenati.mispeliculas.network.RetrofitClient;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements PeliculaFragment.OnPeliculaGuardadaListener {

    private RecyclerView recyclerPeliculas;
    private FloatingActionButton btnAgregar;
    private FrameLayout contenedorFragment;
    private ImageButton btnBuscarHeader;
    private ImageView imgAvatarProfile;
    private LinearLayout layoutEmptyState;
    private ChipGroup chipGroupGeneros;
    private TextInputEditText etBuscarPelicula;

    private ArrayList<Pelicula> listaPeliculasCompleta;
    private ArrayList<Pelicula> listaPeliculasFiltrada;
    private PeliculaAdapter adapter;
    private PeliculaController peliculaController;

    private String generoFiltroActual = "Todas";
    private String textoBusquedaActual = "";
    private String usuarioActual;
    private boolean esAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usuarioActual = LoginActivity.getUsuarioActual(this);
        esAdmin = usuarioActual != null && (usuarioActual.equalsIgnoreCase("admin@netflix.com") || usuarioActual.toLowerCase().contains("admin"));

        View headerBar = findViewById(R.id.headerBar);
        if (headerBar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(headerBar, (v, insets) -> {
                Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                v.setPadding(
                        v.getPaddingLeft(),
                        statusBarInsets.top + 8,
                        v.getPaddingRight(),
                        v.getPaddingBottom()
                );
                return insets;
            });
        }

        recyclerPeliculas = findViewById(R.id.recyclerPeliculas);
        btnAgregar = findViewById(R.id.btnAgregar);
        contenedorFragment = findViewById(R.id.contenedorFragment);
        View btnLogout = findViewById(R.id.btnLogout);
        btnBuscarHeader = findViewById(R.id.btnBuscarHeader);
        imgAvatarProfile = findViewById(R.id.imgAvatarProfile);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        chipGroupGeneros = findViewById(R.id.chipGroupGeneros);
        etBuscarPelicula = findViewById(R.id.etBuscarPelicula);

        peliculaController = new PeliculaController(this);

        // Control de permisos según el rol en el Controller
        if (esAdmin) {
            btnAgregar.setVisibility(View.VISIBLE);
            listaPeliculasCompleta = peliculaController.obtenerTodasPeliculas();
        } else {
            btnAgregar.setVisibility(View.GONE);
            listaPeliculasCompleta = peliculaController.obtenerTodasPeliculas();
        }

        listaPeliculasFiltrada = new ArrayList<>(listaPeliculasCompleta);

        recyclerPeliculas.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PeliculaAdapter(listaPeliculasFiltrada, esAdmin, this::onPeliculaSeleccionada);
        recyclerPeliculas.setAdapter(adapter);

        actualizarVistaVacia();

        // Búsqueda de películas en tiempo real
        if (etBuscarPelicula != null) {
            etBuscarPelicula.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    textoBusquedaActual = s != null ? s.toString().trim() : "";
                    aplicarFiltro();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        btnAgregar.setOnClickListener(v -> {
            if (esAdmin) {
                abrirFragment(PeliculaFragment.nuevaPelicula());
            } else {
                Toast.makeText(this, "Solo el administrador puede agregar películas", Toast.LENGTH_SHORT).show();
            }
        });

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> confirmarCerrarSesion());
        }

        if (imgAvatarProfile != null) {
            imgAvatarProfile.setOnClickListener(v -> {
                String rolText = esAdmin ? " (Administrador)" : " (Lector / Espectador)";
                Toast.makeText(this, "Usuario activo: " + usuarioActual + rolText, Toast.LENGTH_LONG).show();
            });
        }

        if (btnBuscarHeader != null) {
            btnBuscarHeader.setOnClickListener(v -> cargarPeliculasDesdeApi());
        }

        configurarFiltrosGeneros();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (contenedorFragment.getVisibility() == View.VISIBLE) {
                    cerrarFragment();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void onPeliculaSeleccionada(Pelicula pelicula) {
        if (esAdmin) {
            mostrarOpcionesAdmin(pelicula);
        } else {
            reproducirPelicula(pelicula);
        }
    }

    private void reproducirPelicula(Pelicula pelicula) {
        Intent intent = new Intent(this, PlayerActivity.class);
        String streamUrl = (pelicula != null && pelicula.getVideoUrl() != null && !pelicula.getVideoUrl().isEmpty())
                ? pelicula.getVideoUrl()
                : DatabaseHelper.DEFAULT_VIDEO_URL;
        intent.putExtra(PlayerActivity.EXTRA_VIDEO_URL, streamUrl);
        intent.putExtra(PlayerActivity.EXTRA_MOVIE_TITLE, pelicula != null ? pelicula.getTitulo() : "APUPE Cinema");
        startActivity(intent);
    }

    private void cargarPeliculasDesdeApi() {
        Toast.makeText(this, "Conectando con API REST (Retrofit)...", Toast.LENGTH_SHORT).show();
        MovieApiService apiService = RetrofitClient.getApiService();
        apiService.obtenerPeliculasApi().enqueue(new Callback<List<MovieApiModel>>() {
            @Override
            public void onResponse(Call<List<MovieApiModel>> call, Response<List<MovieApiModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (MovieApiModel apiMovie : response.body()) {
                        Pelicula pelicula = new Pelicula(
                                0,
                                apiMovie.getTitulo(),
                                apiMovie.getAnio(),
                                apiMovie.getGenero(),
                                apiMovie.getCalificacion(),
                                R.drawable.poster_generico,
                                apiMovie.getImagenUri()
                        );
                        peliculaController.agregarPelicula(pelicula, usuarioActual);
                    }
                    listaPeliculasCompleta = peliculaController.obtenerTodasPeliculas();
                    aplicarFiltro();
                    Toast.makeText(MainActivity.this, "¡Películas sincronizadas desde API REST!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Retrofit API lista. Estado: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MovieApiModel>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Servicio Retrofit activo (" + t.getMessage() + ")", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarCerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Deseas salir de la cuenta (" + usuarioActual + ")?")
                .setPositiveButton("Sí, Salir", (dialog, which) -> LoginActivity.cerrarSesion(this))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void configurarFiltrosGeneros() {
        if (chipGroupGeneros == null) return;

        chipGroupGeneros.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                generoFiltroActual = "Todas";
            } else {
                int chipId = checkedIds.get(0);
                Chip chip = findViewById(chipId);
                if (chip != null) {
                    generoFiltroActual = chip.getText().toString();
                } else {
                    generoFiltroActual = "Todas";
                }
            }
            aplicarFiltro();
        });
    }

    private void aplicarFiltro() {
        listaPeliculasFiltrada.clear();
        for (Pelicula p : listaPeliculasCompleta) {
            boolean coincideGenero = generoFiltroActual.equalsIgnoreCase("Todas") || generoFiltroActual.isEmpty() ||
                    (p.getGenero() != null && p.getGenero().toLowerCase().contains(generoFiltroActual.toLowerCase()));

            boolean coincideBusqueda = textoBusquedaActual.isEmpty() ||
                    (p.getTitulo() != null && p.getTitulo().toLowerCase().contains(textoBusquedaActual.toLowerCase()));

            if (coincideGenero && coincideBusqueda) {
                listaPeliculasFiltrada.add(p);
            }
        }
        adapter.actualizarLista(listaPeliculasFiltrada);
        actualizarVistaVacia();
    }

    private void actualizarVistaVacia() {
        if (listaPeliculasFiltrada.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerPeliculas.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerPeliculas.setVisibility(View.VISIBLE);
        }
    }

    private void abrirFragment(PeliculaFragment fragment) {
        contenedorFragment.setVisibility(View.VISIBLE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragment, fragment)
                .commit();
    }

    private void cerrarFragment() {
        contenedorFragment.setVisibility(View.GONE);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contenedorFragment);
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    @Override
    public void onPeliculaGuardada(Pelicula pelicula) {
        if (pelicula == null) {
            cerrarFragment();
            return;
        }

        if (pelicula.getId() > 0) {
            boolean actualizado = peliculaController.actualizarPelicula(pelicula);
            if (actualizado) {
                int posCompleta = buscarPosicionEnLista(listaPeliculasCompleta, pelicula.getId());
                if (posCompleta != -1) {
                    listaPeliculasCompleta.set(posCompleta, pelicula);
                }
                aplicarFiltro();
                cerrarFragment();
                Toast.makeText(this, "Película actualizada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        long resultado = peliculaController.agregarPelicula(pelicula, usuarioActual);
        if (resultado != -1) {
            pelicula.setId((int) resultado);
            listaPeliculasCompleta.add(0, pelicula);
            aplicarFiltro();
            recyclerPeliculas.scrollToPosition(0);
            cerrarFragment();
            Toast.makeText(this, "Película añadida al catálogo", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al guardar película", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarOpcionesAdmin(Pelicula pelicula) {
        String[] opciones = {"▶ Reproducir Película", "Editar", "Eliminar", "Cancelar"};

        new AlertDialog.Builder(this)
                .setTitle(pelicula.getTitulo())
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            reproducirPelicula(pelicula);
                            break;
                        case 1:
                            abrirFragment(PeliculaFragment.editarPelicula(pelicula));
                            break;
                        case 2:
                            confirmarEliminacion(pelicula);
                            break;
                        default:
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    private void confirmarEliminacion(Pelicula pelicula) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar película")
                .setMessage("¿Deseas quitar \"" + pelicula.getTitulo() + "\" de la lista?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarPelicula(pelicula))
                .show();
    }

    private void eliminarPelicula(Pelicula pelicula) {
        boolean eliminado = peliculaController.eliminarPelicula(pelicula.getId());
        if (eliminado) {
            int posCompleta = buscarPosicionEnLista(listaPeliculasCompleta, pelicula.getId());
            if (posCompleta != -1) {
                listaPeliculasCompleta.remove(posCompleta);
            }
            aplicarFiltro();
            Toast.makeText(this, "Película eliminada", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No se pudo eliminar", Toast.LENGTH_SHORT).show();
        }
    }

    private int buscarPosicionEnLista(ArrayList<Pelicula> lista, int id) {
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }
}