package Models.Ergast;

public class DriverStanding {
    public String position;
    public String points;
    public String wins;
    public Driver driver; // La tua classe Driver

    @Override
    public String toString() {
        return """
        Pilota: %s %s
        Posizione: %s
        Punti: %s
        Vittorie: %s
        """.formatted(driver.givenName, driver.familyName, position, points, wins);
    }
}
