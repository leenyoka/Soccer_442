package com.oms.lindanyoka.soccer_442;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-22.
 */
public class Result implements Serializable {
    public String HomeTeamName ;
    public String AwayTeamName ;
    public int HomeTeamScore ;
    public int AwayTeamScore ;
    public String Date ;
    public List<String> HomeTeamGoalScorers ;
    public List<String> AwayTeamGoalScorers ;
    public String matchId;
    public ArrayList<Comment> Commentry;

    public Result(String home, String away, int homeScore, int awayScore,
                  String date, String homeScorers, String awayScorers)
    {
        HomeTeamName = home;
        AwayTeamName = away;
        HomeTeamScore = homeScore;
        AwayTeamScore = awayScore;
        Date = date;
        HomeTeamGoalScorers = GetScorers(homeScorers);
        AwayTeamGoalScorers = GetScorers(awayScorers);
    }
    public Result(String value) {
        String[] values = value.split("_");

        HomeTeamName = values[0];
        AwayTeamName = values[1];
        HomeTeamScore = Integer.valueOf(values[2]);
        AwayTeamScore = Integer.valueOf(values[3]);
        Date = values[4];
    }
    public String Compress() {
        String value = String.valueOf(HomeTeamName);
        value += "_" + String.valueOf(AwayTeamName);
        value += "_" + String.valueOf(HomeTeamScore);
        value += "_" + String.valueOf(AwayTeamScore);
        value += "_" + String.valueOf(Date);
        return value;
    }
    public ArrayList<String> GetScorers(String value)
    {

        ArrayList<String> scorers =  new ArrayList<String>();
        String[] values = value.split("\\)");

        for (String myValue : values)
        if (myValue.trim().length() > 3)
            scorers.add(Trim(",",myValue) + ")");

        return scorers;

    }
    private String Trim(String value, String host)
    {
        while (host.endsWith(value))
            host = host.substring(0, host.length()-2);

        while (host.startsWith(value))
            host = host.substring(1);

        return host;
    }

}
