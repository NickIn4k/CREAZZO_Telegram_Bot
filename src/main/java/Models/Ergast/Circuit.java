package Models.Ergast;

public class Circuit {
    public String circuitId;
    public String url;
    public String circuitName;
    public Location location;

    @Override
    public String toString() {
        return """
        Circuito: %s
        ID: %s
        URL: %s
        Posizione: %s
        """.formatted(
                circuitName,
                circuitId,
                url,
                location != null ? location.toString() : "N/A"
        );
    }
}
