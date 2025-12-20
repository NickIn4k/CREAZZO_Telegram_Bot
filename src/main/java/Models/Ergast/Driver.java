package Models.Ergast;

public class Driver {
    public String driverId;
    public String permanentNumber;
    public String code;
    public String url;
    public String givenName;
    public String familyName;
    public String dateOfBirth;
    public String nationality;

    // Utilizzo di formatted() => gestione simile ai prepared statements!
    @Override
    public String toString() {
        return """
           👤 Pilota: %s %s
           🇨🇳 Nazionalità: %s
           🏎 Numero: %s
           """.formatted(givenName, familyName, nationality, permanentNumber);
    }
}
