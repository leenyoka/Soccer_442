package com.nyoka.soccer_442;

import android.content.Context;

/**
 * Created by linda.nyoka on 2015-04-09.
 */
public class UserProfile {
    private StateGuy _stateGuy;
    private String _teamName;
    private int _refreshRate;
    private String _favourate_league;
    private boolean _off_app_notifications;

    public UserProfile(Context context)
    {
        _stateGuy = new StateGuy(context);
        _teamName = _stateGuy.getKeyString("team_name");
        _favourate_league = _stateGuy.getKeyString("favourage_league");
        _refreshRate = _stateGuy.getKeyInt("refresh_rate");
        _off_app_notifications = _stateGuy.getKeyBool("off_app_notifications");
    }
    public String getTeamName()
    {
        return _teamName;
    }
    public void setTeamName(String name)
    {
        _teamName = name;
        _stateGuy.InitializeKey("team_name", name);
    }
    public String getFavourateLeague()
    {
        if(_favourate_league != null && _favourate_league.trim() != "")
            return _favourate_league;
        else return "Premier League";
    }
    public void setFavourateLeague(String name)
    {
        _favourate_league = name;
        _stateGuy.InitializeKey("favourage_league", name);
    }
    public int getRefreshRate()
    {
        return _refreshRate;
    }
    public void setRefreshRate(int name)
    {
        _refreshRate = name;
        _stateGuy.InitializeKey("refresh_rate", name);
    }
    public boolean getOffAppNotifications()
    {
        return _off_app_notifications;
    }
    public void set_off_app_notifications(boolean name)
    {
        _off_app_notifications = name;
        _stateGuy.InitializeKey("off_app_notifications", name);
    }
    public AppOptions getAppOptions()
    {
        return new AppOptions(_stateGuy.getKeyString("app_options"));
    }
    public void setAppOptions(AppOptions options)
    {
        _stateGuy.InitializeKey("app_options", options.Compress());
    }
}
