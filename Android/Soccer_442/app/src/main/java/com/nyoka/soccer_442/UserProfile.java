package com.nyoka.soccer_442;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

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

    // "|" rather than AppOptions' "_" - a real team name ("Inter Milan") could plausibly contain
    // an underscore-adjacent word break, but never a literal pipe.
    private static final String SUPPORTED_TEAMS_DELIMITER = "|";

    /** The team(s) the user follows, for #19's "My Teams" cross-competition mode. */
    public List<String> getSupportedTeams() {
        String raw = _stateGuy.getKeyString("supported_teams");
        List<String> teams = new ArrayList<>();
        if (raw != null && !raw.trim().isEmpty()) {
            for (String team : raw.split("\\" + SUPPORTED_TEAMS_DELIMITER)) {
                if (!team.trim().isEmpty()) teams.add(team.trim());
            }
        }
        return teams;
    }

    public void setSupportedTeams(List<String> teams) {
        StringBuilder sb = new StringBuilder();
        for (String team : teams) {
            if (sb.length() > 0) sb.append(SUPPORTED_TEAMS_DELIMITER);
            sb.append(team.trim());
        }
        _stateGuy.InitializeKey("supported_teams", sb.toString());
    }

    public void addSupportedTeam(String teamName) {
        if (teamName == null || teamName.trim().isEmpty()) return;
        List<String> teams = getSupportedTeams();
        for (String existing : teams) {
            if (existing.equalsIgnoreCase(teamName.trim())) return; // already there
        }
        teams.add(teamName.trim());
        setSupportedTeams(teams);
    }

    public void removeSupportedTeam(String teamName) {
        List<String> teams = getSupportedTeams();
        List<String> remaining = new ArrayList<>();
        for (String existing : teams) {
            if (!existing.equalsIgnoreCase(teamName)) remaining.add(existing);
        }
        setSupportedTeams(remaining);
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
