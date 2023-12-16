package com.nyoka.soccer_442.football_data;

public class AppConfigFootballData {

    public static String LogUri = "https://api.football-data.org/v4/competitions/%s/standings";
    public static String MatchDetails = "http://api.football-data.org/v4/matches/%s";
    public static String LiveUri = "https://api.football-data.org/v4/competitions/%s/matches?status=LIVE";
    public static String FixtureUri = "https://api.football-data.org/v4/competitions/%s/matches?status=SCHEDULED";
    public static String ResultUri = "https://api.football-data.org/v4/competitions/%s/matches?status=FINISHED";
    public static String ScorersUri = "https://api.football-data.org/v4/competitions/%s/scorers";
    public static String StatsUri = "http://api.football-data.org/v4/matches/%s";
    public static String LineUpUri = "http://api.football-data.org/v4/matches/%s";

    public static  String CompetitionsUrl = "http://api.football-data.org/v4/competitions";

    public String GetMatchDetails(String matchId)
    {
        return GetUri(MatchDetails, matchId) ;
    }
    public String GetFixtureUri(String competition)
    {
        return GetUri(FixtureUri, competition);
    }
    public String GetCompetitionsUri()
    {
        return CompetitionsUrl;
    }
    public String GetResultsUri(String competition)
    {
        return GetUri(ResultUri, competition);
    }

    public String GetStatsUri(String matchId)
    {
        return String.format(StatsUri,matchId);
        //return GetUri(ResultUri2, competition);
    }
    public String GetLineUpUri(String matchId)
    {
        return String.format(LineUpUri,matchId);
        //return GetUri(ResultUri2, competition);
    }
    public String GetScorersUr(String competition)
    {
        return GetUri(ScorersUri, competition);
    }
    public String GetLogUri(String competition)
    {
        return GetUri(LogUri, competition);
    }
    public String GetLiveUri(String competition)
    {
        return GetUri(LiveUri, competition);
    }
    private String GetUri(String uri, String competition)
    {
        return String.format(uri, competition);
    }
}
