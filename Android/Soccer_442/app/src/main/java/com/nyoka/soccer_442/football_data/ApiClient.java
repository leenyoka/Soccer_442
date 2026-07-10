package com.nyoka.soccer_442.football_data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nyoka.soccer_442.AppConfig;
import com.nyoka.soccer_442.Comment;
import com.nyoka.soccer_442.HeadToHeadSummary;
import com.nyoka.soccer_442.NewsItem;
import com.nyoka.soccer_442.PlayerProfile;
import com.nyoka.soccer_442.TeamProfile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Talks to the new Soccer442.Api backend (see /Api) instead of ESPN/BBC/OpenLigaDB/SportsDB/
 * News/Wikipedia directly - all of that fetching/parsing/fallback logic now lives server-side,
 * backed by its own Postgres cache. This is the single point every data call in the app now
 * goes through (via FootballData/NewsClient/WikipediaClient, which route through Room first -
 * see AppDatabase - before ever reaching here).
 */
public class ApiClient {
    private static final Gson gson = new Gson();

    // Plain HttpURLConnection rather than the app's usual OkHttp-based HttpUtils - the API is
    // the one plaintext (non-HTTPS) endpoint this app talks to (10.0.2.2, the emulator's
    // localhost alias), and OkHttp 5.0.0's FastFallbackExchangeFinder was hanging for a full
    // connectTimeout on every cleartext connection attempt here (confirmed: a raw `nc -z`
    // socket connect to the same host:port from the same emulator succeeded instantly, so
    // this isn't a real network/NAT problem - it's specific to that OkHttp code path).
    private String get(String path) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(AppConfig.API_BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
            if (stream == null) return null;
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        } catch (Exception ex) {
            android.util.Log.w("ApiClient", "get() failed for " + path, ex);
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    // sinceUtc is null (or empty) on a competition/endpoint's first-ever sync - the server
    // treats a missing `since` the same way (beginning of time, everything comes back).
    // Two forms depending on whether `since` is the first query param or an additional one.
    private String sinceParam(String sinceUtc) {
        return sinceUtc == null || sinceUtc.isEmpty() ? "" : "&since=" + encode(sinceUtc);
    }

    private String sinceOnlyQuery(String sinceUtc) {
        return sinceUtc == null || sinceUtc.isEmpty() ? "" : "?since=" + encode(sinceUtc);
    }

    public List<Competition> getCompetitions() {
        String raw = get("/api/competitions");
        if (raw == null) return new ArrayList<>();
        Type type = new TypeToken<List<Competition>>() {}.getType();
        List<Competition> result = gson.fromJson(raw, type);
        return result != null ? result : new ArrayList<>();
    }

    public SyncEnvelope<FootballMatch> getMatches(String competitionCode, String status, String sinceUtc) {
        String raw = get("/api/competitions/" + competitionCode + "/matches?status=" + status + sinceParam(sinceUtc));
        if (raw == null) return null;
        Type type = new TypeToken<SyncEnvelope<FootballMatch>>() {}.getType();
        return gson.fromJson(raw, type);
    }

    public FootballMatch getMatchDetails(String competitionCode, String matchId) {
        String raw = get("/api/competitions/" + competitionCode + "/matches/" + matchId);
        return raw == null ? null : gson.fromJson(raw, FootballMatch.class);
    }

    public ArrayList<Comment> getCommentary(String competitionCode, String matchId) {
        String raw = get("/api/competitions/" + competitionCode + "/matches/" + matchId + "/commentary");
        if (raw == null) return new ArrayList<>();
        Type type = new TypeToken<SyncEnvelope<Comment>>() {}.getType();
        SyncEnvelope<Comment> envelope = gson.fromJson(raw, type);
        return envelope != null && envelope.items != null ? new ArrayList<>(envelope.items) : new ArrayList<>();
    }

    public HeadToHeadSummary getHeadToHeadAndForm(String competitionCode, String matchId, String homeTeamName, String awayTeamName, String utcDate) {
        String path = "/api/competitions/" + competitionCode + "/matches/" + matchId + "/head-to-head"
                + "?home=" + encode(homeTeamName) + "&away=" + encode(awayTeamName)
                + (utcDate != null ? "&utcDate=" + encode(utcDate) : "");
        String raw = get(path);
        return raw == null ? null : gson.fromJson(raw, HeadToHeadSummary.class);
    }

    public SyncEnvelope<StandingsResponse> getStandings(String competitionCode, String sinceUtc) {
        String raw = get("/api/competitions/" + competitionCode + "/standings" + sinceOnlyQuery(sinceUtc));
        if (raw == null) return null;
        Type type = new TypeToken<SyncEnvelope<StandingsResponse>>() {}.getType();
        return gson.fromJson(raw, type);
    }

    public SyncEnvelope<ScorerResponse> getScorers(String competitionCode, String sinceUtc) {
        String raw = get("/api/competitions/" + competitionCode + "/scorers" + sinceOnlyQuery(sinceUtc));
        if (raw == null) return null;
        Type type = new TypeToken<SyncEnvelope<ScorerResponse>>() {}.getType();
        return gson.fromJson(raw, type);
    }

    public SyncEnvelope<NewsItem> getNews(String competitionCode, String sinceUtc) {
        String raw = get("/api/news/" + competitionCode + sinceOnlyQuery(sinceUtc));
        if (raw == null) return null;
        Type type = new TypeToken<SyncEnvelope<NewsItem>>() {}.getType();
        return gson.fromJson(raw, type);
    }

    public PlayerProfile getPlayerProfile(String playerName) {
        String raw = get("/api/players/" + encode(playerName) + "/profile");
        return raw == null ? null : gson.fromJson(raw, PlayerProfile.class);
    }

    public String getPlayerPhotoUrl(String playerName) {
        String raw = get("/api/players/" + encode(playerName) + "/photo");
        if (raw == null) return null;
        PhotoResponse response = gson.fromJson(raw, PhotoResponse.class);
        return response != null ? response.photoUrl : null;
    }

    public TeamProfile getTeamProfile(String teamName) {
        String raw = get("/api/teams/" + encode(teamName) + "/profile");
        return raw == null ? null : gson.fromJson(raw, TeamProfile.class);
    }

    private static class PhotoResponse {
        String photoUrl;
    }
}
