package Models.Ergast;

public class Driver {
    public String driverId;
    public String permanentNumber;
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
       🏳️ Nazionalità: %s
       🏎 Numero: %s
       🎂 Data di nascita: %s
       🔗 Link: %s
       """.formatted(
                givenName != null ? givenName : "N/A",
                familyName != null ? familyName : "N/A",
                nationality != null ? nationality : "N/A",
                permanentNumber != null ? permanentNumber : "N/A",
                dateOfBirth != null ? dateOfBirth : "N/A",
                url != null ? url : "N/A"
        );
    }

}
