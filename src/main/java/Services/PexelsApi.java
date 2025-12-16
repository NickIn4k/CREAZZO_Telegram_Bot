package Services;

import org.example.ApiClient;
import org.example.StandardConfig;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

public class PexelsApi {
    private static final String foto_url = "https://api.pexels.com/v1";
    private static final String video_url = "https://api.pexels.com/videos";

    private final ApiClient apiClient =  new ApiClient();

    public String searchPhoto(String query){
        return getString(query, foto_url);
    }

    public String searchVideo(String query){
        return getString(query, video_url);
    }

    private String getString(String query, String videoUrl) {
        // Evita errori con char speciali
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        // Dati richiesta HTTP GET
        String apiKey = StandardConfig.getInstance().getProps("PEXELS_API_KEY");
        String url = videoUrl + "/search?query=" + encodedQuery + "&per_page=1";

        // Richiesta tramite ApiClient
        HttpRequest req = apiClient.getRequest(url, "GET", null, apiKey);
        return apiClient.sendRequest(req).body();
    }
}
