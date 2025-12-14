package org.example;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
    private final HttpClient client;
    private final Gson gson;

    public ApiClient() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    // Metodo comune per evitare ripetizioni
    private HttpResponse<String> sendRequest(HttpRequest req){
        HttpResponse<String> response;
        try{
            response = client.send(req, HttpResponse.BodyHandlers.ofString());
        }catch(IOException | InterruptedException e){
            throw new RuntimeException(e);
        }
        return response;
    }

    private HttpRequest getRequest(String url, String method, String body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json");

        if(body == null || body.isEmpty())
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        else
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));

        return builder.build();
    }
}

