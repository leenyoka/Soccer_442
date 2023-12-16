package com.nyoka.soccer_442;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-22.
 */
public class Live implements Serializable {

    public String HomeTeamName;
    public String AwayTeamName ;
    public int HomeTeamScore;
    public int AwayTeamScore;
    public long MatchId;
    public String MatchStatus;
    public List<String> HomeTeamGoalScorers ;
    public List<String> AwayTeamGoalScorers;
    public ArrayList<Comment> Commentry;


    public Live(String home, String away, int homeScore, int awayScore, long matchId)
    {
        HomeTeamName = home;
        AwayTeamName = away;
        HomeTeamScore = homeScore;
        AwayTeamScore = awayScore;
        MatchId = matchId;
    }
}
