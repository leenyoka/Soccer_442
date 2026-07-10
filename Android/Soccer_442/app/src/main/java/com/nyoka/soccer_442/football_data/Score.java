package com.nyoka.soccer_442.football_data;

public class Score {
    public String winner;
    public String duration;
    public FullTime fullTime;
    public HalfTime halfTime;
    // Null unless the match went to a penalty shootout - fullTime stays the regular/extra-time
    // score (which is a draw when it gets here), so these are the only place the actual
    // shootout outcome is recorded.
    public Integer penaltyHome;
    public Integer penaltyAway;

    // Getters and setters
}