package com.nyoka.soccer_442;

/**
 * Created by linda.nyoka on 2015-03-23.
 */
public class TeamStats {
    public String Shots;
    public String Fouls;
    public String Corners;
    public String Offsides;
    public String Yellow;
    public String Red;

    public TeamStats(String shots, String fouls, String corners, String offsides, String yellow, String red)
    {
        this.Shots = shots;
        this.Fouls = fouls;
        this.Corners = corners;
        this.Offsides = offsides;
        this.Yellow = yellow;
        this.Red = red;
    }
}
