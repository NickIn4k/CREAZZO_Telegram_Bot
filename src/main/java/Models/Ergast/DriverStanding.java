package Models.Ergast;

public class DriverStanding {
    public String position;
    public String points;
    public String wins;
    public Driver Driver;

    @Override
    public String toString() {

        return """
        👤 Pilota: %s %s
        🏁 Posizione: %s
        🏆 Punti: %s
        🎯 Vittorie: %s
        """.formatted(
                Driver != null ? Driver.givenName : "N/A",
                Driver != null ? Driver.familyName : "N/A",
                position != null ? position : "N/A",
                points != null ? points : "0",
                wins != null ? wins : "0"
        );
    }
}
