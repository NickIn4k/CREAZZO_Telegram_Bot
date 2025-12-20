package Services;

import Models.PexelsResponse;
import Models.PexelsVideoResponse;
import com.google.gson.Gson;
import org.example.ApiClient;
import org.example.StandardConfig;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class PexelsApi {
    private static final String foto_url = "https://api.pexels.com/v1";
    private static final String video_url = "https://api.pexels.com/videos";

    private final Gson gson = new Gson();
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
        String url = videoUrl + "/search?query=" + encodedQuery + "&per_page=15";

        // Richiesta tramite ApiClient
        HttpRequest req = apiClient.getRequest(url, "GET", null, apiKey);
        return apiClient.sendRequest(req).body();
    }

    public String getPhotoUrl(String query){
        String json = searchPhoto(query);
        PexelsResponse response = gson.fromJson(json, PexelsResponse.class);

        if(response.photos == null || response.photos.isEmpty())
            return null;

        // Dalla lista di foto estrai una foto, poi il suo src, quindi il suo medium url
        Random rnd = new Random();
        int index = rnd.nextInt(response.photos.size());
        return response.photos.get(index).src.medium;
    }

    public String getVideoUrl(String query) {
        String json = searchVideo(query);
        PexelsVideoResponse response = gson.fromJson(json, PexelsVideoResponse.class);

        if (response.videos == null || response.videos.isEmpty())
            return null;

        // Come per le foto
        Random rnd = new Random();
        int index = rnd.nextInt(response.videos.size());
        PexelsVideoResponse.Video video = response.videos.get(index);

        if (video.video_files == null || video.video_files.isEmpty())
            return null;

        // Prendi il primo file video disponibile
        return video.video_files.getFirst().link;
    }
}
