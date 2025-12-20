package Models.Ergast;

public class Location {
    public String lat;
    public String long_;
    public String locality;
    public String country;

    @Override
    public String toString() {
        return """
        %s, %s (Lat: %s, Long: %s)
        """.formatted(locality, country, lat, long_);
    }
}
