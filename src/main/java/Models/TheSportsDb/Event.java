package Models.TheSportsDb;

public class Event {
    public String idEvent;
    public String strEvent;
    public String strLeague;
    public String strSeason;

    public String dateEvent;
    public String strTime;

    public String strVenue;
    public String strCity;
    public String strCountry;

    public String strPoster;
    public String strThumb;
    public String strBanner;

    @Override
    public String toString() {
        return """
        🏁 %s
        📅 %s
        ⏰ %s
        📍 %s
        """.formatted(
                strEvent != null ? strEvent : "Evento",
                dateEvent != null ? dateEvent : "N/A",
                strTime != null ? strTime : "N/A",
                strVenue != null ? strVenue : "N/A"
        );
    }
}
