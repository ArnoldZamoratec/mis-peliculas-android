package com.cursosenati.mispeliculas.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MovieApiService {

    @GET("peliculas")
    Call<List<MovieApiModel>> obtenerPeliculasApi();

    @GET("buscar")
    Call<List<MovieApiModel>> buscarPeliculasApi(@Query("q") String query);
}