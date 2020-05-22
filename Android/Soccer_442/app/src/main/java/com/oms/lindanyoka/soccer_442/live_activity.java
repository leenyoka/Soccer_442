package com.oms.lindanyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by linda.nyoka on 2015-02-24.
 */
public class live_activity  extends ActionBarActivity {
    ListView liveReesultsListView;
    liveListAdapter liveListAdapter;
    Context context;
    Competition competition;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        getSupportActionBar().hide();
        context = this;
        competition = Competition.valueOf(new UserProfile(this).getFavourateLeague());
        ((TextView) findViewById(R.id.competitionName)).setText(competition.toString());
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
        dialog = new ProgressDialog(live_activity.this);
        dialog.setMessage("Getting " + competition.toString() + " live games");
        dialog.setIndeterminate(true);
        dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                SuperSport superSport = new SuperSport();
                final ArrayList<Live> results =superSport.GetLive(competition);
                runOnUiThread(new Runnable() {
                    public void run() {
                        dialog.hide();

                        if(results != null) {
                            liveReesultsListView = (ListView) findViewById(R.id.liveListView);
                            liveListAdapter = new liveListAdapter(context, results, "");
                            liveReesultsListView.setAdapter(liveListAdapter);
                            TextView empty = (TextView) findViewById(R.id.comm_empty);
                            liveReesultsListView.setEmptyView(empty);

                            liveReesultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                    Live live = (Live) liveListAdapter.getItem(position);

                                    GoToCommentry(live);
                                }
                            });

                            if(results.size() == 0)
                                utility.showDialog("There are no live games for " + competition.toString(), false, "No live games",getSupportFragmentManager());
                        }
                        else utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());

                    }
                });
            }
        });
        eThread.start();
    }
    private void GoToCommentry(Live match)
    {
        Intent intent = new Intent(this, activity_live_commentry.class);
        intent.putExtra("competition", Competition.Absa);
        intent.putExtra("matchId", String.valueOf( match.MatchId));
        intent.putExtra("live",match);
        startActivity(intent);
    }
}
