package Models.Ergast;

import java.util.List;

public class DriverTable {
    public String season;
    public List<Driver> Drivers;

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("🏁 Piloti stagione " + (season != null ? season : "N/A") + "\n\n");
        if (Drivers != null) {
            for (Driver d : Drivers) {
                output.append(d.toString()).append("\n");
            }
        }
        return output.toString();
    }
}
