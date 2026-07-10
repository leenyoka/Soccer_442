package com.nyoka.soccer_442;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nyoka.soccer_442.football_data.FootballMatch;

/**
 * Renders MyTeamsData's aggregated standings/fixtures/results into plain text rows, the
 * same "build TextViews straight into a container" approach HeadToHeadRenderer uses - a
 * My Teams screen's row counts are small (a handful of supported teams across at most a
 * couple of competitions each), so a full crest-image ListView row per item isn't needed.
 */
public class MyTeamsRenderer {

    private static final Utility utility = new Utility();

    public static void renderStandings(Context context, LinearLayout container, java.util.List<MyTeamsData.TeamStanding> standings) {
        container.removeAllViews();
        if (standings.isEmpty()) {
            addEmptyRow(context, container, "No standings found for your teams yet.");
            return;
        }
        for (MyTeamsData.TeamStanding ts : standings) {
            TextView row = new TextView(context);
            row.setText(String.format("%s  -  %s: #%d, %d pts (P%d)",
                    ts.entry.team.name, ts.competition.name, ts.entry.position, ts.entry.points, ts.entry.playedGames));
            row.setTextColor(Color.parseColor("#FBF5EF"));
            row.setTextSize(14);
            row.setPadding(10, 8, 10, 8);
            container.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    public static void renderMatches(Context context, LinearLayout container, java.util.List<FootballMatch> matches) {
        container.removeAllViews();
        if (matches.isEmpty()) {
            addEmptyRow(context, container, "Nothing here right now.");
            return;
        }
        for (FootballMatch match : matches) {
            TextView row = new TextView(context);
            String home = match.homeTeam != null ? match.homeTeam.name : "?";
            String away = match.awayTeam != null ? match.awayTeam.name : "?";
            row.setText(String.format("%s: %s %s %s  (%s)",
                    utility.FormatMatchDate(match.utcDate), home, utility.FormatScore(match.score), away,
                    match.competition != null ? match.competition.name : ""));
            row.setTextColor(Color.parseColor("#FBF5EF"));
            row.setTextSize(14);
            row.setPadding(10, 8, 10, 8);
            container.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private static void addEmptyRow(Context context, LinearLayout container, String text) {
        TextView row = new TextView(context);
        row.setText(text);
        row.setTextColor(Color.parseColor("#AAAAAA"));
        row.setTextSize(13);
        row.setPadding(10, 8, 10, 8);
        container.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
}
