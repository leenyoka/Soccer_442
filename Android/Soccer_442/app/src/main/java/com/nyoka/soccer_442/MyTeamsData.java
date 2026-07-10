package com.nyoka.soccer_442;

import com.nyoka.soccer_442.football_data.CompetitionMap;
import com.nyoka.soccer_442.football_data.FootballData;
import com.nyoka.soccer_442.football_data.FootballMatch;
import com.nyoka.soccer_442.football_data.MatchResponse;
import com.nyoka.soccer_442.football_data.Scorer;
import com.nyoka.soccer_442.football_data.ScorerResponse;
import com.nyoka.soccer_442.football_data.StandingsItem;
import com.nyoka.soccer_442.football_data.StandingsResponse;
import com.nyoka.soccer_442.football_data.TableItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * #19's "My Teams" mode: everything else in this app (Log, Fixtures, Results) is organized
 * by a single competition - this aggregates across every competition FootballData knows about
 * (CompetitionMap.all(), currently 13), filtering down to just the team(s) the user follows.
 * There's no existing team->competition(s) index anywhere in this codebase, so this simply
 * checks all of them - each competition's own ESPN/OpenLigaDB/BBC/SportsDB fallback chain
 * still applies underneath, this just calls it once per competition and keeps what matches.
 */
public class MyTeamsData {

    public static class TeamStanding {
        public CompetitionMap.Info competition;
        public TableItem entry;
    }

    public static class TeamScorer {
        public CompetitionMap.Info competition;
        public Scorer scorer;
    }

    public static class MyTeamsResult {
        public List<TeamStanding> standings = new ArrayList<>();
        public List<FootballMatch> fixtures = new ArrayList<>();
        public List<FootballMatch> results = new ArrayList<>();
        public List<FootballMatch> live = new ArrayList<>();
        public List<TeamScorer> scorers = new ArrayList<>();
    }

    private final FootballData footballData = new FootballData();

    /**
     * Fans the 13 competitions out across a small pool (each one still makes several
     * sequential HTTP calls internally for log+fixtures+results+fallbacks) rather than
     * working through them one at a time - otherwise this screen would take a very long
     * time to load. Bounded wait, same fail-soft spirit as the rest of this app: whatever
     * loaded in time is shown, a slow/broken competition just contributes nothing rather
     * than blocking everything else.
     */
    public MyTeamsResult load(final List<String> teamNames) {
        final MyTeamsResult result = new MyTeamsResult();
        if (teamNames == null || teamNames.isEmpty()) return result;

        final ReentrantLock lock = new ReentrantLock();
        List<CompetitionMap.Info> competitions = new ArrayList<>();
        for (CompetitionMap.Info info : CompetitionMap.all()) competitions.add(info);

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(8, competitions.size()));
        for (final CompetitionMap.Info info : competitions) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    List<TeamStanding> standings = loadStandings(info, teamNames);
                    List<FootballMatch> fixtures = loadMatches(info, teamNames, true);
                    List<FootballMatch> results = loadMatches(info, teamNames, false);
                    List<FootballMatch> live = loadLive(info, teamNames);
                    List<TeamScorer> scorers = loadScorers(info, teamNames);

                    lock.lock();
                    try {
                        result.standings.addAll(standings);
                        result.fixtures.addAll(fixtures);
                        result.results.addAll(results);
                        result.live.addAll(live);
                        result.scorers.addAll(scorers);
                    } finally {
                        lock.unlock();
                    }
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(45, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return result;
    }

    private List<TeamStanding> loadStandings(CompetitionMap.Info info, List<String> teamNames) {
        List<TeamStanding> matches = new ArrayList<>();
        try {
            StandingsResponse standings = footballData.GetLog(info.name);
            if (standings == null || standings.standings == null) return matches;
            for (StandingsItem stage : standings.standings) {
                if (stage.table == null) continue;
                for (TableItem item : stage.table) {
                    if (item.team != null && matchesAny(item.team.name, teamNames)) {
                        TeamStanding ts = new TeamStanding();
                        ts.competition = info;
                        ts.entry = item;
                        matches.add(ts);
                    }
                }
            }
        } catch (Exception ignored) {
            // one competition failing shouldn't take the others down with it
        }
        return matches;
    }

    private List<FootballMatch> loadMatches(CompetitionMap.Info info, List<String> teamNames, boolean upcoming) {
        List<FootballMatch> matches = new ArrayList<>();
        try {
            MatchResponse response = upcoming ? footballData.GetFixture(info.name) : footballData.GetResults(info.name);
            if (response == null || response.matches == null) return matches;
            for (FootballMatch match : response.matches) {
                boolean home = match.homeTeam != null && matchesAny(match.homeTeam.name, teamNames);
                boolean away = match.awayTeam != null && matchesAny(match.awayTeam.name, teamNames);
                if (home || away) matches.add(match);
            }
        } catch (Exception ignored) {
        }
        return matches;
    }

    private List<TeamScorer> loadScorers(CompetitionMap.Info info, List<String> teamNames) {
        List<TeamScorer> scorers = new ArrayList<>();
        try {
            ScorerResponse response = footballData.GetScorers(info.name);
            if (response == null || response.scorers == null) return scorers;
            for (Scorer scorer : response.scorers) {
                if (scorer.team != null && matchesAny(scorer.team.name, teamNames)) {
                    TeamScorer ts = new TeamScorer();
                    ts.competition = info;
                    ts.scorer = scorer;
                    scorers.add(ts);
                }
            }
        } catch (Exception ignored) {
        }
        return scorers;
    }

    private List<FootballMatch> loadLive(CompetitionMap.Info info, List<String> teamNames) {
        List<FootballMatch> matches = new ArrayList<>();
        try {
            MatchResponse response = footballData.GetLive(info.name);
            if (response == null || response.matches == null) return matches;
            for (FootballMatch match : response.matches) {
                boolean home = match.homeTeam != null && matchesAny(match.homeTeam.name, teamNames);
                boolean away = match.awayTeam != null && matchesAny(match.awayTeam.name, teamNames);
                if (home || away) matches.add(match);
            }
        } catch (Exception ignored) {
        }
        return matches;
    }

    private boolean matchesAny(String teamName, List<String> supported) {
        if (teamName == null) return false;
        for (String s : supported) {
            if (namesLikelyMatch(teamName, s)) return true;
        }
        return false;
    }

    // Public/static - reused by log_activity (#28) to find which group/table a supported
    // team is in, outside of any MyTeamsData instance.
    public static boolean namesLikelyMatch(String a, String b) {
        if (a == null || b == null) return false;
        String na = normalize(a);
        String nb = normalize(b);
        if (na.isEmpty() || nb.isEmpty()) return false;
        return na.equals(nb) || na.contains(nb) || nb.contains(na);
    }

    private static String normalize(String s) {
        return s.toLowerCase(Locale.US).replaceAll("[^a-z0-9]", "");
    }
}
