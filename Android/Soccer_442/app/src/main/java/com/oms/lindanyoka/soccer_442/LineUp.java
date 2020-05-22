package com.oms.lindanyoka.soccer_442;

import java.util.ArrayList;

/**
 * Created by linda.nyoka on 2015-03-23.
 */
public class LineUp {

    public ArrayList<LineUpPlayer> home;
    public ArrayList<LineUpPlayer> away;

    public LineUp( ArrayList<LineUpPlayer> home, ArrayList<LineUpPlayer> away)
    {
        this.home = home;
        this.away = away;
    }
}
