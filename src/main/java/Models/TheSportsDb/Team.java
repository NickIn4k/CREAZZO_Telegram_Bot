package Models.TheSportsDb;

public class Team {
    public String idTeam;
    public String strTeam;
    public String strLeague;
    public String strCountry;

    public String strTeamBadge;
    public String strTeamLogo;
    public String strTeamBanner;

    public String intFormedYear;
    public String strDescriptionEN;

    @Override
    public String toString() {
        return """
        🏎 %s
        🌍 %s
        📅 Fondato: %s
        """.formatted(
                strTeam != null ? strTeam : "Team",
                strCountry != null ? strCountry : "N/A",
                intFormedYear != null ? intFormedYear : "N/A"
        );
    }
}
