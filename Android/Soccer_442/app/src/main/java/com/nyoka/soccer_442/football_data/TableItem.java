package com.nyoka.soccer_442.football_data;

public class TableItem {
    public int position;
    public Team team;
    public int playedGames;
    public Object form;  // You might want to create a class for Form if it has a specific structure
    public int won;
    public int draw;
    public int lost;
    public int points;
    public int goalsFor;
    public int goalsAgainst;
    public int goalDifference;

    public TableItem(String value) {
        String[] values = value.split("_");

        position = Integer.parseInt(values[0]);
        team = new Team();
        team.name = values[1];
        playedGames = Integer.parseInt(values[2]);
        won = Integer.parseInt(values[3]);
        goalDifference = Integer.parseInt(values[4]);
        points = Integer.parseInt(values[5]);
        //League = values[6];
        //Movement = Integer.parseInt(values[7]);
    }
    public String Compress()
    {
        String value = String.valueOf(position);
        value += "_" + String.valueOf(team.name);
        value += "_" + String.valueOf(playedGames);
        value += "_" + String.valueOf(won);
        value += "_" + String.valueOf(goalDifference);
        value += "_" + String.valueOf(points);
       // value += "_" + String.valueOf(League);
        //value += "_" + String.valueOf(Movement);
        return value;
    }
}
