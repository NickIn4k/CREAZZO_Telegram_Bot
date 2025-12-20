package Models.Ergast;

import java.util.List;

public class Race {
    public String season;
    public String round;
    public String url;
    public String raceName;
    public String date;
    public String time;
    public Circuit Circuit;
    public List<Result> Results;

    // Utilizzo di formatted() => gestione simile ai prepared statements!
    @Override
    public String toString() {
        return """
           🏁 Gara: %s
           📍 Circuito: %s
           📅 Data: %s
           ⏰ Ora: %s
           """.formatted(raceName, Circuit.circuitName, date, time);
    }
}
