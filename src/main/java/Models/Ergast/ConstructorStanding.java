package Models.Ergast;

public class ConstructorStanding {
    public String position;
    public String points;
    public String wins;
    public Constructor constructor; // La tua classe Constructor

    @Override
    public String toString() {
        return """
        Team: %s
        Posizione: %s
        Punti: %s
        Vittorie: %s
        """.formatted(constructor.name, position, points, wins);
    }
}
