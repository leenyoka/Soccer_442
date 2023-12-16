package com.nyoka.soccer_442;

/**
 * Created by linda.nyoka on 2015-02-22.
 */
public class LogItem {
    public int Position;
    public String TeamName ;
    public int GamesPlayed ;
    public int GamesWon ;
    public int GoalDifference ;
    public int Points;
    public String League;
    public int Movement;

    public LogItem(int position,String name, int played, int won, int goalDifference, int points, String league)
    {
        Position = position;
        TeamName = name;
        GamesPlayed = played;
        GamesWon = won;
        GoalDifference = goalDifference;
        Points = points;
        League = league;
    }
    public LogItem(String value) {
        String[] values = value.split("_");

            Position = Integer.parseInt(values[0]);
            TeamName = values[1];
            GamesPlayed = Integer.parseInt(values[2]);
            GamesWon = Integer.parseInt(values[3]);
            GoalDifference = Integer.parseInt(values[4]);
            Points = Integer.parseInt(values[5]);
            League = values[6];
            Movement = Integer.parseInt(values[7]);
    }
    public String Compress()
    {
        String value = String.valueOf(Position);
        value += "_" + String.valueOf(TeamName);
        value += "_" + String.valueOf(GamesPlayed);
        value += "_" + String.valueOf(GamesWon);
        value += "_" + String.valueOf(GoalDifference);
        value += "_" + String.valueOf(Points);
        value += "_" + String.valueOf(League);
        value += "_" + String.valueOf(Movement);
        return value;
    }
}
