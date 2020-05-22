package com.oms.lindanyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by linda.nyoka on 2015-02-24.
 */
public class log_activity  extends ActionBarActivity {
    ListView logResultsListView;
    logListAdapter logListAdapter;
    Context context;
    Competition competition;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        getSupportActionBar().hide();
        context = this;
        try {
            competition = Competition.valueOf(new UserProfile(this).getFavourateLeague());
            ((TextView) findViewById(R.id.competitionName)).setText(competition.toString());
            ShowSavedLog();
            Initialize();
        }
        catch (Exception ex)
        {
            int x = 0;
        }

    }
    Utility utility = new Utility();
    public boolean ShowError()
    {
        if(utility.Connected(getApplicationContext()))
            return false;

        else
        {
            utility.ShowNetworkError((TextView)findViewById(R.id.title), "Log");
            return true;
        }
    }
    private void Initialize()
    {
        if(ShowError()) return;
        dialog = new ProgressDialog(log_activity.this);
        dialog.setMessage("Getting " + competition.toString() + " log standings");
        dialog.setIndeterminate(true);
        dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                SuperSport superSport = new SuperSport();
                final ArrayList<LogItem> results =superSport.GetLog(competition);
                runOnUiThread(new Runnable() {
                    public void run() {
                        dialog.hide();
                        new LeagueSavedState(context).SaveLog(competition, results);
                        ArrayList<LogItem> log = new LeagueSavedState(context).getLog(competition);
                        ShowLog(log, false);

                    }
                });
            }
        });
        eThread.start();
    }
    private void ShowLog(ArrayList<LogItem> results, boolean saved)
    {
        if(results != null) {
            //new LeagueSavedState(context).SaveLog(competition,results);
            ArrayList<LogItem> logItems = new LeagueSavedState(context).getLog(competition);
            logResultsListView = (ListView) findViewById(R.id.logListView);
            logListAdapter = new logListAdapter(context, logItems);
            logResultsListView.setAdapter(logListAdapter);
            TextView empty = (TextView) findViewById(R.id.comm_empty);
            logResultsListView.setEmptyView(empty);
            if(!saved)
            ((TextView)findViewById(R.id.title)).setText("Log(online)");
        }
        //else ShowSavedLog();

    }
    private void ShowSavedLog()
    {
        try {

            ArrayList<LogItem> logItems = new LeagueSavedState(context).getLog(competition);
            if(logItems != null)
                ShowLog(logItems,true);
            //else utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
        }
        catch (Exception ex)
        {
            int x = 0;
        }
    }
}
