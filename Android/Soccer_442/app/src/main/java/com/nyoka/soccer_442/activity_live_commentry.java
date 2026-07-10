package com.nyoka.soccer_442;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.nyoka.soccer_442.football_data.FootballData;
import com.nyoka.soccer_442.football_data.FootballMatch;
import com.nyoka.soccer_442.football_data.HomeOrAway;
import com.nyoka.soccer_442.football_data.Statistics;

/**
 * Created by linda.nyoka on 2015-02-22.
 */
public class activity_live_commentry extends AppCompatActivity {

    Context context;
    String matchId;
    //Live liveGame;
    String competition;
    Utility utility = new Utility();
    ProgressDialog dialog;
    boolean headToHeadEmpty = true;
    boolean commentaryEmpty = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_live_commentry);
        Utility.ApplyEdgeToEdgeInsets(this);
        getSupportActionBar().hide();
        context = this;
        SetMatchId();
        Initialize();

    }

    private void SetMatchId() {
        Intent intent = getIntent();
        matchId = intent.getStringExtra("matchId");
        competition = new UserProfile(this).getFavourateLeague();
        //liveGame = (Live) intent.getSerializableExtra("live");
    }
   public boolean ShowError()
    {
        if(utility.Connected(getApplicationContext()))
            return false;

        else
        {
            utility.ShowNetworkError(getSupportFragmentManager());
            return true;
        }
    }
    private void Initialize() {

        if(ShowError())
            return;


        Utility.ShowLoading(this);
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                FootballData superSport = new FootballData();
                FootballMatch holder = null;
                try {
                    holder = superSport.GetMatchDetails( competition, matchId);

                    if (holder == null) {
                        android.util.Log.w("activity_live_commentry", "GetMatchDetails returned null for matchId=" + matchId + " competition=" + competition);
                        utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
                        return;
                    }

                    final FootballMatch finalHolder = holder;
                    final HeadToHeadSummary h2h = superSport.GetHeadToHeadAndForm(competition, matchId, holder.homeTeam.name, holder.awayTeam.name, holder.utcDate);
                    final java.util.ArrayList<Comment> commentary = superSport.GetCommentary(competition, matchId);
                    // Still on the background thread - fans out ~20-30 Wikipedia lookups
                    // across a small pool rather than blocking the UI thread on them.
                    LineupPhotoEnricher.enrich(holder.homeTeam, holder.awayTeam);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            // comm_homeTeamImage/comm_awayTeamImage were TextView slots with no
                            // actual crest ImageView anywhere on this screen - GetMeAnImage(TextView,..)
                            // is a deliberate no-op now (see its comment), so nothing ever rendered here.
                            com.bumptech.glide.Glide.with(context).load(finalHolder.homeTeam.crest).into((android.widget.ImageView) findViewById(R.id.comm_homeTeamImage));
                            com.bumptech.glide.Glide.with(context).load(finalHolder.awayTeam.crest).into((android.widget.ImageView) findViewById(R.id.comm_awayTeamImage));
                            ((TextView) findViewById(R.id.comm_homeTeamName)).setText(finalHolder.homeTeam.name);
                            ((TextView) findViewById(R.id.comm_awayTeamName)).setText(finalHolder.awayTeam.name);

                            TextView score = (TextView) findViewById(R.id.comm_matchScore);
                            score.setText(utility.FormatScore(finalHolder.score));
                            // This TextView is 40sp, sized for a plain "1 - 1" between the two
                            // crests - a penalty-shootout suffix ("1 - 1 (4-2 pens)") is much
                            // longer and would overflow into the crest columns at that size.
                            boolean hasPenalties = finalHolder.score.penaltyHome != null;
                            score.setTextSize(hasPenalties ? 18 : 40);
                            TextView title = (TextView) findViewById(R.id.matchStatus);
                            title.setText(finalHolder.status);

                            if (finalHolder.score.fullTime.home != 0) {
                                TextView scorersHome = (TextView) findViewById(R.id.homeTeamScorers);
                                scorersHome.setText("");
                                for (String scorer : finalHolder.GetScorers(HomeOrAway.Home))
                                    scorersHome.setText(scorersHome.getText() + scorer);
                                scorersHome.setText(utility.Trim(",", scorersHome.getText().toString()));
                            }

                            if (finalHolder.score.fullTime.away != 0) {
                                TextView scorersAway = (TextView) findViewById(R.id.awayTeamScorers);
                                scorersAway.setText("");
                                for (String scorer : finalHolder.GetScorers(HomeOrAway.Away))
                                    scorersAway.setText(scorersAway.getText() + scorer);
                                scorersAway.setText(utility.Trim(",", scorersAway.getText().toString()));
                            }

                            headToHeadEmpty = HeadToHeadRenderer.isEmpty(h2h);
                            HeadToHeadRenderer.render(context, (LinearLayout) findViewById(R.id.headToHeadContainer), h2h, finalHolder.homeTeam.name, finalHolder.awayTeam.name);

                            commentaryEmpty = commentary.isEmpty();
                            ((android.widget.ListView) findViewById(R.id.commentaryListView)).setAdapter(new CommentryListAdapter(context, commentary));

                            HeadToHead(null);

                            SetStats(finalHolder.homeTeam.statistics, finalHolder.awayTeam.statistics);
                            SetLineUp(finalHolder);
                            Utility.HideLoading(activity_live_commentry.this);

                        }
                    });

                }
                catch (Exception ex)
                {
                    android.util.Log.w("activity_live_commentry", "Failed to load match " + matchId, ex);
                    utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
                    return;
                }
            }
        });
        eThread.start();
    }
    public void Commentary(View view)
    {
        ((android.widget.ListView)findViewById(R.id.commentaryListView)).setVisibility(View.VISIBLE);
        ((android.widget.ScrollView)findViewById(R.id.headToHeadScroll)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.LineUpView)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.statsView)).setVisibility(View.GONE);
        findViewById(R.id.comm_empty).setVisibility(commentaryEmpty ? View.VISIBLE : View.GONE);

        ((Button)findViewById(R.id.btnCommentary)).setBackgroundResource(R.drawable.green_button_selected);
        ((Button)findViewById(R.id.btnHeadToHead)).setBackgroundResource(R.drawable.green_button);
        ((Button)findViewById(R.id.btnStats)).setBackgroundResource(R.drawable.green_button);
        ((Button)findViewById(R.id.button1)).setBackgroundResource(R.drawable.green_button);
    }
    public void HeadToHead(View view)
    {
        ((android.widget.ListView)findViewById(R.id.commentaryListView)).setVisibility(View.GONE);
        ((android.widget.ScrollView)findViewById(R.id.headToHeadScroll)).setVisibility(View.VISIBLE);
        ((LinearLayout)findViewById(R.id.LineUpView)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.statsView)).setVisibility(View.GONE);
        // comm_empty is a match_parent-height TextView shared by every tab's empty state,
        // so it has to be explicitly restored here or it stays GONE forever once Stats()/
        // LineUp() below turn it off.
        findViewById(R.id.comm_empty).setVisibility(headToHeadEmpty ? View.VISIBLE : View.GONE);

        ((Button)findViewById(R.id.btnCommentary)).setBackgroundResource(R.drawable.green_button);
        ((Button)findViewById(R.id.btnHeadToHead)).setBackgroundResource(R.drawable.green_button_selected);
        ((Button)findViewById(R.id.btnStats)).setBackgroundResource(R.drawable.green_button);
        ((Button)findViewById(R.id.button1)).setBackgroundResource(R.drawable.green_button);
    }
    public void Stats(View view)
    {
        ((android.widget.ListView)findViewById(R.id.commentaryListView)).setVisibility(View.GONE);
        ((android.widget.ScrollView)findViewById(R.id.headToHeadScroll)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.LineUpView)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.statsView)).setVisibility(View.VISIBLE);
        // comm_empty is match_parent height - left visible (from another tab having no data)
        // it swallows all remaining vertical space and hides statsView entirely even though
        // statsView itself is correctly set to VISIBLE above.
        findViewById(R.id.comm_empty).setVisibility(View.GONE);

        ((Button)findViewById(R.id.btnCommentary)).setBackgroundResource(R.drawable.green_button);
        ((Button)findViewById(R.id.btnHeadToHead)).setBackgroundResource(R.drawable.green_button);
        ((Button)findViewById(R.id.btnStats)).setBackgroundResource(R.drawable.green_button_selected);
        ((Button)findViewById(R.id.button1)).setBackgroundResource(R.drawable.green_button);
    }
    public void LineUp(View view)
    {
        ((android.widget.ListView)findViewById(R.id.commentaryListView)).setVisibility(View.GONE);
        ((android.widget.ScrollView)findViewById(R.id.headToHeadScroll)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.LineUpView)).setVisibility(View.VISIBLE);
        ((LinearLayout)findViewById(R.id.statsView)).setVisibility(View.GONE);
        // Same reason as Stats() above - comm_empty must be explicitly cleared or it blocks
        // LineUpView from having any visible space.
        findViewById(R.id.comm_empty).setVisibility(View.GONE);
        ((Button)findViewById(R.id.btnCommentary)).setBackgroundResource(R.drawable.green_button);
        ((Button)findViewById(R.id.btnHeadToHead)).setBackgroundResource(R.drawable.green_button);
        ((Button)findViewById(R.id.btnStats)).setBackgroundResource(R.drawable.green_button);
        ((Button)findViewById(R.id.button1)).setBackgroundResource(R.drawable.green_button_selected);
    }
    private void SetStats(Statistics home, Statistics away)
    {
        if(home != null && away != null) {
            // setText(int) resolves to the "look up a string resource by this ID" overload,
            // not "render this number" - these are int fields, so they must go through
            // String.valueOf() first or Android throws Resources.NotFoundException.
            ((TextView) findViewById(R.id.shotsHome)).setText(String.valueOf(home.shots));
            ((TextView) findViewById(R.id.shotsAway)).setText(String.valueOf(away.shots));

            ((TextView) findViewById(R.id.foulsHome)).setText(String.valueOf(home.fouls));
            ((TextView) findViewById(R.id.foulsAway)).setText(String.valueOf(away.fouls));

            ((TextView) findViewById(R.id.cornersHome)).setText(String.valueOf(home.corner_kicks));
            ((TextView) findViewById(R.id.cornersAway)).setText(String.valueOf(away.corner_kicks));

            ((TextView) findViewById(R.id.offsidesHome)).setText(String.valueOf(home.offsides));
            ((TextView) findViewById(R.id.offsidesAway)).setText(String.valueOf(away.offsides));

            ((TextView) findViewById(R.id.yellowHome)).setText(String.valueOf(home.yellow_cards));
            ((TextView) findViewById(R.id.yellowAway)).setText(String.valueOf(away.yellow_cards));

            ((TextView) findViewById(R.id.redHome)).setText(String.valueOf(home.red_cards));
            ((TextView) findViewById(R.id.redAway)).setText(String.valueOf(away.red_cards));
        }
    }
    private void SetLineUp(FootballMatch lineUp)
    {
        LinearLayout pitchRows = (LinearLayout) findViewById(R.id.pitchRows);
        LinearLayout substitutesContainer = (LinearLayout) findViewById(R.id.substitutesContainer);
        LineupRenderer.render(context, pitchRows, substitutesContainer, lineUp.homeTeam, lineUp.awayTeam, new LineupRenderer.OnPlayerClick() {
            @Override
            public void onClick(String playerName) {
                Intent intent = new Intent(activity_live_commentry.this, player_profile_activity.class);
                intent.putExtra("playerName", playerName);
                startActivity(intent);
            }
        });
    }
}
