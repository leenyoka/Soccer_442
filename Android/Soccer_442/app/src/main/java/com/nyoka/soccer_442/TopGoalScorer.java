package com.nyoka.soccer_442;

/**
 * Created by linda.nyoka on 2015-02-22.
 */
public class TopGoalScorer {

    public String Player ;
    public String Team  ;
    public int Goals  ;

    public TopGoalScorer(String player, String team, int goals)
    {
        Player = player;
        Team = team;
        Goals = goals;
    }
    public TopGoalScorer(String value)
    {
        String[] values = value.split("_");
        Player = values[0];
        Team = values[1];
        Goals = Integer.parseInt(values[2]);
    }
    public String Compress()
    {
        String value = String.valueOf(Player);
        value += "_" + String.valueOf(Team);
        value += "_" + String.valueOf(Goals);
        return value;
    }
}
