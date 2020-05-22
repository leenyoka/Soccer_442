package com.oms.lindanyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by linda.nyoka on 2015-03-23.
 */
public class activity_past_commentry extends ActionBarActivity {

        CommentryListAdapter commentaryListAdapter;
        ListView commentaryListView;
        Context context;
        String matchId;
        Result liveGame;
        Competition competition;
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
        competition = Competition.valueOf(new UserProfile(this).getFavourateLeague());
        liveGame = (Result) intent.getSerializableExtra("live");
        matchId = liveGame.matchId;
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


        dialog = new ProgressDialog(activity_past_commentry.this);
        dialog.setMessage("Getting comments for " + liveGame.HomeTeamName + " VS " + liveGame.AwayTeamName);
        dialog.setIndeterminate(true);
        dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                SuperSport superSport = new SuperSport();
                final Result comments; Result holder = null;
                final GameStats stats; GameStats statsHolder = null;
                final LineUp lineups; LineUp lineupsHolder = null;
                try {
                    holder = superSport.GetMatchDetails(liveGame, competition, liveGame.matchId);
                    statsHolder = superSport.GetStats(liveGame.matchId);
                    lineupsHolder = new SuperSport().GetLineUp(liveGame.matchId);
                }
                catch (Exception ex)
                {

                    utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
                    return;
                }

                comments = holder;
                stats = statsHolder;
                lineups = lineupsHolder;


                if(comments != null) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (comments != null) {
                                commentaryListView = (ListView) findViewById(R.id.liveGameCommentary);
                                commentaryListAdapter = new CommentryListAdapter(context, comments.Commentry);
                                commentaryListView.setAdapter(commentaryListAdapter);
                                TextView empty = (TextView) findViewById(R.id.comm_empty);
                                commentaryListView.setEmptyView(empty);

                                utility.GetMeAnImage((TextView) findViewById(R.id.comm_homeTeamImage), comments.HomeTeamName);
                                utility.GetMeAnImage((TextView) findViewById(R.id.comm_awayTeamImage), comments.AwayTeamName);

                                TextView score = (TextView) findViewById(R.id.comm_matchScore);
                                score.setText(String.valueOf(comments.HomeTeamScore) + " - "
                                        + String.valueOf(liveGame.AwayTeamScore));
                                TextView title = (TextView) findViewById(R.id.matchStatus);
                                title.setText("Result");

                                if (comments.HomeTeamScore != 0) {
                                    TextView scorersHome = (TextView) findViewById(R.id.homeTeamScorers);
                                    scorersHome.setText("");
                                    for (String scorer : comments.HomeTeamGoalScorers)
                                        scorersHome.setText(scorersHome.getText() + scorer);
                                    scorersHome.setText(utility.Trim(",", scorersHome.getText().toString()));
                                }

                                if (comments.AwayTeamScore != 0) {
                                    TextView scorersAway = (TextView) findViewById(R.id.awayTeamScorers);
                                    scorersAway.setText("");
                                    for (String scorer : comments.AwayTeamGoalScorers)
                                        scorersAway.setText(scorersAway.getText() + scorer);
                                    scorersAway.setText(utility.Trim(",", scorersAway.getText().toString()));
                                }

                                RelativeLayout scorers = (RelativeLayout) findViewById(R.id.showdescriptioncontenttitle);


                                //if (comments.AwayTeamScore == 0 && comments.HomeTeamScore == 0)
                                    //scorers.setVisibility(View.GONE);
                                //else if (comments.AwayTeamScore <= 2 && comments.HomeTeamScore <= 2) {
                                    //scorers.getLayoutParams().height = 50;
                                //} else
                                    //scorers.getLayoutParams().height = 100;
                            }
                            SetStats(stats);
                            SetLineUp(lineups);
                            dialog.hide();

                        }
                    });
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
    private void SetStats(GameStats stats)
    {
        ((TextView)findViewById(R.id.shotsHome)).setText(stats.home.Shots);
        ((TextView)findViewById(R.id.shotsAway)).setText(stats.away.Shots);

        ((TextView)findViewById(R.id.foulsHome)).setText(stats.home.Fouls);
        ((TextView)findViewById(R.id.foulsAway)).setText(stats.away.Fouls);

        ((TextView)findViewById(R.id.cornersHome)).setText(stats.home.Corners);
        ((TextView)findViewById(R.id.cornersAway)).setText(stats.away.Corners);

        ((TextView)findViewById(R.id.offsidesHome)).setText(stats.home.Offsides);
        ((TextView)findViewById(R.id.offsidesAway)).setText(stats.away.Offsides);

        ((TextView)findViewById(R.id.yellowHome)).setText(stats.home.Yellow);
        ((TextView)findViewById(R.id.yellowAway)).setText(stats.away.Yellow);

        ((TextView)findViewById(R.id.redHome)).setText(stats.home.Red);
        ((TextView)findViewById(R.id.redAway)).setText(stats.away.Red);
    }
    private void SetLineUp(LineUp lineUp)
    {
        ((TextView)findViewById(R.id.player1Home)).setText(lineUp.home.get(0).Position.toUpperCase() + " " + lineUp.home.get(0).Player);
        ((TextView)findViewById(R.id.player2Home)).setText(lineUp.home.get(1).Position.toUpperCase() + " " + lineUp.home.get(1).Player);
        ((TextView)findViewById(R.id.player3Home)).setText(lineUp.home.get(2).Position.toUpperCase() + " " + lineUp.home.get(2).Player);
        ((TextView)findViewById(R.id.player4Home)).setText(lineUp.home.get(3).Position.toUpperCase() + " " + lineUp.home.get(3).Player);
        ((TextView)findViewById(R.id.player5Home)).setText(lineUp.home.get(4).Position.toUpperCase() + " " + lineUp.home.get(4).Player);
        ((TextView)findViewById(R.id.player6Home)).setText(lineUp.home.get(5).Position.toUpperCase() + " " + lineUp.home.get(5).Player);
        ((TextView)findViewById(R.id.player7Home)).setText(lineUp.home.get(6).Position.toUpperCase() + " " + lineUp.home.get(6).Player);
        ((TextView)findViewById(R.id.player8Home)).setText(lineUp.home.get(7).Position.toUpperCase() + " " + lineUp.home.get(7).Player);
        ((TextView)findViewById(R.id.player9Home)).setText(lineUp.home.get(8).Position.toUpperCase() + " " + lineUp.home.get(8).Player);
        ((TextView)findViewById(R.id.player10Home)).setText(lineUp.home.get(9).Position.toUpperCase() + " " + lineUp.home.get(9).Player);
        ((TextView)findViewById(R.id.player11Home)).setText(lineUp.home.get(10).Position.toUpperCase() + " " + lineUp.home.get(10).Player);


        ((TextView)findViewById(R.id.player1Away)).setText(lineUp.away.get(0).Player.toUpperCase() + " " + lineUp.away.get(0).Position);
        ((TextView)findViewById(R.id.player2Away)).setText(lineUp.away.get(1).Player.toUpperCase() + " " + lineUp.away.get(1).Position);
        ((TextView)findViewById(R.id.player3Away)).setText(lineUp.away.get(2).Player.toUpperCase() + " " + lineUp.away.get(2).Position);
        ((TextView)findViewById(R.id.player4Away)).setText(lineUp.away.get(3).Player.toUpperCase() + " " + lineUp.away.get(3).Position);
        ((TextView)findViewById(R.id.player5Away)).setText(lineUp.away.get(4).Player.toUpperCase() + " " + lineUp.away.get(4).Position);
        ((TextView)findViewById(R.id.player6Away)).setText(lineUp.away.get(5).Player.toUpperCase() + " " + lineUp.away.get(5).Position);
        ((TextView)findViewById(R.id.player7Away)).setText(lineUp.away.get(6).Player.toUpperCase() + " " + lineUp.away.get(6).Position);
        ((TextView)findViewById(R.id.player8Away)).setText(lineUp.away.get(7).Player.toUpperCase() + " " + lineUp.away.get(7).Position);
        ((TextView)findViewById(R.id.player9Away)).setText(lineUp.away.get(8).Player.toUpperCase() + " " + lineUp.away.get(8).Position);
        ((TextView)findViewById(R.id.player10Away)).setText(lineUp.away.get(9).Player.toUpperCase() + " " + lineUp.away.get(9).Position);
        ((TextView)findViewById(R.id.player11Away)).setText(lineUp.away.get(10).Player.toUpperCase() + " " + lineUp.away.get(10).Position);
    }

}
