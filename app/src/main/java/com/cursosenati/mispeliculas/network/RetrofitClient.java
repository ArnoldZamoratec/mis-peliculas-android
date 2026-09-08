package com.cursosenati.mispeliculas.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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