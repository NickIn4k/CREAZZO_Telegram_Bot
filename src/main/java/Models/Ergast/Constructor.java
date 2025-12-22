package Models.Ergast;

public class Constructor {
    public String constructorId;
    public String url;
    public String name;
    public String nationality;

    // Utilizzo di formatted() => gestione simile ai prepared statements!
    @Override
    public String toString() {
        return """
           🏎 Team: <b>%s</b>
           🏳️ Nazionalità: %s
           🔗 Link: %s
           """.formatted(
                name != null ? name : "N/A",
                nationality != null ? nationality : "N/A",
                url != null ? url : "N/A"
           );

    }
}
