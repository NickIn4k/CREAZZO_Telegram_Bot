package Services;

import Models.BallDontLie.GamesResponse;
import Models.BallDontLie.PlayersResponse;
import Models.BallDontLie.TeamsResponse;
import com.google.gson.Gson;
import org.example.ApiClient;
import org.example.StandardConfig;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

public class BallDontLieApi {

    private static final String base_url = "https://api.balldontlie.io/v1";

    private final ApiClient apiClient = new ApiClient();
    private final Gson gson = new Gson();

    private String get(String endpoint) {
        String url = base_url + endpoint;
        HttpRequest req = apiClient.getRequest(url, "GET", null, StandardConfig.getInstance().getProps("BALLDONTLIE_API_KEY"));
        return apiClient.sendRequest(req).body();
    }

    public PlayersResponse getPlayers(int page) {
        String json = get("/players?page=" + page);
        return gson.fromJson(json, PlayersResponse.class);
    }

    public PlayersResponse searchPlayers(String name) {
        String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8);
        String json = get("/players?search=" + encoded);
        return gson.fromJson(json, PlayersResponse.class);
    }

    public TeamsResponse getTeams() {
        String json = get("/teams");
        return gson.fromJson(json, TeamsResponse.class);
    }

    public GamesResponse getGamesBySeason(int season) {
        String json = get("/games?seasons[]=" + season);
        return gson.fromJson(json, GamesResponse.class);
    }

    public GamesResponse getGamesByTeam(int teamId, int season) {
        String json = get("/games?team_ids[]=" + teamId + "&seasons[]=" + season);
        return gson.fromJson(json, GamesResponse.class);
    }
}
