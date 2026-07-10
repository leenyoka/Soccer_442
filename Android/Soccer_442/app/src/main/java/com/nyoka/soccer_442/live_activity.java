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
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-24.
 */
public class live_activity  extends AppCompatActivity {
    ListView liveReesultsListView;
    liveListAdapter liveListAdapter;
    Context context;
    String competition;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        Utility.ApplyEdgeToEdgeInsets(this);
        getSupportActionBar().hide();
        context = this;
        competition = new UserProfile(this).getFavourateLeague();
        Initialize();

    }
    Utility utility = new Utility();
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
    private void Initialize()
    {
        if(ShowError())return;

        // #26: My Teams mode - merged live matches across every competition a supported
        // team is in, instead of one competition at a time.
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
                            Utility.HideLoading(live_activity.this);
                            ShowLive(result.live);
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
                final MatchResponse results =superSport.GetLive(competition);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Utility.HideLoading(live_activity.this);
                        if (results != null) {
                            ShowLive(results.matches);
                        } else {
                            utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
                        }
                    }
                });

            }
        });
        eThread.start();
    }
    private void ShowLive(List<FootballMatch> matches) {
        liveReesultsListView = (ListView) findViewById(R.id.liveListView);
        liveListAdapter = new liveListAdapter(context, matches, "");
        liveReesultsListView.setAdapter(liveListAdapter);
        TextView empty = (TextView) findViewById(R.id.comm_empty);
        liveReesultsListView.setEmptyView(empty);

        liveReesultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                FootballMatch live = (FootballMatch) liveListAdapter.getItem(position);

                GoToCommentry(live);
            }
        });

        if (matches.size() == 0) {
            utility.showDialog("There are no live games right now", false, "No live games", getSupportFragmentManager());
        }
    }
    private void GoToCommentry(FootballMatch match)
    {
        Intent intent = new Intent(this, activity_live_commentry.class);
        //intent.putExtra("competition", Competition.Absa);
        intent.putExtra("matchId", String.valueOf( match.id));
        //intent.putExtra("live",match);
        startActivity(intent);
    }
}
