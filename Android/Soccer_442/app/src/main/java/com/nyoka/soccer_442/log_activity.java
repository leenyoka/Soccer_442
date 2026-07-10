package com.nyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.nyoka.soccer_442.football_data.FootballData;
import com.nyoka.soccer_442.football_data.StandingsItem;
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
    StandingsResponse currentStandings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        Utility.ApplyEdgeToEdgeInsets(this);
        getSupportActionBar().hide();
        context = this;
        try {
            competition = new UserProfile(this).getFavourateLeague();
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

        // #26 (revised): "My Teams" is picked from the dashboard's main dropdown like any
        // other competition, not a separate settings toggle - competition holds the literal
        // string "My Teams" when that's what was selected (see summary_activity's
        // MY_TEAMS_OPTION/competitions[]). When it is (and at least one team is actually
        // configured; otherwise there'd be nothing to show) this screen shows each
        // supported team's own row from every competition it's in, merged, instead of one
        // competition's full table.
        List<String> supportedTeams = new UserProfile(this).getSupportedTeams();
        if ("My Teams".equals(competition) && !supportedTeams.isEmpty()) {
            InitializeMyTeams(supportedTeams);
            return;
        }

        ((TextView) findViewById(R.id.competitionName)).setText(competition);
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                FootballData superSport = new FootballData();
                final String current = competition;
                final StandingsResponse results =superSport.GetLog(current);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Utility.HideLoading(log_activity.this);
                        //new LeagueSavedState(context).SaveLog(competition, results.standings.get(0).table);
                        //ArrayList<TableItem> log = new LeagueSavedState(context).getLog(competition);
                        currentStandings = results;
                        SetupGroupSelector(results);
                    }
                });
            }
        });
        Utility.ShowLoading(this);
        eThread.start();
    }

    private void InitializeMyTeams(final List<String> supportedTeams) {
        ((TextView) findViewById(R.id.competitionName)).setText("My Teams");
        findViewById(R.id.groupSpinnerRow).setVisibility(View.GONE);
        Utility.ShowLoading(this);
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final MyTeamsData.MyTeamsResult result = new MyTeamsData().load(supportedTeams);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Utility.HideLoading(log_activity.this);
                        ShowMyTeamsLog(result.standings);
                    }
                });
            }
        });
        eThread.start();
    }

    // #26: "one row per competition [a team is in], and add a competition column" - each
    // MyTeamsData.TeamStanding is already exactly that pairing (one team's row from one
    // competition's table); this just splits it into the parallel lists logListAdapter's
    // My Teams constructor overload expects.
    private void ShowMyTeamsLog(List<MyTeamsData.TeamStanding> standings) {
        List<TableItem> items = new ArrayList<>();
        List<String> competitionCodes = new ArrayList<>();
        for (MyTeamsData.TeamStanding ts : standings) {
            items.add(ts.entry);
            competitionCodes.add(ts.competition.code);
        }

        logResultsListView = (ListView) findViewById(R.id.logListView);
        logListAdapter = new logListAdapter(context, items, competitionCodes);
        logResultsListView.setAdapter(logListAdapter);
        TextView empty = (TextView) findViewById(R.id.comm_empty);
        logResultsListView.setEmptyView(empty);
        ((TextView) findViewById(R.id.title)).setText("Log(online)");
    }
    /**
     * Cup competitions (World Cup, etc.) come back with one table per group ("Group A".."Group H")
     * instead of a single league table - show a spinner to switch between them when there's more
     * than one, and just skip straight to the table for the normal single-table case.
     */
    private void SetupGroupSelector(StandingsResponse results)
    {
        if (results == null || results.standings == null || results.standings.isEmpty()) {
            ShowLog(new ArrayList<TableItem>(), false);
            return;
        }

        View groupSpinnerRow = findViewById(R.id.groupSpinnerRow);
        if (results.standings.size() <= 1) {
            groupSpinnerRow.setVisibility(View.GONE);
            ShowLog(results.standings.get(0).table, false);
            return;
        }

        List<String> groupLabels = new ArrayList<>();
        for (int i = 0; i < results.standings.size(); i++) {
            StandingsItem stage = results.standings.get(i);
            String label = stage.group != null ? String.valueOf(stage.group) : "Group " + (i + 1);
            groupLabels.add(label);
        }

        Spinner spinner = (Spinner) findViewById(R.id.groupSpinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, groupLabels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ShowLog(currentStandings.standings.get(position).table, false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // #28: default to whichever group a supported team is actually in, rather than
        // always landing on the first group - a user following a team in Group D shouldn't
        // have to know to go find it themselves every time this screen opens.
        int myTeamGroupIndex = findMyTeamGroupIndex(results.standings);
        if (myTeamGroupIndex >= 0) {
            spinner.setSelection(myTeamGroupIndex);
        }
        groupSpinnerRow.setVisibility(View.VISIBLE);
        // spinner.setOnItemSelectedListener fires immediately for the initial selection
        // (whether that's index 0 or the match above), which already calls ShowLog - no
        // separate initial ShowLog call needed here.
    }

    private int findMyTeamGroupIndex(List<StandingsItem> stages) {
        List<String> supportedTeams = new UserProfile(this).getSupportedTeams();
        if (supportedTeams.isEmpty()) return -1;

        for (int i = 0; i < stages.size(); i++) {
            List<TableItem> table = stages.get(i).table;
            if (table == null) continue;
            for (TableItem item : table) {
                if (item.team == null) continue;
                for (String supported : supportedTeams) {
                    if (MyTeamsData.namesLikelyMatch(item.team.name, supported)) return i;
                }
            }
        }
        return -1;
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
