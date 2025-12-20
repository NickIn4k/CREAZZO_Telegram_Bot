package Services;

import Models.Ergast.MRData;
import com.google.gson.Gson;
import org.example.ApiClient;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ErgastApi {
    private static final String f1_url = "https://ergast.com/api/f1";

    private static final String BASE_URL = "https://ergast.com/api/f1";
    private final ApiClient apiClient = new ApiClient();
    private final Gson gson = new Gson();

    public ErgastApi() {}

    private String getString(String endpoint) {
        String url = BASE_URL + endpoint + ".json"; // tutti gli URL finiscono con .json
        HttpRequest req = apiClient.getRequest(url, "GET", null, null);
        return apiClient.sendRequest(req).body();
    }

    // Prossima gara
    public MRData getNextRace() {
        String json = getString("/current/next");
        return gson.fromJson(json, MRData.class);
    }

    // Ultima gara
    public MRData getLastRace() {
        String json = getString("/current/last");
        return gson.fromJson(json, MRData.class);
    }

    // Classifica piloti aggiornata
    public MRData getDriverStandings() {
        String json = getString("/current/driverStandings");
        return gson.fromJson(json, MRData.class);
    }

    // Classifica costruttori aggiornata
    public MRData getConstructorStandings() {
        String json = getString("/current/constructorStandings");
        return gson.fromJson(json, MRData.class);
    }

    // Calendario stagione per anno specifico
    public MRData getCalendar(int year) {
        String newUrl = (year == java.time.Year.now().getValue()) ? "/current" : "/" + year;
        String json = getString(newUrl);
        return gson.fromJson(json, MRData.class);
    }

    // Dati pilota specifico
    public MRData getDriver(String driverId) {
        String json = getString("/drivers/" + driverId);
        return gson.fromJson(json, MRData.class);
    }

    // Lista team
    public MRData getConstructors() {
        String json = getString("/constructors");
        return gson.fromJson(json, MRData.class);
    }

    // Classifica ultima gara
    public MRData getLastRaceResults() {
        String json = getString("/current/last/results");
        return gson.fromJson(json, MRData.class);
    }

    // Qualifiche ultima gara
    public MRData getLastQualifying() {
        String json = getString("/current/last/qualifying");
        return gson.fromJson(json, MRData.class);
    }

}
