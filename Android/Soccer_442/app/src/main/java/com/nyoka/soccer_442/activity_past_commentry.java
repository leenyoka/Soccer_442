package com.nyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nyoka.soccer_442.football_data.FootballData;
import com.nyoka.soccer_442.football_data.FootballMatch;
import com.nyoka.soccer_442.football_data.HomeOrAway;
import com.nyoka.soccer_442.football_data.MatchResponse;
import com.nyoka.soccer_442.football_data.Statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-03-23.
 */
public class activity_past_commentry extends AppCompatActivity {

        CommentryListAdapter commentaryListAdapter;
        ListView commentaryListView;
        Context context;
        String matchId;
        //Result liveGame;
        String competition;
        Utility utility = new Utility();
        ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_live_commentry);
        getSupportActionBar().hide();
        context = this;
        SetMatchId();
        Initialize();

    }

    private void SetMatchId() {
        Intent intent = getIntent();
        matchId = intent.getStringExtra("matchId");
        competition = new UserProfile(this).getFavourateLeague();
        //liveGame = (Result) intent.getSerializableExtra("live");
        //matchId = liveGame.matchId;
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


        //dialog = new ProgressDialog(activity_past_commentry.this);
        //dialog.setMessage("Getting comments for " + liveGame.HomeTeamName + " VS " + liveGame.AwayTeamName);
        //dialog.setIndeterminate(true);
        //dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                FootballData superSport = new FootballData();
                FootballMatch holder = null;
                final GameStats stats; GameStats statsHolder = null;
                //final LineUp lineups; LineUp lineupsHolder = null;
                try {
                    holder = superSport.GetMatchDetails( competition, matchId);
                    //statsHolder = superSport.GetStats(liveGame.matchId);
                    //lineupsHolder = new SuperSport().GetLineUp(liveGame.matchId);


                    //comments = holder;
                    //stats = statsHolder;
                    //lineups = lineupsHolder;

                    FootballMatch finalHolder = holder;
                    ArrayList<Comment> comments = holder.GetComments();

                    runOnUiThread(new Runnable() {
                            public void run() {
                                if (comments != null) {
                                    commentaryListView = (ListView) findViewById(R.id.liveGameCommentary);
                                    commentaryListAdapter = new CommentryListAdapter(context, comments);
                                    commentaryListView.setAdapter(commentaryListAdapter);
                                    TextView empty = (TextView) findViewById(R.id.comm_empty);
                                    commentaryListView.setEmptyView(empty);

                                    utility.GetMeAnImage((TextView) findViewById(R.id.comm_homeTeamImage), finalHolder.homeTeam.name);
                                    utility.GetMeAnImage((TextView) findViewById(R.id.comm_awayTeamImage), finalHolder.awayTeam.name);

                                    TextView score = (TextView) findViewById(R.id.comm_matchScore);
                                    score.setText(String.valueOf(finalHolder.score.fullTime.home) + " - "
                                            + String.valueOf(finalHolder.score.fullTime.away));
                                    TextView title = (TextView) findViewById(R.id.matchStatus);
                                    title.setText("Result");

                                    if (finalHolder.score.fullTime.home != 0) {
                                        TextView scorersHome = (TextView) findViewById(R.id.homeTeamScorers);
                                        scorersHome.setText("");
                                        for (String scorer : finalHolder.GetScorers(HomeOrAway.Home))
                                            if(scorer != null) {
                                                scorersHome.setText(scorersHome.getText() + scorer);
                                                scorersHome.setText(utility.Trim(",", scorersHome.getText().toString()));
                                            }
                                    }

                                    if (finalHolder.score.fullTime.away != 0) {
                                        TextView scorersAway = (TextView) findViewById(R.id.awayTeamScorers);
                                        scorersAway.setText("");
                                        for (String scorer : finalHolder.GetScorers(HomeOrAway.Away))
                                            if(scorer != null) {
                                                scorersAway.setText(scorersAway.getText() + scorer);
                                                scorersAway.setText(utility.Trim(",", scorersAway.getText().toString()));
                                            }
                                    }

                                    RelativeLayout scorers = (RelativeLayout) findViewById(R.id.showdescriptioncontenttitle);


                                    //if (comments.AwayTeamScore == 0 && comments.HomeTeamScore == 0)
                                    //scorers.setVisibility(View.GONE);
                                    //else if (comments.AwayTeamScore <= 2 && comments.HomeTeamScore <= 2) {
                                    //scorers.getLayoutParams().height = 50;
                                    //} else
                                    //scorers.getLayoutParams().height = 100;
                                }

                                if (finalHolder != null) {
                                    SetStats(finalHolder.homeTeam.statistics, finalHolder.awayTeam.statistics);
                                }
                                SetLineUp(finalHolder);
                                //dialog.hide();
                            }

                        });


                }
                catch (Exception ex)
                    {

                        utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
                        return;
                    }
                }

        });
        eThread.start();
    }
    public void Commentary(View view)
    {
        ((ListView)findViewById(R.id.liveGameCommentary)).setVisibility(View.VISIBLE);
        ((LinearLayout)findViewById(R.id.LineUpView)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.statsView)).setVisibility(View.GONE);


        ((Button)findViewById(R.id.btnCommentary)).setBackgroundResource(R.drawable.green_button_selected);
        ((Button)findViewById(R.id.btnStats)).setBackgroundResource(R.drawable.green_button);
        ((Button)findViewById(R.id.button1)).setBackgroundResource(R.drawable.green_button);
    }
    public void Stats(View view)
    {
        ((ListView)findViewById(R.id.liveGameCommentary)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.LineUpView)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.statsView)).setVisibility(View.VISIBLE);

        ((Button)findViewById(R.id.btnCommentary)).setBackgroundResource(R.drawable.green_button);
        ((Button)findViewById(R.id.btnStats)).setBackgroundResource(R.drawable.green_button_selected);
        ((Button)findViewById(R.id.button1)).setBackgroundResource(R.drawable.green_button);
    }
    public void LineUp(View view)
    {
        ((ListView)findViewById(R.id.liveGameCommentary)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.LineUpView)).setVisibility(View.VISIBLE);
        ((LinearLayout)findViewById(R.id.statsView)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.btnCommentary)).setBackgroundResource(R.drawable.green_button);
        ((Button)findViewById(R.id.btnStats)).setBackgroundResource(R.drawable.green_button);
        ((Button)findViewById(R.id.button1)).setBackgroundResource(R.drawable.green_button_selected);
    }
    private void SetStats(Statistics home, Statistics away)
    {
        if(home != null && away != null) {
            ((TextView) findViewById(R.id.shotsHome)).setText(home.shots);
            ((TextView) findViewById(R.id.shotsAway)).setText(away.shots);

            ((TextView) findViewById(R.id.foulsHome)).setText(home.fouls);
            ((TextView) findViewById(R.id.foulsAway)).setText(away.fouls);

            ((TextView) findViewById(R.id.cornersHome)).setText(home.corner_kicks);
            ((TextView) findViewById(R.id.cornersAway)).setText(away.corner_kicks);

            ((TextView) findViewById(R.id.offsidesHome)).setText(home.offsides);
            ((TextView) findViewById(R.id.offsidesAway)).setText(away.offsides);

            ((TextView) findViewById(R.id.yellowHome)).setText(home.yellow_cards);
            ((TextView) findViewById(R.id.yellowAway)).setText(away.yellow_cards);

            ((TextView) findViewById(R.id.redHome)).setText(home.red_cards);
            ((TextView) findViewById(R.id.redAway)).setText(away.red_cards);
        }
    }
    private void SetLineUp(FootballMatch lineUp) {
        if (lineUp.homeTeam.lineup != null) {
            ((TextView) findViewById(R.id.player1Home)).setText(lineUp.homeTeam.lineup.get(0).position.toUpperCase() + " " + lineUp.homeTeam.lineup.get(0).name);
            ((TextView) findViewById(R.id.player2Home)).setText(lineUp.homeTeam.lineup.get(1).position.toUpperCase() + " " + lineUp.homeTeam.lineup.get(1).name);
            ((TextView) findViewById(R.id.player3Home)).setText(lineUp.homeTeam.lineup.get(2).position.toUpperCase() + " " + lineUp.homeTeam.lineup.get(2).name);
            ((TextView) findViewById(R.id.player4Home)).setText(lineUp.homeTeam.lineup.get(3).position.toUpperCase() + " " + lineUp.homeTeam.lineup.get(3).name);
            ((TextView) findViewById(R.id.player5Home)).setText(lineUp.homeTeam.lineup.get(4).position.toUpperCase() + " " + lineUp.homeTeam.lineup.get(4).name);
            ((TextView) findViewById(R.id.player6Home)).setText(lineUp.homeTeam.lineup.get(5).position.toUpperCase() + " " + lineUp.homeTeam.lineup.get(5).name);
            ((TextView) findViewById(R.id.player7Home)).setText(lineUp.homeTeam.lineup.get(6).position.toUpperCase() + " " + lineUp.homeTeam.lineup.get(6).name);
            ((TextView) findViewById(R.id.player8Home)).setText(lineUp.homeTeam.lineup.get(7).position.toUpperCase() + " " + lineUp.homeTeam.lineup.get(7).name);
            ((TextView) findViewById(R.id.player9Home)).setText(lineUp.homeTeam.lineup.get(8).position.toUpperCase() + " " + lineUp.homeTeam.lineup.get(8).name);
            ((TextView) findViewById(R.id.player10Home)).setText(lineUp.homeTeam.lineup.get(9).position.toUpperCase() + " " + lineUp.homeTeam.lineup.get(9).name);
            ((TextView) findViewById(R.id.player11Home)).setText(lineUp.homeTeam.lineup.get(10).position.toUpperCase() + " " + lineUp.homeTeam.lineup.get(10).name);
        }
        if ( lineUp.awayTeam.lineup != null) {
            ((TextView) findViewById(R.id.player1Away)).setText(lineUp.awayTeam.lineup.get(0).name.toUpperCase() + " " + lineUp.awayTeam.lineup.get(0).position);
            ((TextView) findViewById(R.id.player2Away)).setText(lineUp.awayTeam.lineup.get(1).name.toUpperCase() + " " + lineUp.awayTeam.lineup.get(1).position);
            ((TextView) findViewById(R.id.player3Away)).setText(lineUp.awayTeam.lineup.get(2).name.toUpperCase() + " " + lineUp.awayTeam.lineup.get(2).position);
            ((TextView) findViewById(R.id.player4Away)).setText(lineUp.awayTeam.lineup.get(3).name.toUpperCase() + " " + lineUp.awayTeam.lineup.get(3).position);
            ((TextView) findViewById(R.id.player5Away)).setText(lineUp.awayTeam.lineup.get(4).name.toUpperCase() + " " + lineUp.awayTeam.lineup.get(4).position);
            ((TextView) findViewById(R.id.player6Away)).setText(lineUp.awayTeam.lineup.get(5).name.toUpperCase() + " " + lineUp.awayTeam.lineup.get(5).position);
            ((TextView) findViewById(R.id.player7Away)).setText(lineUp.awayTeam.lineup.get(6).name.toUpperCase() + " " + lineUp.awayTeam.lineup.get(6).position);
            ((TextView) findViewById(R.id.player8Away)).setText(lineUp.awayTeam.lineup.get(7).name.toUpperCase() + " " + lineUp.awayTeam.lineup.get(7).position);
            ((TextView) findViewById(R.id.player9Away)).setText(lineUp.awayTeam.lineup.get(8).name.toUpperCase() + " " + lineUp.awayTeam.lineup.get(8).position);
            ((TextView) findViewById(R.id.player10Away)).setText(lineUp.awayTeam.lineup.get(9).name.toUpperCase() + " " + lineUp.awayTeam.lineup.get(9).position);
            ((TextView) findViewById(R.id.player11Away)).setText(lineUp.awayTeam.lineup.get(10).name.toUpperCase() + " " + lineUp.awayTeam.lineup.get(10).position);
        }
    }

}
