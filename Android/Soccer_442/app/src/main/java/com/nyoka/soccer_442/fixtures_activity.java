package com.nyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.nyoka.soccer_442.football_data.FootballData;
import com.nyoka.soccer_442.football_data.FootballMatch;
import com.nyoka.soccer_442.football_data.MatchResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-23.
 */
public class fixtures_activity extends AppCompatActivity  {
    ListView fixtureResultsListView;
    fixtureListAdapter fixtureListAdapter;
    Context context;
    String competition;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixtures);
        Utility.ApplyEdgeToEdgeInsets(this);
        getSupportActionBar().hide();
        context = this;
        competition = new UserProfile(this).getFavourateLeague();
        ShowSavedFixtures();
        Initialize();

    }
    Utility utility = new Utility();
    public boolean ShowError()
    {
        if(utility.Connected(getApplicationContext()))
            return false;

        else
        {
            utility.ShowNetworkError((TextView)findViewById(R.id.title),"Fixtures");
            return true;
        }
    }
    private void Initialize()
    {
        if(ShowError())return;

        // #26: My Teams mode - merged fixtures across every competition a supported team is
        // in, instead of one competition at a time.
        final List<String> supportedTeams = new UserProfile(this).getSupportedTeams();
        if ("My Teams".equals(competition) && !supportedTeams.isEmpty()) {
            ((TextView) findViewById(R.id.competitionName)).setText("My Teams");
            Utility.ShowLoading(this);
            final Thread myTeamsThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    final MyTeamsData.MyTeamsResult result = new MyTeamsData().load(supportedTeams);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Utility.HideLoading(fixtures_activity.this);
                            ShowFixtures(result.fixtures, false);
                        }
                    });
                }
            });
            myTeamsThread.start();
            return;
        }

        ((TextView) findViewById(R.id.competitionName)).setText(competition.toString());
        Utility.ShowLoading(this);
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                FootballData superSport = new FootballData();
                final MatchResponse results =superSport.GetFixture(competition);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Utility.HideLoading(fixtures_activity.this);
                        ShowFixtures(results.matches,false);
                        new LeagueSavedState(context).SaveFixtures(competition,results.matches);

                    }
                });
            }
        });
        eThread.start();
    }
    private void ShowFixtures(List<FootballMatch> results, boolean saved)
    {
        if(results != null) {
            fixtureResultsListView = (ListView) findViewById(R.id.fixtureListView);
            // Soonest fixture first, so the next match to be played is at the top.
            Collections.sort(results, Comparator.comparing(FootballMatch::GetDate, Comparator.nullsLast(Comparator.naturalOrder())));
            fixtureListAdapter = new fixtureListAdapter(context, results, "");
            fixtureResultsListView.setAdapter(fixtureListAdapter);
            TextView empty = (TextView) findViewById(R.id.comm_empty);
            fixtureResultsListView.setEmptyView(empty);
            fixtureResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    FootballMatch match = (FootballMatch) fixtureListAdapter.getItem(position);
                    Intent intent = new Intent(context, head_to_head_activity.class);
                    // My Teams merges matches from several competitions - each match already
                    // knows its own competition (FootballMatch.competition), which is always
                    // correct, unlike the single favourite-league competition variable that
                    // only applies in the normal single-competition mode.
                    String matchCompetition = match.competition != null && match.competition.name != null
                            ? match.competition.name : competition;
                    intent.putExtra("competition", matchCompetition);
                    intent.putExtra("matchId", String.valueOf(match.id));
                    intent.putExtra("homeTeamName", match.homeTeam.name);
                    intent.putExtra("awayTeamName", match.awayTeam.name);
                    startActivity(intent);
                }
            });
            if(!saved) {
                ((TextView)findViewById(R.id.title)).setText("Fixtures(online)");
            }

            if(results.size() == 0)
                utility.showDialog("There are no fixtures for " + competition.toString(), false, "No fixtures",getSupportFragmentManager());

        } //else ShowSavedFixtures();
    }
    private void ShowSavedFixtures()
    {
        try {

            //List<FootballMatch> logItems = new LeagueSavedState(context).getFixtures(competition);
            //if(logItems != null)
              //  ShowFixtures(logItems,true);
            //else utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
        }
        catch (Exception ex)
        {
            int x = 0;
        }
    }
}
