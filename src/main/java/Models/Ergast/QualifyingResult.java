package Models.Ergast;

public class QualifyingResult {
    public String position;
    public Driver driver;
    public Constructor constructor;
    public String q1;
    public String q2;
    public String q3;

    @Override
    public String toString() {
        return """
        Posizione: %s
        Pilota: %s %s
        Team: %s
        Q1: %s, Q2: %s, Q3: %s
        """.formatted(position, driver.givenName, driver.familyName, constructor.name, q1, q2, q3);
    }
}
