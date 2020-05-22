package com.oms.lindanyoka.soccer_442;

/**
 * Created by linda.nyoka on 2015-02-22.
 */
public class AppConfig {

    static String BPL = "barclays-premier-league";
    static String FA = "fa-cup";
    static String AFCON = "africa-cup-of-nations";
    static String LaLiga = "spain";
    static String Bundasliga = "germany";
    static String Absa = "absa-premiership";
    static String League1 = "france";
    static String serieA = "italy";
    static String UEFA = "uefa-champions-league";
    static String UEFA_Euro = "uefa-euro";
    static String copa = "copa-del-rey";

    public static String baseUri = "mobi.supersport.com";
    public static String LogUri = "mobi.supersport.com/football/%s/logs";
    public static String MatchDetails = "mobi.supersport.com/football/%s/match/";//"{1}/commentary";
    public static String MatchDetailsPast= "mobi.supersport.com/football/match/%s/commentary";
    public static String LiveUri = "mobi.supersport.com/football/%s";
    public static String FixtureUri = "www.supersport.com/football/%s/fixtures";
    public static String ResultUri = "www.supersport.com/football/%s/Results";
    public static String ResultUri2 = "mobi.supersport.com/football/%s/Results";
    public static String ScorersUri = "www.supersport.com/football/%s/scorers";
    public static String StatsUri = "mobi.supersport.com/football/match/%s/stats";
    public static String LineUpUri = "mobi.supersport.com/football/match/%s/lineup";
    public static String News = "mobi.supersport.com/football/%s/news";

    public String GetMatchDetails(Competition cometition, long matchId)
    {
        return GetUri(MatchDetails, cometition) + String.valueOf(matchId) + "/commentary";
    }
    public String GetFixtureUri(Competition competition)
    {
        return GetUri(FixtureUri, competition);
    }
    public String GetResultsUri(Competition competition)
    {
        return GetUri(ResultUri, competition);
    }
    public String GetResultsUri2(Competition competition)
    {
        return GetUri(ResultUri2, competition);
    }
    public String News(Competition competition)
    {
        return GetUri(News, competition);
    }
    public String NewsArticle(String uri)
    {
        return baseUri + uri;
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
    public String GetScorersUr(Competition competition)
    {
        return GetUri(ScorersUri, competition);
    }
    public String GetLogUri(Competition competition)
    {
        return GetUri(LogUri, competition);
    }
    public String GetLiveUri(Competition competition)
    {
        return GetUri(LiveUri, competition);
    }
    private String GetUri(String uri, Competition competition)
    {
        switch (competition)
        {
            case Absa: return String.format(uri, Absa);
            //case AFCON: return String.format(uri, AFCON);
            case BPL: return String.format(uri, BPL);
            case Bundesliga: return String.format(uri, Bundasliga);
            case Copa: return String.format(uri, copa);
            case FA: return String.format(uri, FA);
            case LaLiga: return String.format(uri, LaLiga);
            case League1: return String.format(uri, League1);
            case SerieA: return String.format(uri, serieA);
            case UEFA: return String.format(uri, UEFA);
            //case UEFA_Euro: return String.format(uri, UEFA_Euro);
        }
        return null;
    }
}
