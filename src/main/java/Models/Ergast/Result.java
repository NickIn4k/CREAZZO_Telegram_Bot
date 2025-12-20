package Models.Ergast;

public class Result {
    public int position;
    public String positionText;
    public Driver driver;
    public Constructor constructor;
    public int grid;
    public int laps;
    public String status;
    public Time time;
    public FastestLap fastestLap;

    @Override
    public String toString() {
        return """
        Posizione: %s
        Pilota: %s %s
        Team: %s
        Punti: %s
        Griglia: %s
        Giri completati: %s
        Stato: %s
        Tempo: %s
        Veloce giro: %s
        """.formatted(
                position,
                driver != null ? driver.givenName : "N/A",
                driver != null ? driver.familyName : "",
                constructor != null ? constructor.name : "N/A",
                grid,
                laps,
                status,
                time != null ? time.toString() : "N/A",
                fastestLap != null ? fastestLap.toString() : "N/A"
        );
    }
}
