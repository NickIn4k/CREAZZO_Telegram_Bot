package Models.Ergast;

public class ConstructorStanding {
    public String position;
    public String points;
    public String wins;
    public Constructor Constructor; // La tua classe Constructor

    @Override
    public String toString() {
        return """
    🏢 Team: %s
    🏁 Posizione: %s
    🏆 Punti: %s
    🎯 Vittorie: %s
    """.formatted(
                Constructor != null ? Constructor.name : "N/A",
                position != null ? position : "N/A",
                points != null ? points : "0",
                wins != null ? wins : "0"
        );
    }

}
