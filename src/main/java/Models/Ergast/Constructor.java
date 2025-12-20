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
           🏎 Team: %s
           🇨🇳 Nazionalità: %s
           """.formatted(name, nationality);
    }
}
