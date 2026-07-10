package com.nyoka.soccer_442;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nyoka.soccer_442.football_data.Team;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by linda.nyoka on 2015-02-23.
 */
public class Utility {
    /**
     * Pads the activity's content root by the system bar insets (status bar, nav bar/gesture
     * bar). Needed because targetSdk 35+ makes edge-to-edge unconditional - content draws
     * underneath the system bars by default unless something applies this padding. Call once,
     * right after setContentView(), from every activity (works for both Activity and
     * AppCompatActivity subclasses since it only relies on findViewById(android.R.id.content),
     * which every activity has).
     */
    public static void ApplyEdgeToEdgeInsets(Activity activity) {
        View root = activity.findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });
    }

    // ESPN omits seconds ("2026-08-24T19:00Z"); OpenLigaDB includes them - try both.
    private static final String[] UTC_DATE_PATTERNS = {
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm'Z'",
    };

    /** Parses a UTC ISO-8601 match date/time and formats it in the device's local timezone. */
    public String FormatMatchDate(String utcDate) {
        if (utcDate == null) return "";
        for (String pattern : UTC_DATE_PATTERNS) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(pattern, Locale.US);
                parser.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date parsed = parser.parse(utcDate);

                SimpleDateFormat formatter = new SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault());
                formatter.setTimeZone(TimeZone.getDefault());
                return formatter.format(parsed);
            } catch (Exception ignored) {
                // try the next pattern
            }
        }
        return utcDate; // couldn't parse it - show the raw value rather than crash or go blank
    }
    /**
     * Non-blocking replacement for the ProgressDialog spinner every screen used to show while
     * loading - a slim bar pinned to the top (id topProgressBar, from res/layout/progress_bar_top.xml,
     * included as the first child of the activity's root layout) rather than a modal dialog that
     * blocks the whole screen. Safe to call even if a screen hasn't added the include yet.
     */
    public static void ShowLoading(Activity activity) {
        View bar = activity.findViewById(R.id.topProgressBar);
        if (bar != null) bar.setVisibility(View.VISIBLE);
    }

    public static void HideLoading(Activity activity) {
        View bar = activity.findViewById(R.id.topProgressBar);
        if (bar != null) bar.setVisibility(View.GONE);
    }

    // A knockout match decided by penalties keeps a drawn fullTime score (that's the whole
    // point of a shootout) - without this, "1 - 1" shows for both an actual draw and a
    // shootout win, with no way to tell them apart.
    public String FormatScore(com.nyoka.soccer_442.football_data.Score score) {
        String base = score.fullTime.home + " - " + score.fullTime.away;
        if (score.penaltyHome != null && score.penaltyAway != null) {
            return base + " (" + score.penaltyHome + "-" + score.penaltyAway + " pens)";
        }
        return base;
    }
    public String FixName(String word)
    {
        word = word.toLowerCase();

        String[] numbers = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8","9", ".", "-", " "};

        for (String number :numbers)
            word = word.replace(number, "");

        word = word.trim();

        word = Trim("_",word);

        String newName = "";
        for(char value : word.toCharArray()) {
            if (IsEnglishLetter(value))
                newName += value;
        }

        return newName;
    }
    public static boolean IsEnglishLetter(char c)
    {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }
    public boolean GetMeAnImage(ImageView view, String Name) {

        try  {
            /*
            final R.drawable drawableResources = new R.drawable();
            final Class<R.drawable> c = R.drawable.class;
            final Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                String name = field.getName();


                Name = FixName(Name);

                if(Name.toLowerCase().trim().equals(name.toLowerCase().trim())){
                  //  ||(Name.toLowerCase().trim().contains(name.trim().toLowerCase()))
                //||(name.toLowerCase().trim().contains(Name.toLowerCase().trim()))) {
                    int resourceId = field.getInt(drawableResources);
                    view.setImageResource(resourceId);
                    return true;
                }
            }
             */
        }
        catch (Exception ex){
        }
        return false;
    }
    /**
     * Short text to show instead of a crest when a team has no usable crest URL - mainly
     * placeholder "teams" for not-yet-determined fixture participants (e.g. World Cup
     * "Quarterfinal 1 Winner", which ESPN gives an empty-string logo instead of a real one).
     */
    public String TeamFallbackLabel(Team team) {
        if (team == null) return "";
        if (team.tla != null && !team.tla.isEmpty()) return team.tla;
        if (team.name != null && !team.name.isEmpty()) {
            return team.name.length() > 3 ? team.name.substring(0, 3).toUpperCase() : team.name.toUpperCase();
        }
        return "";
    }

    /**
     * Loads a team's crest into imageView + clears nameView, or - when there's no usable
     * crest (TBD fixture participants) - shows a placeholder badge behind a short label
     * instead of leaving the row blank. Centralizes what fixtureListAdapter/resultsListAdapter/
     * liveListAdapter all need, including reloading on every call (not just first view
     * creation) so ListView row recycling doesn't leave a stale crest/badge on screen.
     */
    public void ShowTeamBadge(Context contextView, Team team, ImageView imageView, TextView nameView) {
        if (team != null && team.crest != null && !team.crest.isEmpty()) {
            nameView.setText("");
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
            if (team.crest.endsWith(".svg")) {
                new SvgImageLoader().loadSvgImage(contextView, team.crest, imageView);
            } else {
                Glide.with(contextView).load(team.crest).into(imageView);
            }
        } else {
            imageView.setImageResource(R.drawable.team_placeholder_badge);
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            nameView.setText(TeamFallbackLabel(team));
            // template_game.xml declares the crest ImageView after the name TextView, so the
            // ImageView draws on top and would otherwise fully hide this label behind the badge.
            nameView.bringToFront();
            View parent = (View) nameView.getParent();
            parent.requestLayout();
            parent.invalidate();
        }
    }

    public boolean GetMeAnImage(TextView view, String Name) {
        // This TextView sits directly behind a crest ImageView in every layout that
        // uses it (template_game.xml and friends) - it used to fall back to a 3-letter
        // team code (e.g. "EVE") when no local drawable badge matched, but crests now
        // load reliably from ESPN/OpenLigaDB, so that fallback just rendered as text
        // overlapping the real crest image. Leave it blank; the crest is the badge now.
        view.setText("");
        return false;
    }
    public boolean GetMeAnImage(TextView view, String Name, boolean notTeamImg) {

        try  {
            /*
            final R.drawable drawableResources = new R.drawable();
            final Class<R.drawable> c = R.drawable.class;
            final Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                String name = field.getName();


                //Name = FixName(Name);

                if(Name.equals(name)){
                    int resourceId = field.getInt(drawableResources);
                    view.setBackgroundResource(resourceId);
                    view.setText("");
                    return true;
                }
            }
             */
        }
        catch (Exception ex){
        }
        view.setBackgroundResource(R.drawable.invisible);
        //view.setText(Name.toUpperCase().substring(0,3));
        return false;
    }
    public String Trim(String value, String host)
    {
        while (host.endsWith(value))
            host = host.substring(0, host.length()-1);

        while (host.startsWith(value))
            host = host.substring(1);

        return host;
    }
    public void showDialog(String message, boolean saved, String heading, FragmentManager manager)
    {
        FragmentManager fm = manager;
        activity_msg acceptTermsDialogFragment = new activity_msg();
        Bundle args = new Bundle();
        args.putString("message", message);
        args.putBoolean("post", saved);
        args.putString("heading",heading);
        acceptTermsDialogFragment.setArguments(args);
        acceptTermsDialogFragment.show(fm, "");
    }

    public void ShowNetworkError(FragmentManager manager)
    {
        showDialog("No active network connection found. please enable data or connect to wifi"
                , false, "No Internet Connection", manager);
    }

    public void ShowNetworkError(TextView view, String txt)
    {
        view.setText(txt + "(offline)");
    }
    public boolean Connected(Context context)
    {
        DeviceConnectivityHelper connectivityHelper = DeviceConnectivityHelper.getInstance(context);
        if(!connectivityHelper.isInternetOn(context) && !WifiConnected(context)) {
            return false;
        }
        return true;
    }
    public boolean WifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            return true;
        }
        return false;
    }
    public boolean IsInt(String value)
    {
        try {
            int x = Integer.parseInt(value);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }
}
