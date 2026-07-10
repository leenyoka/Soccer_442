package com.nyoka.soccer_442;

import android.util.Log;

import com.nyoka.soccer_442.football_data.Player;
import com.nyoka.soccer_442.football_data.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Fills in lineup/bench player photos from Wikipedia - the same source the player
 * profile screen uses, so a player looks the same whether you're viewing them from a
 * lineup chip or their own profile. Wikipedia is tried first for every player; if it
 * has no article/photo for someone, whatever photo the match data source already set
 * (e.g. ESPN's headshot) is left in place rather than being cleared, so a player never
 * goes from "has a photo" to "no photo" here.
 *
 * Runs on the calling thread (meant to be called from the existing background thread
 * that already loads match details, before posting to the UI thread) but fans the
 * ~20-30 individual Wikipedia lookups a full lineup needs out across a small pool so
 * they overlap instead of taking a lookup-per-player serial sum. Bounded wait so a slow
 * Wikipedia can't hang match loading indefinitely - same fail-soft spirit as every other
 * data source in this app.
 */
public class LineupPhotoEnricher {

    private static final String TAG = "LineupPhotoEnricher";
    private static final int MAX_THREADS = 16;
    private static final long TIMEOUT_SECONDS = 25;

    private static final WikipediaClient wiki = new WikipediaClient();

    public static void enrich(Team home, Team away) {
        List<Player> players = new ArrayList<>();
        addAll(players, home);
        addAll(players, away);
        if (players.isEmpty()) return;

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(MAX_THREADS, players.size()));
        for (final Player player : players) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        String name = player.name != null && !player.name.isEmpty() ? player.name : player.shortName;
                        String photo = wiki.getPlayerPhotoUrl(name);
                        if (photo != null) {
                            player.photoUrl = photo;
                        } else {
                            Log.i(TAG, "no Wikipedia photo found for " + name);
                        }
                    } catch (Exception ex) {
                        Log.w(TAG, "photo lookup failed for " + player.name, ex);
                    }
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static void addAll(List<Player> out, Team team) {
        if (team == null) return;
        if (team.lineup != null) out.addAll(team.lineup);
        if (team.bench != null) out.addAll(team.bench);
    }
}
