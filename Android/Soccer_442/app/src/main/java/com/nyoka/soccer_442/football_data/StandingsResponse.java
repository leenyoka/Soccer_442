package com.nyoka.soccer_442.football_data;
import java.util.List;

public class StandingsResponse {
    private int count;
    private Filters filters;
    private List<Competition> competitions;

    public Area area;
    public Competition competition;
    public Season season;
    public List<StandingsItem> standings;

    // getters and setters
}
