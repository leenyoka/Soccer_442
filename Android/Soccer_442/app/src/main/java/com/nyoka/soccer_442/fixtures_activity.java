package com.nyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.nyoka.soccer_442.football_data.FootballData;
import com.nyoka.soccer_442.football_data.FootballMatch;
import com.nyoka.soccer_442.football_data.MatchResponse;

import java.util.ArrayList;
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
        getSupportActionBar().hide();
        context = this;
        competition = new UserProfile(this).getFavourateLeague();
        ((TextView) findViewById(R.id.competitionName)).setText(competition.toString());
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

        dialog = new ProgressDialog(fixtures_activity.this);
        dialog.setMessage("Getting " + competition.toString() + " fixtures");
        dialog.setIndeterminate(true);
        dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                FootballData superSport = new FootballData();
                final MatchResponse results =superSport.GetFixture(competition);
                runOnUiThread(new Runnable() {
                    public void run() {
                        dialog.hide();
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
            fixtureListAdapter = new fixtureListAdapter(context, results, "");
            fixtureResultsListView.setAdapter(fixtureListAdapter);
            TextView empty = (TextView) findViewById(R.id.comm_empty);
            fixtureResultsListView.setEmptyView(empty);
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
