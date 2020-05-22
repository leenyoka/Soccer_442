package com.oms.lindanyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by linda.nyoka on 2015-02-23.
 */
public class fixtures_activity extends ActionBarActivity {
    ListView fixtureResultsListView;
    fixtureListAdapter fixtureListAdapter;
    Context context;
    Competition competition;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixtures);
        getSupportActionBar().hide();
        context = this;
        competition = Competition.valueOf(new UserProfile(this).getFavourateLeague());
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

                SuperSport superSport = new SuperSport();
                final ArrayList<Fixture> results =superSport.GetFixture(competition);
                runOnUiThread(new Runnable() {
                    public void run() {
                        dialog.hide();
                        ShowFixtures(results,false);
                        new LeagueSavedState(context).SaveFixtures(competition,results);

                    }
                });
            }
        });
        eThread.start();
    }
    private void ShowFixtures(ArrayList<Fixture> results, boolean saved)
    {
        if(results != null) {
            fixtureResultsListView = (ListView) findViewById(R.id.fixtureListView);
            fixtureListAdapter = new fixtureListAdapter(context, results, "");
            fixtureResultsListView.setAdapter(fixtureListAdapter);
            TextView empty = (TextView) findViewById(R.id.comm_empty);
            fixtureResultsListView.setEmptyView(empty);
            if(!saved)
            ((TextView)findViewById(R.id.title)).setText("Fixtures(online)");

            if(results.size() == 0)
                utility.showDialog("There are no fixtures for " + competition.toString(), false, "No fixtures",getSupportFragmentManager());

        } //else ShowSavedFixtures();
    }
    private void ShowSavedFixtures()
    {
        try {

            ArrayList<Fixture> logItems = new LeagueSavedState(context).getFixtures(competition);
            if(logItems != null)
                ShowFixtures(logItems,true);
            //else utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
        }
        catch (Exception ex)
        {
            int x = 0;
        }
    }
}
