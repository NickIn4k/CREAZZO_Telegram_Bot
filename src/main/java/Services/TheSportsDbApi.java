package Services;

import Models.TheSportsDb.EventsResponse;
import Models.TheSportsDb.TeamsResponse;
import com.google.gson.Gson;
import org.example.ApiClient;
import org.example.StandardConfig;

import java.net.http.HttpRequest;

public class TheSportsDbApi {
    private static final String wec_url = "https://www.thesportsdb.com/api/v1/json/";
    private static final String wec_id = "4413";

    private final ApiClient apiClient = new ApiClient();
    private final Gson gson = new Gson();

    private String getString(String endpoint) {
        String url = wec_url + StandardConfig.getInstance().getProps("THESPORTSDB_API_KEY") + endpoint;
        HttpRequest req = apiClient.getRequest(url, "GET", null, null);
        return apiClient.sendRequest(req).body();
    }

    public EventsResponse getNextEvents() {
        String json = getString("/eventsnextleague.php?id=" + wec_id);
        return gson.fromJson(json, EventsResponse.class);
    }

    public EventsResponse getLastEvents() {
        String json = getString("/eventspastleague.php?id=" + wec_id);
        return gson.fromJson(json, EventsResponse.class);
    }

    public EventsResponse getSeasonEvents(String season) {
        String json = getString("/eventsseason.php?id=" + wec_id + "&s=" + season);
        return gson.fromJson(json, EventsResponse.class);
    }

    public TeamsResponse getTeams() {
        String json = getString("/lookup_all_teams.php?id=" + wec_id);
        return gson.fromJson(json, TeamsResponse.class);
    }
}
