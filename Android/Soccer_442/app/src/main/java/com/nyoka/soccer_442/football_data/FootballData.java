package com.nyoka.soccer_442.football_data;

import android.content.Context;

import com.google.gson.Gson;
import com.nyoka.soccer_442.HeadToHeadSummary;
import com.nyoka.soccer_442.Soccer442Application;
import com.nyoka.soccer_442.data.AppDatabase;
import com.nyoka.soccer_442.data.entity.CachedMatchEntity;
import com.nyoka.soccer_442.data.entity.CachedScorerEntity;
import com.nyoka.soccer_442.data.entity.CachedStandingsEntity;
import com.nyoka.soccer_442.data.entity.SyncStateEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * All the fetching/parsing/fallback logic that used to live here (talking to ESPN, BBC,
 * OpenLigaDB, TheSportsDB directly) now lives server-side in Soccer442.Api (see /Api at the
 * repo root) - this class's job is now purely the incremental-sync dance against that API:
 * read this device's last-synced timestamp for a resource out of Room, send it as `?since=`,
 * merge whatever changed into Room, bump the synced timestamp to the server's own clock, then
 * read the full current set back out of Room for the caller. Every public method here keeps
 * its exact old signature so none of this app's ~15 call sites needed to change.
 */
public class FootballData {

    private final Context context;
    private final ApiClient api = new ApiClient();
    private final Gson gson = new Gson();

    public FootballData() {
        context = Soccer442Application.getAppContext();
    }

    public Response GetCompetitions() {
        Response response = new Response();
        List<Competition> competitions = new ArrayList<>();
        for (CompetitionMap.Info info : CompetitionMap.all()) {
            Competition c = new Competition();
            c.name = info.name;
            c.code = info.code;
            c.area = new Area();
            c.area.name = info.areaName;
            competitions.add(c);
        }
        response.competitions = competitions;
        return response;
    }

    public static String GetCompetitionCode(String teamName) {
        switch (teamName) {
            case "Campeonato Brasileiro Série A":
                return "BSA";
            case "Championship":
                return "ELC";
            case "Premier League":
                return "PL";
            case "UEFA Champions League":
                return "CL";
            case "European Championship":
                return "EC";
            case "Ligue 1":
                return "FL1";
            case "Bundesliga":
                return "BL1";
            case "Serie A":
                return "SA";
            case "SerieA":
                return "SA";
            case "Eredivisie":
                return "DED";
            case "Primeira Liga":
                return "PPL";
            case "Copa Libertadores":
                return "CLI";
            case "Primera Division":
                return "PD";
            case "FIFA World Cup":
                return "WC";
            default:
                return "Code not found";
        }
    }

    public FootballMatch GetMatchDetails(String competition, String matchId) {
        CompetitionMap.Info info = CompetitionMap.byCode(GetCompetitionCode(competition));
        if (info == null) return null;
        return api.getMatchDetails(info.code, matchId);
    }

    public ArrayList<com.nyoka.soccer_442.Comment> GetCommentary(String competition, String matchId) {
        CompetitionMap.Info info = CompetitionMap.byCode(GetCompetitionCode(competition));
        if (info == null) return new ArrayList<>();
        return api.getCommentary(info.code, matchId);
    }

    public HeadToHeadSummary GetHeadToHeadAndForm(String competition, String matchId, String homeTeamName, String awayTeamName) {
        return GetHeadToHeadAndForm(competition, matchId, homeTeamName, awayTeamName, null);
    }

    public HeadToHeadSummary GetHeadToHeadAndForm(String competition, String matchId, String homeTeamName, String awayTeamName, String utcDate) {
        CompetitionMap.Info info = CompetitionMap.byCode(GetCompetitionCode(competition));
        if (info == null) return null;
        return api.getHeadToHeadAndForm(info.code, matchId, homeTeamName, awayTeamName, utcDate);
    }

    /** Legacy entry point, unused by any activity - the cross-competition scan this used to do
     * isn't supported by the new API (which needs a competition code up front), so this now
     * fails soft rather than reproducing that scan client-side for genuinely dead code. */
    public MatchResponse GetStats(String matchId) {
        return emptyMatchResponse();
    }

    /** Legacy entry point, unused by any activity - see GetStats. */
    public MatchResponse GetLineUp(String matchId) {
        return emptyMatchResponse();
    }

    private MatchResponse emptyMatchResponse() {
        MatchResponse response = new MatchResponse();
        response.matches = new ArrayList<>();
        return response;
    }

    public MatchResponse GetFixture(String competition) {
        return syncMatches(competition, "fixture");
    }

    public MatchResponse GetResults(String competition) {
        return syncMatches(competition, "result");
    }

    public MatchResponse GetLive(String competition) {
        return syncMatches(competition, "live");
    }

    private MatchResponse syncMatches(String competition, String status) {
        MatchResponse response = emptyMatchResponse();
        CompetitionMap.Info info = CompetitionMap.byCode(GetCompetitionCode(competition));
        if (info == null) return response;

        AppDatabase db = AppDatabase.getInstance(context);
        String key = "matches:" + info.code + ":" + status;
        SyncStateEntity state = db.syncStateDao().get(key);
        String since = state != null ? state.lastSyncedUtc : null;

        SyncEnvelope<FootballMatch> envelope = api.getMatches(info.code, status, since);
        if (envelope != null && envelope.items != null) {
            for (FootballMatch match : envelope.items) {
                CachedMatchEntity row = new CachedMatchEntity();
                row.competitionCode = info.code;
                row.status = status;
                row.matchId = String.valueOf(match.id);
                row.rawJson = gson.toJson(match);
                row.updatedUtc = envelope.serverTimeUtc;
                db.cachedMatchDao().upsert(row);
            }
            SyncStateEntity newState = new SyncStateEntity();
            newState.endpointKey = key;
            newState.lastSyncedUtc = envelope.serverTimeUtc;
            db.syncStateDao().upsert(newState);
        }

        List<CachedMatchEntity> rows = db.cachedMatchDao().getAll(info.code, status);
        List<FootballMatch> matches = new ArrayList<>();
        for (CachedMatchEntity row : rows) {
            FootballMatch match = gson.fromJson(row.rawJson, FootballMatch.class);
            if (match != null) matches.add(match);
        }
        response.matches = matches;
        Competition comp = new Competition();
        comp.name = info.name;
        comp.code = info.code;
        response.competition = comp;
        return response;
    }

    public ScorerResponse GetScorers(String competition) {
        ScorerResponse response = new ScorerResponse();
        response.scorers = new ArrayList<>();
        CompetitionMap.Info info = CompetitionMap.byCode(GetCompetitionCode(competition));
        if (info == null) return response;

        AppDatabase db = AppDatabase.getInstance(context);
        String key = "scorers:" + info.code;
        SyncStateEntity state = db.syncStateDao().get(key);
        String since = state != null ? state.lastSyncedUtc : null;

        SyncEnvelope<ScorerResponse> envelope = api.getScorers(info.code, since);
        if (envelope != null && envelope.items != null) {
            if (!envelope.items.isEmpty()) {
                CachedScorerEntity row = new CachedScorerEntity();
                row.competitionCode = info.code;
                row.rawJson = gson.toJson(envelope.items.get(0));
                row.updatedUtc = envelope.serverTimeUtc;
                db.cachedScorerDao().upsert(row);
            }
            SyncStateEntity newState = new SyncStateEntity();
            newState.endpointKey = key;
            newState.lastSyncedUtc = envelope.serverTimeUtc;
            db.syncStateDao().upsert(newState);
        }

        CachedScorerEntity row = db.cachedScorerDao().get(info.code);
        if (row != null) {
            ScorerResponse cached = gson.fromJson(row.rawJson, ScorerResponse.class);
            if (cached != null) return cached;
        }
        Competition comp = new Competition();
        comp.name = info.name;
        comp.code = info.code;
        response.competition = comp;
        return response;
    }

    public StandingsResponse GetLog(String competition) {
        CompetitionMap.Info info = CompetitionMap.byCode(GetCompetitionCode(competition));
        if (info == null) return null;

        AppDatabase db = AppDatabase.getInstance(context);
        String key = "standings:" + info.code;
        SyncStateEntity state = db.syncStateDao().get(key);
        String since = state != null ? state.lastSyncedUtc : null;

        SyncEnvelope<StandingsResponse> envelope = api.getStandings(info.code, since);
        if (envelope != null && envelope.items != null) {
            if (!envelope.items.isEmpty()) {
                CachedStandingsEntity row = new CachedStandingsEntity();
                row.competitionCode = info.code;
                row.rawJson = gson.toJson(envelope.items.get(0));
                row.updatedUtc = envelope.serverTimeUtc;
                db.cachedStandingsDao().upsert(row);
            }
            SyncStateEntity newState = new SyncStateEntity();
            newState.endpointKey = key;
            newState.lastSyncedUtc = envelope.serverTimeUtc;
            db.syncStateDao().upsert(newState);
        }

        CachedStandingsEntity row = db.cachedStandingsDao().get(info.code);
        return row != null ? gson.fromJson(row.rawJson, StandingsResponse.class) : null;
    }
}
