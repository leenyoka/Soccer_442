package com.oms.lindanyoka.soccer_442;

/**
 * Created by linda.nyoka on 2015-03-23.
 */
public class GameStats {

    public TeamStats home;
    public TeamStats away;

    public GameStats(TeamStats home, TeamStats away)
    {
        this.home = home;
        this.away = away;
    }
}
