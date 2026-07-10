package com.nyoka.soccer_442;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Builds the head-to-head history + recent-form sections into a container. Shared
 * between head_to_head_activity (its own dedicated screen, reachable from an upcoming
 * fixture) and the match commentary screens' "Head to Head" tab, which replaced the
 * old Commentary tab - ESPN's summary endpoint never actually populates goals/bookings,
 * so that tab always showed "no commentary" no matter the match.
 */
public class HeadToHeadRenderer {

    public static boolean isEmpty(HeadToHeadSummary summary) {
        boolean hasHeadToHead = summary != null && summary.headToHead != null && !summary.headToHead.isEmpty();
        boolean hasHomeForm = summary != null && summary.homeForm != null && !summary.homeForm.isEmpty();
        boolean hasAwayForm = summary != null && summary.awayForm != null && !summary.awayForm.isEmpty();
        return !hasHeadToHead && !hasHomeForm && !hasAwayForm;
    }

    public static void render(Context context, LinearLayout container, HeadToHeadSummary summary, String homeTeamName, String awayTeamName) {
        container.removeAllViews();
        if (isEmpty(summary)) return;

        boolean hasHeadToHead = summary.headToHead != null && !summary.headToHead.isEmpty();
        boolean hasHomeForm = summary.homeForm != null && !summary.homeForm.isEmpty();
        boolean hasAwayForm = summary.awayForm != null && !summary.awayForm.isEmpty();

        if (hasHeadToHead) {
            addSectionHeader(context, container, "Head to Head");
            for (GameResult game : summary.headToHead) addGameRow(context, container, game);
        }
        if (hasHomeForm) {
            addSectionHeader(context, container, homeTeamName + " - Recent Form");
            for (GameResult game : summary.homeForm) addGameRow(context, container, game);
        }
        if (hasAwayForm) {
            addSectionHeader(context, container, awayTeamName + " - Recent Form");
            for (GameResult game : summary.awayForm) addGameRow(context, container, game);
        }
    }

    private static void addSectionHeader(Context context, LinearLayout container, String text) {
        TextView header = new TextView(context);
        header.setText(text);
        header.setTextColor(Color.parseColor("#FBF5EF"));
        header.setTextSize(18);
        header.setPadding(0, 30, 0, 10);
        container.addView(header, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private static void addGameRow(Context context, LinearLayout container, GameResult game) {
        TextView row = new TextView(context);
        String resultLabel = game.result != null ? game.result : "?";
        row.setText(String.format("%s  %s  vs %s  (%s)", resultLabel, game.scoreLine, game.opponentName, game.competitionName));
        row.setTextColor(resultColor(game.result));
        row.setTextSize(14);
        row.setPadding(10, 8, 10, 8);
        container.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private static int resultColor(String result) {
        if ("W".equals(result)) return Color.parseColor("#4CAF50");
        if ("L".equals(result)) return Color.parseColor("#E57373");
        return Color.parseColor("#EEEEEE");
    }
}
