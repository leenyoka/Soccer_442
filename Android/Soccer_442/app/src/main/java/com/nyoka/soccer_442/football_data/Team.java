package com.nyoka.soccer_442.football_data;

import java.util.List;

public class Team {
    public int id;
    public String name;
    public String shortName;
    public String tla;
    public String crest;
    private Coach coach;
    public List<Player> lineup;
    public List<Player> bench;
    public Statistics statistics;
}
