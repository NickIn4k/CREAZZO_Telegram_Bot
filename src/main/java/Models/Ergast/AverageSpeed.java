package Models.Ergast;

public class AverageSpeed {
    public String units;
    public String speed;

    @Override
    public String toString() {
        return """
        Velocità media: %s %s
        """.formatted(speed, units);
    }
}
