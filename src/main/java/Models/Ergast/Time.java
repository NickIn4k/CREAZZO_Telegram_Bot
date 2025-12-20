package Models.Ergast;

public class Time {
    public String millis;
    public String time;

    @Override
    public String toString() {
        return """
        Tempo: %s (%s ms)
        """.formatted(time, millis);
    }
}
