package com.nyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.nyoka.soccer_442.football_data.FootballData;
import com.nyoka.soccer_442.football_data.FootballMatch;
import com.nyoka.soccer_442.football_data.MatchResponse;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-23.
 */
public class results_activity extends AppCompatActivity {
    ListView resultsListView;
    resultsListAdapter resultsListAdapter;
    Context context;
    String competition;
    ProgressDialog dialog;

    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        getSupportActionBar().hide();
        context = this;
        competition = new UserProfile(this).getFavourateLeague();
        ((TextView) findViewById(R.id.competitionName)).setText(competition.toString());
        ShowSavedResults();
        Initialize();

    }
    Utility utility = new Utility();
    public boolean ShowError()
    {
        if(utility.Connected(getApplicationContext()))
            return false;

        else
        {
            utility.ShowNetworkError((TextView)findViewById(R.id.title), "Results");
            return true;
        }
    }
    private void Initialize()
    {
        if(ShowError()) return;
        dialog = new ProgressDialog(results_activity.this);
        dialog.setMessage("Getting " + competition.toString() + " results");
        dialog.setIndeterminate(true);
        dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                FootballData superSport = new FootballData();
                final MatchResponse results =superSport.GetResults(competition);
                runOnUiThread(new Runnable() {
                    public void run() {
                        dialog.hide();
                        ShowResults(results.matches,false);
                        new LeagueSavedState(context).SaveResults(competition,results.matches);
                    }
                });
            }
        });
        eThread.start();
    }
    private void ShowResults(List<FootballMatch> results, boolean saved)
    {
        if(results != null) {
            resultsListView = (ListView) findViewById(R.id.resultsListView);
            Collections.sort(results, Comparator.comparing(FootballMatch::GetDate).reversed());
            resultsListAdapter = new resultsListAdapter(context, results, "");
            resultsListView.setAdapter(resultsListAdapter);
            TextView empty = (TextView) findViewById(R.id.comm_empty);
            resultsListView.setEmptyView(empty);
            if(!saved) {
                ((TextView)findViewById(R.id.title)).setText("Results(online)");
            }

            resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    FootballMatch live = (FootballMatch) resultsListAdapter.getItem(position);

                    //GoToCommentry(live);
                }
            });

            if(results.size() == 0)
                utility.showDialog("There are no results to show", false, "No results",getSupportFragmentManager());
        }
        //else utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());

    }
    private void ShowSavedResults()
    {
        try {

            //ArrayList<Result> logItems = new LeagueSavedState(context).getResults(competition);
            //if(logItems != null)
              //  ShowResults(logItems,true);
            //else utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
        }
        catch (Exception ex)
        {
            int x = 0;
        }
    }
    private void GoToCommentry(FootballMatch match)
    {
        if(match.id > 0 ) {
            Intent intent = new Intent(this, activity_past_commentry.class);
            //intent.putExtra("competition", Competition.Absa);
            intent.putExtra("matchId", String.valueOf(match.id));
            //intent.putExtra("live", match);
            startActivity(intent);
        }
    }
}
