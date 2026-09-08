# 🎬 APUPE Cinema - Aplicación Android

**APUPE Cinema** es una aplicación móvil nativa para Android desarrollada en **Java**, diseñada bajo la arquitectura **MVC (Modelo-Vista-Controlador)**, el patrón de diseño **DAO (Data Access Object)** y los principios fundamentales de la **Programación Orientada a Objetos (POO)**.

---

## 🏛️ Arquitectura del Software (MVC + DAO)

El proyecto está estructurado limpiamente dividiendo responsabilidades en capas independientes:

```
com.cursosenati.mispeliculas
├── 📂 model              -> Clases POJO / DTOs (Modelos)
│   ├── Pelicula.java
│   └── Usuario.java
├── 📂 dao                -> Objeto de Acceso a Datos (Persistencia SQLite)
│   ├── PeliculaDao.java (Interfaz)
│   ├── PeliculaDaoImpl.java (Implementación)
│   ├── UsuarioDao.java (Interfaz)
│   └── UsuarioDaoImpl.java (Implementación)
├── 📂 controller         -> Controladores de Lógica de Negocio
│   ├── PeliculaController.java
│   └── UsuarioController.java
├── 📂 network            -> Capa de Red (Consumo API REST con Retrofit)
│   ├── MovieApiModel.java
│   ├── MovieApiService.java
│   └── RetrofitClient.java
└── 📂 ui / views         -> Interfaz de Usuario y Actividades
    ├── WelcomeActivity.java (Transición de Video)
    ├── LoginActivity.java (Autenticación y Registro)
    ├── MainActivity.java (Catálogo Principal y Búsqueda)
    ├── PlayerActivity.java (Reproductor Media3 ExoPlayer)
    ├── PeliculaFragment.java (Formulario Modal con Spinner)
    └── PeliculaAdapter.java (Adaptador RecyclerView)
```

---

## 🧩 Principios de Programación Orientada a Objetos (POO)

1. **Encapsulamiento**:
   - Los atributos de las entidades `Pelicula` y `Usuario` son privados (`private`) y se acceden a través de métodos de acceso públicos (`getters` y `setters`).
2. **Abstracción e Interfaces**:
   - Las interfaces `PeliculaDao` y `UsuarioDao` definen los contratos de las operaciones CRUD, ocultando los detalles específicos del motor SQL.
3. **Herencia**:
   - Las actividades heredan de `AppCompatActivity` y los fragmentos de `androidx.fragment.app.Fragment`.
4. **Sobrecarga de Métodos y Constructores**:
   - La clase `Pelicula` cuenta con múltiples constructores sobrecargados para instanciar objetos con o sin imágenes y URLs de video.

---

## 🚀 Funcionalidades Principales

### 1. 🎞️ Video de Transición de Entrada
- Al abrir la app, `WelcomeActivity` reproduce el video de entrada `apu_transition.mp4` en pantalla completa.
- Al terminar la animación (`setOnCompletionListener`), transiciona automáticamente a la pantalla de **Login**.

### 2. 🔐 Autenticación y Control de Roles (Admin vs Espectador)
- **Modo Administrador (`admin@netflix.com`)**:
  - Posee permisos totales para **Agregar (+)**, **Editar** y **Eliminar** películas.
- **Modo Espectador / Usuario Regular**:
  - Acceso en modo **solo lectura**. El botón de agregar y los iconos de edición se ocultan.
  - Al presionar cualquier película, abre directamente el reproductor de video.

### 3. 🔍 Búsqueda de Películas en Tiempo Real
- En `MainActivity`, el campo de texto `etBuscarPelicula` implementa un `TextWatcher`.
- Filtra instantáneamente la lista de películas por título conforme el usuario escribe.

### 4. 📑 Selector de Géneros con `Spinner`
- En el formulario (`PeliculaFragment`), la selección del género se realiza mediante un componente `Spinner` desplegable (`spinnerGenero`) cargado desde `arrays.xml` (`Acción`, `Sci-Fi`, `Drama`, `Animación`, etc.).

### 5. 🌐 Consumo de API REST con Retrofit
- La capa de red implementa **Retrofit 2** con conversor **Gson**.
- Al presionar el botón de sincronizar, se realiza una petición HTTP GET asíncrona (`obtenerPeliculasApi()`) para poblar el catálogo desde un servicio web.

### 6. 🍿 Reproductor de Video HLS (Media3 ExoPlayer)
- `PlayerActivity` utiliza `androidx.media3.ui.PlayerView` para transmitir listas de reproducción HLS (`.m3u8`) o MP4 con controles de reproducción.

---

## 💻 Muestras de Código y Sintaxis

### 1. Interfaz DAO (`PeliculaDao.java`)
```java
public interface PeliculaDao {
    long insertar(Pelicula pelicula, String usuarioEmail);
    ArrayList<Pelicula> obtenerPorUsuario(String usuarioEmail);
    ArrayList<Pelicula> obtenerTodas();
    ArrayList<Pelicula> buscarPorTitulo(String query, String usuarioEmail);
    int actualizar(Pelicula pelicula);
    int eliminar(int id);
}
```

### 2. Controlador (`PeliculaController.java`)
```java
public class PeliculaController {
    private final PeliculaDao peliculaDao;

    public PeliculaController(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        this.peliculaDao = new PeliculaDaoImpl(dbHelper);
    }

    public long agregarPelicula(Pelicula pelicula, String usuarioEmail) {
        return peliculaDao.insertar(pelicula, usuarioEmail);
    }

    public ArrayList<Pelicula> obtenerTodasPeliculas() {
        return peliculaDao.obtenerTodas();
    }
}
```

### 3. Cliente Retrofit (`RetrofitClient.java`)
```java
public class RetrofitClient {
    private static final String BASE_URL = "https://raw.githubusercontent.com/ArnoldZamoratec/mis-peliculas-android/main/";
    private static Retrofit retrofit = null;

    public static MovieApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(MovieApiService.class);
    }
}
```

---

## 🛠️ Tecnologías y Librerías Utilizadas
- **Lenguaje**: Java 11 / Android SDK 37 (target 35)
- **Base de Datos**: SQLite (`SQLiteOpenHelper`)
- **Red / HTTP**: Retrofit 2 + Gson Converter
- **Video / Multimedia**: AndroidX Media3 ExoPlayer (`media3-exoplayer`, `media3-ui`, `media3-exoplayer-hls`)
- **UI & Layouts**: Material Components, ConstraintLayout, RecyclerView, CardView, Spinner
