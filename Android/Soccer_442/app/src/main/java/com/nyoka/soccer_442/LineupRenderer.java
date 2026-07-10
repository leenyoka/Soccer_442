package com.nyoka.soccer_442;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nyoka.soccer_442.football_data.Player;
import com.nyoka.soccer_442.football_data.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Renders two teams' starting XI onto a pitch as rows of player "chips" grouped by
 * how advanced their position is (goalkeeper -> defence -> midfield -> attack),
 * mirroring the away team from the top down and the home team from the bottom up so
 * both sides face each other across the halfway line - the same layout convention
 * used by ESPN/Sofascore-style lineup graphics.
 *
 * There's no reliable pitch-coordinate field in ESPN's data (formationPlace is just
 * squad-list order, not a grid position), so row membership is inferred entirely from
 * each player's position abbreviation (e.g. "CD-L", "AM", "RM"). This naturally
 * reproduces the right row sizes for whatever formation is in play without needing to
 * trust or parse ESPN's separate formation string at all.
 */
public class LineupRenderer {

    public interface OnPlayerClick {
        void onClick(String playerName);
    }

    public static void render(Context context, LinearLayout container, Team home, Team away, OnPlayerClick listener) {
        render(context, container, null, home, away, listener);
    }

    public static void render(Context context, LinearLayout container, LinearLayout substitutesContainer, Team home, Team away, OnPlayerClick listener) {
        container.removeAllViews();
        container.setBackground(new PitchDrawable());
        if (substitutesContainer != null) substitutesContainer.removeAllViews();
        if (home == null || away == null || home.lineup == null || away.lineup == null) return;

        LayoutInflater inflater = LayoutInflater.from(context);

        // The away team is drawn attacking downward (upside-down relative to its normal
        // shape), so its left/right has to be mirrored to keep both teams' actual pitch
        // sides lined up on the same side of the screen - exactly like real broadcast
        // lineup graphics (e.g. a left-back stays on the same physical touchline as the
        // other team's left-back, which visually puts them on opposite screen sides).
        List<List<Player>> awayRows = bucketRows(away, true);
        for (List<Player> row : awayRows) {
            container.addView(buildRow(inflater, context, row, listener));
        }

        List<List<Player>> homeRows = bucketRows(home, false);
        Collections.reverse(homeRows);
        for (List<Player> row : homeRows) {
            container.addView(buildRow(inflater, context, row, listener));
        }

        if (substitutesContainer != null) {
            renderSubstitutes(inflater, context, substitutesContainer, "Away Substitutes", away.bench, listener);
            renderSubstitutes(inflater, context, substitutesContainer, "Home Substitutes", home.bench, listener);
        }
    }

    private static final int SUBS_PER_ROW = 4;

    // Plain wrapped rows of the same player chip, not on the pitch graphic - a substitute
    // never took the pitch, so grouping them by playing position (like the starting XI rows
    // above) would be misleading; squad-list order is all there is to go on anyway.
    private static void renderSubstitutes(LayoutInflater inflater, Context context, LinearLayout container,
                                            String heading, List<Player> bench, OnPlayerClick listener) {
        if (bench == null || bench.isEmpty()) return;

        TextView header = new TextView(context);
        header.setText(heading);
        header.setTextColor(0xFFFCFAFA);
        header.setTextSize(14);
        header.setPadding(16, 12, 16, 6);
        container.addView(header);

        List<Player> row = new ArrayList<>();
        for (int i = 0; i < bench.size(); i++) {
            row.add(bench.get(i));
            if (row.size() == SUBS_PER_ROW || i == bench.size() - 1) {
                container.addView(buildRow(inflater, context, row, listener));
                row = new ArrayList<>();
            }
        }
    }

    private static List<List<Player>> bucketRows(Team team, final boolean mirror) {
        Map<Integer, List<Player>> byAdvancement = new TreeMap<>();
        for (Player p : team.lineup) {
            int score = advancement(p.position);
            List<Player> row = byAdvancement.get(score);
            if (row == null) {
                row = new ArrayList<>();
                byAdvancement.put(score, row);
            }
            row.add(p);
        }
        List<List<Player>> rows = new ArrayList<>();
        for (List<Player> row : byAdvancement.values()) {
            Collections.sort(row, new Comparator<Player>() {
                @Override
                public int compare(Player a, Player b) {
                    int order = Integer.compare(horizontalOrder(a.position), horizontalOrder(b.position));
                    return mirror ? -order : order;
                }
            });
            rows.add(row);
        }
        return rows;
    }

    private static int advancement(String posAbbr) {
        if (posAbbr == null) return 3;
        String p = posAbbr.toUpperCase();
        if (p.startsWith("G")) return 0;
        if (p.startsWith("CD") || p.equals("RB") || p.equals("LB") || p.startsWith("WB") || p.startsWith("RWB") || p.startsWith("LWB")) return 1;
        if (p.startsWith("DM") || p.equals("CDM")) return 2;
        if (p.startsWith("CM") || p.equals("LM") || p.equals("RM")) return 3;
        if (p.startsWith("AM")) return 4;
        if (p.startsWith("CF") || p.startsWith("ST") || p.equals("F") || p.startsWith("LW") || p.startsWith("RW")) return 5;
        return 3;
    }

    // A row can hold a genuinely wide player (LB, LM, LW - a specialist standing right on
    // the touchline) alongside a "-L"-suffixed player who's still fundamentally central
    // (CD-L, CM-L - a centre-back/centre-mid who happens to cover the left side of a back
    // four or midfield three). Treating both as equally "left" let a centre-back's sort
    // position collapse onto the same slot as the actual left-back, sometimes landing him
    // out on the wide edge where a specialist full-back belongs. Five buckets keep genuine
    // wide players outermost and "-L"/"-R" players one step in from them.
    private static int horizontalOrder(String posAbbr) {
        if (posAbbr == null) return 0;
        String p = posAbbr.toUpperCase();
        if (p.endsWith("-L")) return -1;
        if (p.endsWith("-R")) return 1;
        if (p.startsWith("L")) return -2;
        if (p.startsWith("R")) return 2;
        return 0;
    }

    private static View buildRow(LayoutInflater inflater, Context context, List<Player> players, final OnPlayerClick listener) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER);
        // wrap_content, not weight=1 - forcing every row to an equal share of a fixed total
        // height clipped chip names off the bottom whenever a formation needed more than
        // ~5 rows combined (both teams stacked in the same container). Sizing each row by
        // its actual content lets the container (now in a ScrollView) grow to fit instead.
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setPadding(0, 6, 0, 6);

        for (final Player player : players) {
            View chip = inflater.inflate(R.layout.pitch_player_chip, row, false);
            ImageView photo = chip.findViewById(R.id.chipPhoto);
            TextView number = chip.findViewById(R.id.chipNumber);
            TextView name = chip.findViewById(R.id.chipName);

            if (player.photoUrl != null) {
                // Wikipedia thumbnails are rectangular - centerCrop alone fills the whole
                // square ImageView with it, hiding the circular chip background entirely and
                // leaving photo chips square-cornered next to the perfect circles shown for
                // players with no photo. circleCrop masks the loaded image to the same circle.
                Glide.with(context).load(player.photoUrl).circleCrop().into(photo);
            }
            number.setText(player.shirtNumber > 0 ? String.valueOf(player.shirtNumber) : "");
            // Full names ("Quilindschy Hartman") don't fit this chip - shortName ("Q. Hartman")
            // is what ESPN itself uses for tight spaces, and reads the same as any other
            // lineup graphic. Falls back to the full name for the rare case shortName is missing.
            name.setText(player.shortName != null ? player.shortName : player.name);

            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onClick(player.name);
                }
            });

            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(chip, chipParams);
        }
        return row;
    }
}
