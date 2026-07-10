package com.nyoka.soccer_442;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.nyoka.soccer_442.football_data.FootballData;
import com.nyoka.soccer_442.football_data.FootballMatch;
import com.nyoka.soccer_442.football_data.MatchResponse;
import com.nyoka.soccer_442.football_data.Scorer;
import com.nyoka.soccer_442.football_data.ScorerResponse;
import com.nyoka.soccer_442.football_data.StandingsResponse;
import com.nyoka.soccer_442.football_data.TableItem;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-05-10.
 */
public class summary_activity extends Activity {
    ListView logResultsListView;
    logListAdapter logListAdapter;
    ListView scorerResultsListView;
    scorerListAdapter scorerListAdapter;
    ListView resultsListView;
    resultsListAdapter resultsListAdapter;
    ListView fixtureResultsListView;
    fixtureListAdapter fixtureListAdapter;
    ListView liveReesultsListView;
    liveListAdapter liveListAdapter;
    Context context;
    String competition;
    UserProfile profile;
    private StateGuy _stateGuy;
    boolean update;
    private WorkerAssist worker;

    // #20: this screen kicks off 6 sections (Log/Scorers/Results/Fixtures/Live/News)
    // concurrently, each on its own background thread, all sharing one topProgressBar - a
    // naive ShowLoading-at-start/HideLoading-at-end pair per section means whichever section
    // finishes first hides the bar while the other five (some of which previously had no
    // loading call at all) are still fetching, so the bar barely flashes if it shows at all.
    // Counting how many sections are still in flight and only hiding the bar once it's back
    // to zero makes it accurately reflect "is anything still loading", not "did the section
    // that happens to hide it last just finish".
    private int pendingLoads = 0;

    private synchronized void beginLoad() {
        pendingLoads++;
        if (pendingLoads == 1) Utility.ShowLoading(this);
    }

    private synchronized void endLoad() {
        pendingLoads = Math.max(0, pendingLoads - 1);
        if (pendingLoads == 0) Utility.HideLoading(this);
    }

    // #26 (revised): "My Teams" is picked from this same dropdown as just another entry,
    // not a separate Settings toggle - MY_TEAMS_OPTION is checked wherever the app used to
    // check UserProfile.getMyTeamsModeEnabled(). Kept distinct from any real competition
    // name Since it flows through the exact same `competition` string used everywhere else
    // (including the temporary favourite-league swap Logs()/Results()/etc. already do when
    // navigating to a full-screen view), no other plumbing needed changing to support it.
    private static final String MY_TEAMS_OPTION = "My Teams";
    private static final String[] competitions = {
            MY_TEAMS_OPTION,
            "Championship",
            "Premier League",
            "UEFA Champions League",
            "European Championship",
            "Ligue 1",
            "Bundesliga",
            "Serie A",
            "Eredivisie",
            "Primeira Liga",
            "Copa Libertadores",
            "Primera Division",
            "FIFA World Cup"
    };

    //ProgressDialog dialog;
    // #25: without this, the dashboard only ever fetches once at onCreate - leaving the app
    // open (or backgrounding and returning to it) never refreshes it, so a match that finishes
    // while the user isn't actively reloading the screen never appears as "the latest result"
    // until the app is fully restarted. onResume() below re-fetches on every resume after the
    // first (the first onResume, immediately following onCreate on initial launch, is skipped -
    // onCreate's own Start() call just did the same fetch, a second one back to back would be
    // redundant).
    private boolean hasResumedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiity_summary);
        Utility.ApplyEdgeToEdgeInsets(this);
        //getSupportActionBar().hide();
        Start();
    }
    private void Start()
    {
        profile = new UserProfile(this.getApplication());
        _stateGuy = new StateGuy(this);
        if (_stateGuy.getKeyString("LoadFavourite") != "No")
            competition = profile.getFavourateLeague();
        else
            competition = _stateGuy.getKeyString("Last_Selected");

        if(competition == null){
            competition = "Premier League";
        }
        String toUpdate = _stateGuy.getKeyString("Update_When");
        toUpdate = toUpdate !=null && !toUpdate.trim().equals("")?toUpdate:"Always";
        update =  toUpdate.equals("Always");
        HandleSpinner();
        context = this;

        InitializeCompetition();
    }
    private void InitializeCompetition()
    {
        worker = new WorkerAssist();
        UpdateAppState();
        HideAll();
        AppOptions options = profile.getAppOptions();
         /*
        if(options.Logs)
            ShowSavedLog();
        if(options.Scorers)
            ShowSavedScorers();
        if(options.Results)
            ShowSavedResults();
        if(options.Fixtures)
            ShowSavedFixtures();
        if(options.News)
            ShowSavedNews();
          */
        // #26: My Teams mode covers Log/Results/Fixtures/Live/Scorers with one shared fetch
        // instead of each section independently re-aggregating all 13 competitions on its
        // own (running that many full My Teams loads at once was needlessly slow - see
        // InitializeMyTeamsDashboard). News isn't My-Teams-aware yet, so it still goes
        // through the normal single-competition path regardless of the toggle.
        List<String> myTeamsSupported = new UserProfile(this).getSupportedTeams();
        boolean myTeamsDashboard = MY_TEAMS_OPTION.equals(competition) && !myTeamsSupported.isEmpty();

        if(update)
        {
            if (myTeamsDashboard) {
                worker.Logs = options.Logs;
                worker.Results = options.Results;
                worker.Fixtures = options.Fixtures;
                worker.Scorers = options.Scorers;
                InitializeMyTeamsDashboard(myTeamsSupported, options);
            } else {
                if(options.Logs) {
                    worker.Logs = true;
                    InitializeLog();
                }

                if(options.Results) {
                    worker.Results = true;
                    InitializeResults();
                }

                if(options.Fixtures) {
                    worker.Fixtures = true;
                    InitializeFixtures();
                }

                if(options.Scorers) {
                    worker.Scorers = true;
                    InitializeScorers();
                }
            }

            if(options.News) {
                worker.News = true;
                InitializeNews();
            }
        }
        worker.Live = true;
        if (myTeamsDashboard) {
            // Already covered by the one shared fetch above - InitializeMyTeamsDashboard
            // populates Live too, so this just needs to skip the normal-mode fetch below.
        } else {
            InitializeLive();
        }
    }

    private void InitializeMyTeamsDashboard(final List<String> supportedTeams, final AppOptions options) {
        beginLoad();
        final Thread myTeamsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final MyTeamsData.MyTeamsResult result = new MyTeamsData().load(supportedTeams);
                runOnUiThread(new Runnable() {
                    public void run() {
                        endLoad();
                        if (options.Logs) {
                            ShowMyTeamsLogPreview(result.standings);
                            worker.LogsDone = true;
                        }
                        if (options.Results) {
                            ShowResults(result.results, false);
                            worker.ResultsDone = true;
                        }
                        if (options.Fixtures) {
                            ShowFixtures(result.fixtures, false);
                            worker.FixturesDone = true;
                        }
                        if (options.Scorers) {
                            ShowMyTeamsScorersPreview(result.scorers);
                            worker.ScorersDone = true;
                        }
                        ShowLiveDashboard(result.live);
                        worker.LiveDone = true;
                        UpdateAppState();
                    }
                });
            }
        });
        myTeamsThread.start();
    }
    private void UpdateAppState()
    {
        if(worker.Done())
        {
            (findViewById(R.id.online)).setVisibility(View.VISIBLE);
            (findViewById(R.id.offline)).setVisibility(View.GONE);
        }
        else
        {
            (findViewById(R.id.online)).setVisibility(View.GONE);
            (findViewById(R.id.offline)).setVisibility(View.VISIBLE);
        }
    }
    private void HideAll() {
        (findViewById(R.id.logLayout)).setVisibility(View.GONE);
        (findViewById(R.id.spacer_log)).setVisibility(View.GONE);

        (findViewById(R.id.fixtureLayout)).setVisibility(View.GONE);
        (findViewById(R.id.spacer_fixture)).setVisibility(View.GONE);

        (findViewById(R.id.resultLayout)).setVisibility(View.GONE);
        (findViewById(R.id.spacer_result)).setVisibility(View.GONE);

        (findViewById(R.id.liveLayout)).setVisibility(View.GONE);
        (findViewById(R.id.spacer_live)).setVisibility(View.GONE);

        (findViewById(R.id.scorersLayout)).setVisibility(View.GONE);
        (findViewById(R.id.spacer_scorer)).setVisibility(View.GONE);

        (findViewById(R.id.news)).setVisibility(View.GONE);
        (findViewById(R.id.spacer_news)).setVisibility(View.GONE);
    }
    public void Settings(View view)
    {
        settingsChanged = true;
        startActivity( new Intent(this,settings_activity.class));
    }
    boolean settingsChanged = false;

    private void HandleSpinner()
    {
        Spinner spinner = (Spinner) findViewById(R.id.competionSpinner);
        // android:entries + the theme's spinnerItemStyle/spinnerDropDownItemStyle attributes
        // (styles.xml) are supposed to color this, but AppCompat's Spinner doesn't reliably
        // pick them up - the resting text renders in the platform default (dark) color
        // instead of white, unreadable against this screen's dark panel background. Setting
        // an explicit adapter that forces the color on every row sidesteps that entirely.
        // The resting view and the dropdown popup need opposite colors: the resting view sits
        // on this screen's dark panel (needs white), but the popup list itself renders on
        // AppCompat's default light popup background (needs dark, or it's invisible there).
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, competitions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(0xFFFFFFFF);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(0xFF000000);
                return view;
            }
        };
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(getIndex(spinner, competition.toString()));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here

                try {
                    competition = competitions[position];
                    _stateGuy.InitializeKey("Last_Selected", competition.toString());
                    InitializeCompetition();
                    int x =0;
                }
                catch (Exception ex)
                {
                    int x = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }
    private int getIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }
    private void ShowLog(StandingsResponse results, boolean saved)
    {
        if(results != null) {
            //new LeagueSavedState(context).SaveLog(competition,results);
            if(results.standings.size() > 0) {
                ArrayList<TableItem> logItems = new LeagueSavedState(context).getLog(competition);
                logResultsListView = (ListView) findViewById(R.id.logListView);

                // Cup competitions have one table per group - this preview only has room to show
                // one, so label which group it is rather than silently showing an arbitrary one.
                TextView logHeader = (TextView) findViewById(R.id.entry);
                if (results.standings.size() > 1 && results.standings.get(0).group != null) {
                    logHeader.setText("Log (" + results.standings.get(0).group + ")");
                } else {
                    logHeader.setText("Log");
                }

                List<TableItem> filtered = FilterLog(results.standings.get(0).table);
                if (filtered != null) {
                    logListAdapter = new logListAdapter(context, filtered);
                    logResultsListView.setAdapter(logListAdapter);
                    TextView empty = (TextView) findViewById(R.id.comm_empty_log);
                    logResultsListView.setEmptyView(empty);
                    logResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            ///Result live = (Result) resultsListAdapter.getItem(position);
                            Logs(null);
                            //GoToCommentry(live);
                        }
                    });
                    //logResultsListView.setEnabled(false);
                    //if(!saved)
                    //((TextView)findViewById(R.id.title)).setText("Log(online)");
                    if (results.standings.size() > 0) {
                        (findViewById(R.id.logLayout)).setVisibility(View.VISIBLE);
                        (findViewById(R.id.spacer_log)).setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        //else ShowSavedLog();

    }
    private void ShowSavedLog()
    {
        try {

            ArrayList<TableItem> logItems = new LeagueSavedState(context).getLog(competition);
            //if(logItems != null)
                //ShowLog(logItems,true);
            //else utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
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

             //utility.ShowNetworkError((TextView)findViewById(R.id.title), "Log");
            //utility.ShowNetworkError(getFragmentManager());
            return true;
        }
    }
    private void InitializeLog()
    {
        if(ShowError()) return;
        beginLoad();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                FootballData superSport = new FootballData();
                final String current = competition;
                final StandingsResponse results =superSport.GetLog(current);
                runOnUiThread(new Runnable() {
                    public void run() {
                        endLoad();

                        //new LeagueSavedState(context).SaveLog(current, results);
                       // ArrayList<LogItem> log = new LeagueSavedState(context).getLog(current);
                        if(current == competition) {
                            ShowLog(results, false);
                        }
                        worker.LogsDone = true;
                        UpdateAppState();
                    }
                });
            }
        });
        eThread.start();
    }

    private void ShowMyTeamsLogPreview(List<MyTeamsData.TeamStanding> standings) {
        if (standings.isEmpty()) return;

        logResultsListView = (ListView) findViewById(R.id.logListView);
        TextView logHeader = (TextView) findViewById(R.id.entry);
        logHeader.setText("My Teams");

        List<TableItem> items = new ArrayList<>();
        List<String> competitionCodes = new ArrayList<>();
        // Preview only has room for a handful of rows - same cap FilterLog() already uses
        // for the normal single-competition case.
        for (int i = 0; i < standings.size() && i < 4; i++) {
            items.add(standings.get(i).entry);
            competitionCodes.add(standings.get(i).competition.code);
        }

        logListAdapter = new logListAdapter(context, items, competitionCodes);
        logResultsListView.setAdapter(logListAdapter);
        TextView empty = (TextView) findViewById(R.id.comm_empty_log);
        logResultsListView.setEmptyView(empty);
        logResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Logs(null);
            }
        });
        (findViewById(R.id.logLayout)).setVisibility(View.VISIBLE);
        (findViewById(R.id.spacer_log)).setVisibility(View.VISIBLE);
    }
    private void InitializeScorers()
    {
        if(ShowError())
            return;

        beginLoad();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {


                FootballData superSport = new FootballData();
                final String current = competition;
                final ScorerResponse results =superSport.GetScorers(current);
                runOnUiThread(new Runnable() {
                    public void run() {
                        endLoad();
                        if(current == competition)
                            ShowScorers(results.scorers, false);
                        //new LeagueSavedState(context).SaveTopScorers(current,results.scorers);
                        worker.ScorersDone = true;
                        UpdateAppState();
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
            ArrayList<Scorer> scorers = FilterScorers(results);
            if(scorers != null) {
                scorerListAdapter = new scorerListAdapter(context, scorers);
                scorerResultsListView.setAdapter(scorerListAdapter);
                TextView empty = (TextView) findViewById(R.id.comm_empty_scorer);
                scorerResultsListView.setEmptyView(empty);
                scorerResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        ///Result live = (Result) resultsListAdapter.getItem(position);
                        Scorers(null);
                        //GoToCommentry(live);
                    }
                });
                //scorerResultsListView.setEnabled(false);
                //if(!saved)
                //((TextView)findViewById(R.id.title)).setText("Goal Scorers(online)");

                if (results.size() == 0) {
                }// utility.showDialog("There are no goal scorers at this point", false, "No scorers", getSupportFragmentManager());
                else {
                    (findViewById(R.id.scorersLayout)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.spacer_scorer)).setVisibility(View.VISIBLE);
                }
            }
        }
        //else ShowSavedScorers();

    }

    private void ShowMyTeamsScorersPreview(List<MyTeamsData.TeamScorer> teamScorers) {
        if (teamScorers.isEmpty()) return;

        scorerResultsListView = (ListView) findViewById(R.id.scorerListView);
        List<Scorer> scorers = new ArrayList<>();
        List<String> competitionCodes = new ArrayList<>();
        // Preview only has room for a handful of rows - same cap FilterScorers() already
        // uses for the normal single-competition case.
        for (int i = 0; i < teamScorers.size() && i < 3; i++) {
            scorers.add(teamScorers.get(i).scorer);
            competitionCodes.add(teamScorers.get(i).competition.code);
        }

        scorerListAdapter = new scorerListAdapter(context, scorers, competitionCodes);
        scorerResultsListView.setAdapter(scorerListAdapter);
        TextView empty = (TextView) findViewById(R.id.comm_empty_scorer);
        scorerResultsListView.setEmptyView(empty);
        scorerResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Scorers(null);
            }
        });
        (findViewById(R.id.scorersLayout)).setVisibility(View.VISIBLE);
        (findViewById(R.id.spacer_scorer)).setVisibility(View.VISIBLE);
    }

    private ArrayList<Scorer> FilterScorers(List<Scorer> bigList)
    {
        if (bigList == null) return null;
        try {
            return new ArrayList<Scorer>(Arrays.asList(new Scorer[]{bigList.get(0), bigList.get(1), bigList.get(2)}));
        }
        catch(Exception ex)
        {
            return null;
        }
    }
    private List<TableItem> FilterLog(List<TableItem> bigList)
    {
        if (bigList == null) return null;
        try {
        return new ArrayList<TableItem>(Arrays.asList(new TableItem[] {bigList.get(0),bigList.get(1),bigList.get(2),bigList.get(3)}));
        }
        catch(Exception ex)
        {
            return null;
        }
    }
    private List<FootballMatch> FilterFixture(List<FootballMatch> bigList)
    {
        if (bigList == null) return null;
        try {
        // The preview panel only ever shows bigList.get(0), so it has to be the soonest
        // fixture, not whatever happened to come first in fetch order.
        Collections.sort(bigList, Comparator.comparing(FootballMatch::GetDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return new ArrayList<FootballMatch>(Arrays.asList(new FootballMatch[] {bigList.get(0)}));
        }
        catch(Exception ex)
        {
            return null;
        }
    }
    private List<FootballMatch> FilterResult(List<FootballMatch> bigList)
    {
        if (bigList == null) return null;
        try {
        // Same reasoning as FilterFixture - the single previewed result has to be the
        // most recent one, so sort most-recent-first before grabbing get(0).
        Collections.sort(bigList, Comparator.comparing(FootballMatch::GetDate, Comparator.nullsLast(Comparator.<java.time.LocalDateTime>reverseOrder())));
        return new ArrayList<FootballMatch>(Arrays.asList(new FootballMatch[] {bigList.get(0)}));
        }
        catch(Exception ex)
        {
            return null;
        }
    }
    private ArrayList<FootballMatch> FilterLive(List<FootballMatch> bigList)
    {
        if (bigList == null) return null;
        try {
        return new ArrayList<FootballMatch>(Arrays.asList(new FootballMatch[] {bigList.get(0)}));
        }
        catch(Exception ex)
        {
            return null;
        }
    }
    private void InitializeResults()
    {
        if(ShowError()) return;
        beginLoad();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                FootballData superSport = new FootballData();
                final String current = competition;
                final MatchResponse results =superSport.GetResults(current);
                runOnUiThread(new Runnable() {
                    public void run() {
                        endLoad();
                        if(current.equals( competition))
                            ShowResults(results.matches,false);
                        new LeagueSavedState(context).SaveResults(current,results.matches);
                        worker.ResultsDone = true;
                        UpdateAppState();
                    }
                });
            }
        });
        eThread.start();
    }
    private void ShowResults(final List<FootballMatch> results, boolean saved)
    {
        if(results != null) {
            if(results.size() > 0) {
                resultsListView = (ListView) findViewById(R.id.resultsListView);
                List<FootballMatch> currentResults = FilterResult(results);
                if (currentResults != null) {
                    resultsListAdapter = new resultsListAdapter(context, currentResults, "Results");
                    resultsListView.setAdapter(resultsListAdapter);
                    TextView empty = (TextView) findViewById(R.id.comm_empty_results);
                    resultsListView.setEmptyView(empty);
                    //resultsListView.setEnabled(false);
                    //if(!saved)
                    //((TextView)findViewById(R.id.title)).setText("Fixtures(online)");

                    (findViewById(R.id.resultLayout)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.spacer_result)).setVisibility(View.VISIBLE);


                    resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            ///Result live = (Result) resultsListAdapter.getItem(position);
                            Results(null);
                            //GoToCommentry(live);
                        }
                    });
                }
            }

            //if(results.size() == 0)
              //  utility.showDialog("There are no results to show", false, "No results",getSupportFragmentManager());
        }
        //else utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());

    }
    private void ShowSavedResults()
    {
        try {

           // ArrayList<Result> logItems = new LeagueSavedState(context).getResults(competition);
            //if(logItems != null)
              //  ShowResults(logItems,true);
            //else utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
        }
        catch (Exception ex)
        {
            int x = 0;
        }
    }
    private void InitializeFixtures()
    {
        if(ShowError())return;

        beginLoad();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                FootballData superSport = new FootballData();
                final MatchResponse results =superSport.GetFixture(competition);
                runOnUiThread(new Runnable() {
                    public void run() {
                        endLoad();
                        //if(current competition)
                            ShowFixtures(results.matches,false);
                        //new LeagueSavedState(context).SaveFixtures(current,results);
                        worker.FixturesDone = true;
                        UpdateAppState();

                    }
                });
            }
        });
        eThread.start();
    }
    private void ShowFixtures(List<FootballMatch> results, boolean saved)
    {
        if(results != null) {
            if(results.size() > 0) {
                fixtureResultsListView = (ListView) findViewById(R.id.fixtureListView);
                List<FootballMatch> filtered = FilterFixture(results);
                if (filtered != null) {
                    fixtureListAdapter = new fixtureListAdapter(context, filtered, "Fixtures");
                    fixtureResultsListView.setAdapter(fixtureListAdapter);
                    TextView empty = (TextView) findViewById(R.id.comm_empty);
                    fixtureResultsListView.setEmptyView(empty);
                    //fixtureResultsListView.setEnabled(false);
                    fixtureResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            ///Result live = (Result) resultsListAdapter.getItem(position);
                            Fixtures(null);
                            //GoToCommentry(live);
                        }
                    });
                    //if(!saved)
                    //((TextView)findViewById(R.id.title)).setText("Fixtures(online)");

                    if (results.size() == 0) {
                    }//utility.showDialog("There are no fixtures for " + competition.toString(), false, "No fixtures",getSupportFragmentManager());
                    else {
                        (findViewById(R.id.fixtureLayout)).setVisibility(View.VISIBLE);
                        (findViewById(R.id.spacer_fixture)).setVisibility(View.VISIBLE);
                    }
                }
            }
        } //else ShowSavedFixtures();
    }
    private void ShowSavedFixtures()
    {
        try {

            //ArrayList<Fixture> logItems = new LeagueSavedState(context).getFixtures(competition);
            //if(logItems != null)
              //  ShowFixtures(logItems,true);
            //else utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
        }
        catch (Exception ex)
        {
            int x = 0;
        }
    }
    private void InitializeLive()
    {
        if(ShowError())return;

        beginLoad();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                FootballData superSport = new FootballData();
                final MatchResponse results =superSport.GetLive(competition);
                runOnUiThread(new Runnable() {
                    public void run() {
                        endLoad();
                        if (results != null) {
                            ShowLiveDashboard(results.matches);
                            worker.LiveDone = true;
                        }
                    }
                });
            }
        });
        eThread.start();
    }

    private void ShowLiveDashboard(List<FootballMatch> matches) {
        liveReesultsListView = (ListView) findViewById(R.id.liveListView);
        if(matches != null && matches.size() > 0) {
            liveListAdapter = new liveListAdapter(context, FilterLive(matches), "Live");
            liveReesultsListView.setAdapter(liveListAdapter);
            TextView empty = (TextView) findViewById(R.id.comm_empty);
            liveReesultsListView.setEmptyView(empty);

            liveReesultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    FootballMatch live = (FootballMatch) liveListAdapter.getItem(position);
                    Live(null);
                    //GoToCommentry(live);
                }
            });
        }

        if(matches == null || matches.size() == 0) {
            (findViewById(R.id.liveLayout)).setVisibility(View.GONE);
            (findViewById(R.id.spacer_live)).setVisibility(View.GONE);
        } else {
            (findViewById(R.id.liveLayout)).setVisibility(View.VISIBLE);
            (findViewById(R.id.spacer_live)).setVisibility(View.VISIBLE);
        }
    }
    public void Fixtures(View view)
    {
        backUpFav = profile.getFavourateLeague();
        profile.setFavourateLeague(competition.toString());
        startActivity(new Intent(this, fixtures_activity.class));
    }
    public void Logs(View view)
    {

        if(!competition.equals("UEFA Champions League")) {
            backUpFav = profile.getFavourateLeague();
            profile.setFavourateLeague(competition.toString());
            startActivity(new Intent(this, log_activity.class));
        }

    }
    public void Live(View view)
    {
        backUpFav = profile.getFavourateLeague();
        profile.setFavourateLeague(competition.toString());
        startActivity( new Intent(this,live_activity.class));
    }
    public void Scorers(View view)
    {
        backUpFav = profile.getFavourateLeague();
        profile.setFavourateLeague(competition.toString());
        startActivity( new Intent(this,scorer_activity.class));
    }

    public void Results(View view)
    {
        backUpFav = profile.getFavourateLeague();
        profile.setFavourateLeague(competition.toString());
        startActivity( new Intent(this,results_activity.class));
    }
    public void News(View view)
    {
        backUpFav = profile.getFavourateLeague();
        profile.setFavourateLeague(competition.toString());
        startActivity( new Intent(this,news_activity.class));
    }
    @Override
    public void onResume() {
        super.onResume();

        if(backUpFav != null)
        {
            profile.setFavourateLeague(backUpFav.toString());
            backUpFav = null;
        }

        if (settingsChanged)
        {
            settingsChanged = false;
            Start();
        }
        else if (hasResumedOnce)
        {
            // Refetch so a result that finished (or a fixture that went live) while the user
            // was away from this screen actually shows up instead of the stale onCreate() data.
            Start();
        }
        hasResumedOnce = true;
    }
    public
    String backUpFav;
    public void SetImage(String Url)
    {
        loadImageFromURL(Url, (ImageView)findViewById(R.id.newsImage));
    }
    public boolean loadImageFromURL(String fileUrl,
    final ImageView iv){
        // Network I/O must never run on the calling thread if it's the UI thread (ANR risk -
        // this used to block the whole app for the duration of the image download), and the
        // resulting bitmap must never be applied to the view off the UI thread either. Always
        // fetching on a fresh thread and posting the result back to the view covers both
        // callers of this method (one calls it from the UI thread, the other from a background
        // thread) with a single thread-safe implementation.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL myFileUrl = new URL(fileUrl);
                    HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    final InputStream is = conn.getInputStream();
                    final android.graphics.Bitmap bitmap = BitmapFactory.decodeStream(is);
                    iv.post(new Runnable() {
                        @Override
                        public void run() {
                            iv.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return true;
    }
    private void ShowSavedNews()
    {
        try {

            //ArrayList<NewsItem> logItems = new LeagueSavedState(context).getNews(competition);
            //if(logItems != null)
              //  ShowNews(logItems,true);
            //else utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
        }
        catch (Exception ex)
        {
            int x = 0;
        }
    }
    private void ShowNews(ArrayList<NewsItem> results, boolean saved)
    {
        if (results != null && results.size() > 1) {
            //commentaryListView = (ListView) findViewById(R.id.logListView);
            //commentaryListAdapter = new newsListAdapter(context, results);
            //commentaryListView.setAdapter(commentaryListAdapter);
            TextView title = (TextView) findViewById(R.id.newsTitle);
            title.setText(results.get(0).title);
            SetImage(results.get(0).imgSrc);

            (findViewById(R.id.news)).setVisibility(View.VISIBLE);
            (findViewById(R.id.spacer_news)).setVisibility(View.VISIBLE);
            //commentaryListView.setEmptyView(empty);
            //if(!saved)
                //((TextView)findViewById(R.id.title)).setText("Log(online)");

        }
    }
    private void InitializeNews() {

        if(ShowError())
            return;

        beginLoad();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                NewsClient newsClient = new NewsClient();
                final ArrayList<NewsItem> newsItems; ArrayList<NewsItem> holder = null;
                try {
                    holder = newsClient.GetNews(competition);

                }
                catch (Exception ex)
                {
                    // Exception path used to return here with no loading-state cleanup at
                    // all - endLoad() must still run or the shared progress bar would get
                    // stuck on if News is what throws.
                    runOnUiThread(new Runnable() {
                        public void run() {
                            endLoad();
                        }
                    });
                    return;
                }

                newsItems = holder;

                worker.NewsDone = true;
                UpdateAppState();


                if(newsItems != null) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            endLoad();
                            //dialog.hide();
                            ShowNews(newsItems,false);
                            //new LeagueSavedState(context).SaveNews(current,newsItems);
                        }
                    });
                    if(newsItems != null && newsItems.size() > 0)
                        SetImage(newsItems.get(0).imgSrc);
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            endLoad();
                        }
                    });
                }
            }
        });
        eThread.start();
    }
}
