package com.nyoka.soccer_442.football_data;

import com.nyoka.soccer_442.Comment;
import com.nyoka.soccer_442.Fixture;
import com.nyoka.soccer_442.GameStats;
import com.nyoka.soccer_442.LineUp;
import com.nyoka.soccer_442.Live;
import com.nyoka.soccer_442.LogItem;
import com.nyoka.soccer_442.NewsItem;
import com.nyoka.soccer_442.Result;
import com.nyoka.soccer_442.TopGoalScorer;
import com.nyoka.soccer_442.WebDog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.google.gson.Gson;

public class FootballData {

    AppConfigFootballData _uriProvider = new AppConfigFootballData();
    private String competitionCode;


    public Response GetCompetitions() {
        try {
            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetCompetitionsUri());
            Gson gson = new Gson();
            Response standingsResponse = gson.fromJson(value, Response.class);
            return standingsResponse;
        } catch (Exception ex) {

            return null;
        }
    }
    public static String GetCompetitionCode(String teamName) {
        switch (teamName) {
            case "Campeonato Brasileiro Série A":
                return "BSA";
            case "Championship":
                return "ELC";
            case "Premier League":
                return "PL";
            case "UEFA Champions League":
                return "CL";
            case "European Championship":
                return "EC";
            case "Ligue 1":
                return "FL1";
            case "Bundesliga":
                return "BL1";
            case "Serie A":
                return "SA";
            case "SerieA":
                return "SA";
            case "Eredivisie":
                return "DED";
            case "Primeira Liga":
                return "PPL";
            case "Copa Libertadores":
                return "CLI";
            case "Primera Division":
                return "PD";
            case "FIFA World Cup":
                return "WC";
            default:
                return "Code not found";
        }
    }

    public Live GetMatchDetails(Live game, String competition, String matchId) {
        try {
            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetMatchDetails(matchId));
            return Commentry(game, value);
        } catch (Exception ex) {

            return null;
        }
    }
    public FootballMatch GetMatchDetails( String competition, String matchId) {
        try {
            String competitionCode =  GetCompetitionCode(competition);

            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetMatchDetails(matchId));
            Gson gson = new Gson();
            FootballMatch standingsResponse = gson.fromJson(value, FootballMatch.class);
            return standingsResponse;
        } catch (Exception ex) {

            return null;
        }
    }

    public MatchResponse GetFixture(String competition) {
        try {
            String competitionCode =  GetCompetitionCode(competition);

            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetFixtureUri(competitionCode));
            Gson gson = new Gson();
            MatchResponse standingsResponse = gson.fromJson(value, MatchResponse.class);
            return standingsResponse;
        } catch (Exception ex) {

            return null;
        }
    }
    public MatchResponse GetStats(String matchId)
    {
        try {
            String competitionCode =  GetCompetitionCode(matchId);

            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetStatsUri(matchId));
            Gson gson = new Gson();
            MatchResponse standingsResponse = gson.fromJson(value, MatchResponse.class);
            return standingsResponse;
        } catch (Exception ex) {

            return null;
        }
    }
    public MatchResponse GetLineUp(String matchId)
    {
        try {
            String competitionCode =  GetCompetitionCode(matchId);

            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetLineUpUri(matchId));
            Gson gson = new Gson();
            MatchResponse standingsResponse = gson.fromJson(value, MatchResponse.class);
            return standingsResponse;
        } catch (Exception ex) {

            return null;
        }
    }
    public MatchResponse GetResults(String competition) {
        try {
            String competitionCode =  GetCompetitionCode(competition);

            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetResultsUri(competitionCode));
            Gson gson = new Gson();
            MatchResponse standingsResponse = gson.fromJson(value, MatchResponse.class);
            return standingsResponse;
        } catch (Exception ex) {

            return null;
        }

    }

    public ScorerResponse GetScorers(String competition) {
        try {
            String competitionCode =  GetCompetitionCode(competition);

            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetScorersUr(competitionCode));
            Gson gson = new Gson();
            ScorerResponse standingsResponse = gson.fromJson(value, ScorerResponse.class);
            return standingsResponse;
        } catch (Exception ex) {

            return null;
        }
    }

    public StandingsResponse GetLog(String competition) {
        try {
            String competitionCode =  GetCompetitionCode(competition);

            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetLogUri(competitionCode));
            Gson gson = new Gson();
            StandingsResponse standingsResponse = gson.fromJson(value, StandingsResponse.class);
            return standingsResponse;
        } catch (Exception ex) {

            return null;
        }
    }

    public MatchResponse GetLive(String competition) {
        try {

            String competitionCode =  GetCompetitionCode(competition);
            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetLiveUri(competitionCode));
            Gson gson = new Gson();
            MatchResponse standingsResponse = gson.fromJson(value, MatchResponse.class);
            return standingsResponse;
        } catch (Exception ex) {

            return null;
        }
    }

    private GameStats GetStatsFromFile(String html)
    {
        return  null;
    }

    private ArrayList<LogItem> Logs(ArrayList<ArrayList<String>> pieces, Competition competition) {
        return  null;
    }

    private boolean IsInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private ArrayList<Result> Results(String htmlTable, String mobi) {

        return  null;

    }
    private LineUp LineUp(String htmlTable)
    {
        return  null;


    }
    private ArrayList<Fixture> Fixtures(String htmlTable) {
        return  null;
    }


    private Date ParseDate(String value) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date start = simpleDateFormat.parse(value);
            //calendar.setTime(start); // comment out to test current time
            return start;
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean IsDate(String value) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date start = simpleDateFormat.parse(value);
            return true;
        } catch (Exception ex) {
            return false;
        }


    }

    private ArrayList<Live> Live(String content) {

        return  null;
    }
    private ArrayList<NewsItem> NewsList(String content) {
        return  null;
    }

    private String NewsArticle(String content)
    {
        return  null;
    }
    private Live Commentry(Live game, String content) {

        return  null;
    }
    private Result Commentry(Result game, String content) {

        return  null;
    }

    private Live GetScorers(Live game, ArrayList<String> rawData) {
        return  null;
    }
    private Result GetScorers(Result game, ArrayList<String> rawData) {
        return  null;
    }


    private ArrayList<Comment> Comments(String rawData) {
        return  null;
    }

    private ArrayList<Live> Live(ArrayList<String> rawLive) {
        return  null;
    }

    private ArrayList<TopGoalScorer> Scorers(String htmlTable) {
       return  null;
    }


}
