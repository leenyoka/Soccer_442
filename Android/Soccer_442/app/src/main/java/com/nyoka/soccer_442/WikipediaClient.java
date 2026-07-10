package com.nyoka.soccer_442;

import com.nyoka.soccer_442.football_data.ApiClient;

/**
 * Player/team profile screens sourced from Wikipedia via Soccer442.Api (which does the actual
 * Wikipedia lookups + wikitext parsing + persistent caching now) instead of this class calling
 * Wikipedia directly - see /Api/Soccer442.Api/Sources/WikipediaClient.cs for the logic that
 * used to live here.
 */
public class WikipediaClient {
    private final ApiClient api = new ApiClient();

    public String getPlayerPhotoUrl(String playerName) {
        if (playerName == null || playerName.isEmpty()) return null;
        return api.getPlayerPhotoUrl(playerName);
    }

    public PlayerProfile getPlayerProfile(String playerName) {
        return api.getPlayerProfile(playerName);
    }

    public TeamProfile getTeamProfile(String teamName) {
        return api.getTeamProfile(teamName);
    }
}
