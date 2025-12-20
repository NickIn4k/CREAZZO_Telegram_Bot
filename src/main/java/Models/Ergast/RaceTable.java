package Models.Ergast;

import java.util.List;

public class RaceTable {
    public String season;
    public String round;
    public List<Race> Races;

    @Override
    public String toString() {
        String output = """
        🏁 Calendario/Stagione: %s
        """.formatted(season);

        for (Race race : Races) {
            output += "\n\n" + (race != null ? race.toString() : "N/A");
        }

        return output;
    }
}
