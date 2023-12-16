package com.nyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.nyoka.soccer_442.football_data.FootballData;
import com.nyoka.soccer_442.football_data.StandingsResponse;
import com.nyoka.soccer_442.football_data.TableItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-24.
 */
public class log_activity  extends AppCompatActivity {
    ListView logResultsListView;
    logListAdapter logListAdapter;
    Context context;
    String competition;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        getSupportActionBar().hide();
        context = this;
        try {
            competition = new UserProfile(this).getFavourateLeague();
            //((TextView) findViewById(R.id.competitionName)).setText(competition.toString());
            //ShowSavedLog();
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
        //dialog = new ProgressDialog(log_activity.this);
        //dialog.setMessage("Getting " + competition.toString() + " log standings");
        //dialog.setIndeterminate(true);
        //dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                FootballData superSport = new FootballData();
                final String current = competition;
                final StandingsResponse results =superSport.GetLog(current);
                runOnUiThread(new Runnable() {
                    public void run() {
                        //dialog.hide();
                        //new LeagueSavedState(context).SaveLog(competition, results.standings.get(0).table);
                        //ArrayList<TableItem> log = new LeagueSavedState(context).getLog(competition);
                        ShowLog(results.standings.get(0).table, false);

                    }
                });
            }
        });
        eThread.start();
    }
    private void ShowLog(List<TableItem> results, boolean saved)
    {
        if(results != null) {
            //new LeagueSavedState(context).SaveLog(competition,results);
            //ArrayList<TableItem> logItems = new LeagueSavedState(context).getLog(competition);
            logResultsListView = (ListView) findViewById(R.id.logListView);
            logListAdapter = new logListAdapter(context, results);
            logResultsListView.setAdapter(logListAdapter);
            TextView empty = (TextView) findViewById(R.id.comm_empty);
            logResultsListView.setEmptyView(empty);
            if(!saved) {
                ((TextView)findViewById(R.id.title)).setText("Log(online)");
            }
        }
        //else ShowSavedLog();

    }

}
