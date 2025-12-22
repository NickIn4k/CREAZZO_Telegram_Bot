package Models.Ergast;

import java.util.List;

public class StandingsTable {
    public String season;
    public String round;
    public List<StandingsList> StandingsLists;

    @Override
    public String toString() {
        String output = "🏆 Classifica 🏆 - Stagione " + season + ", Round " + round + "\n\n";

        if (StandingsLists != null) {
            for (StandingsList sl : StandingsLists) {
                if (sl.DriverStandings != null) {
                    for (DriverStanding ds : sl.DriverStandings) {
                        output += ds.toString() + "\n\n";
                    }
                }
                if(sl.ConstructorStandings != null) {
                    for (ConstructorStanding cs : sl.ConstructorStandings) {
                        output += cs.toString() + "\n\n";
                    }
                }
            }
        }
        return output;
    }
}
