package com.nyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.nyoka.soccer_442.football_data.FootballData;
import com.nyoka.soccer_442.football_data.Scorer;
import com.nyoka.soccer_442.football_data.ScorerResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-25.
 */
public class scorer_activity   extends AppCompatActivity {
    ListView scorerResultsListView;
    scorerListAdapter scorerListAdapter;
    Context context;
    String competition;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorer);
        getSupportActionBar().hide();
        context = this;
        try {
            competition = new UserProfile(this).getFavourateLeague();
            ((TextView) findViewById(R.id.competitionName)).setText(competition.toString());
            ShowSavedScorers();
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
            utility.ShowNetworkError((TextView)findViewById(R.id.title),"Fixtures");
            return true;
        }
    }
    private void Initialize()
    {
        if(ShowError())
            return;

        dialog = new ProgressDialog(scorer_activity.this);
        dialog.setMessage("Getting " + competition.toString() + " top scorers ");
        dialog.setIndeterminate(true);
        dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {


                FootballData superSport = new
                        FootballData();
                final ScorerResponse results =superSport.GetScorers(competition);
                runOnUiThread(new Runnable() {
                    public void run() {
                        dialog.hide();
                        ShowScorers(results.scorers, false);
                        new LeagueSavedState(context).SaveTopScorers(competition,results.scorers);
                    }
                });
            }
        });
        eThread.start();
    }
    private void ShowSavedScorers()
    {
        try {

            //ArrayList<TopGoalScorer> logItems = new LeagueSavedState(context).getScorers(competition);
            //if(logItems != null)
                //ShowScorers(logItems,true);
            //else utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
        }
        catch (Exception ex)
        {
            int x = 0;
        }
    }
    private void ShowScorers(List<Scorer> results, boolean saved)
    {
        if(results != null) {
            scorerResultsListView = (ListView) findViewById(R.id.scorerListView);
            scorerListAdapter = new scorerListAdapter(context, results);
            scorerResultsListView.setAdapter(scorerListAdapter);
            //TextView empty = (TextView) findViewById(R.id.comm_empty);
            //scorerResultsListView.setEmptyView(empty);

            if(!saved)
                ((TextView)findViewById(R.id.title)).setText("Goal Scorers(online)");

            if(results.size() == 0)
                utility.showDialog("There are no goal scorers at this point", false, "No scorers",getSupportFragmentManager());
        }
        //else ShowSavedScorers();

    }
}
