package com.alura.literalura.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ApiConsumidorService {
    private static final String BASE_URL = "https://gutendex.com/books/";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String buscarLibroPorTitulo(String titulo) throws Exception {
        Request request = new Request.Builder()
                .url(BASE_URL + "?search=" + titulo.replace(" ", "+"))
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}