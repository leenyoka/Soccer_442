package com.oms.lindanyoka.soccer_442;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

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
    Competition competition;
    UserProfile profile;
    private StateGuy _stateGuy;
    boolean update;
    private WorkerAssist worker;

    //ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiity_summary);
        //getSupportActionBar().hide();
        Start();
    }
    private void Start()
    {
        profile = new UserProfile(this.getApplication());
        _stateGuy = new StateGuy(this);
        if (_stateGuy.getKeyString("LoadFavourite") != "No")
            competition = Competition.valueOf(profile.getFavourateLeague());
        else
            competition = Competition.valueOf(_stateGuy.getKeyString("Last_Selected"));
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
        if(update)
        {
            if(options.Logs) {
                worker.Logs = true;
                InitializeLog();
            }
            if(options.Scorers) {
                worker.Scorers = true;
                InitializeScorers();
            }
            if(options.Results) {
                worker.Results = true;
                InitializeResults();
            }
            if(options.Fixtures) {
                worker.Fixtures = true;
                InitializeFixtures();
            }
            if(options.News) {
                worker.News = true;
                InitializeNews();
            }
        }
        worker.Live = true;
        InitializeLive();
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
        spinner.setSelection(getIndex(spinner, competition.toString()));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here

                try {
                    competition = Competition.values()[position];
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
    private void ShowLog(ArrayList<LogItem> results, boolean saved)
    {
        if(results != null) {
            //new LeagueSavedState(context).SaveLog(competition,results);
            if(results.size() > 0) {
                ArrayList<LogItem> logItems = new LeagueSavedState(context).getLog(competition);
                logResultsListView = (ListView) findViewById(R.id.logListView);
                ArrayList<LogItem> filtered = FilterLog(logItems);
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
                    if (results.size() > 0) {
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
        //dialog = new ProgressDialog(summary_activity.this);
        //dialog.setMessage("Getting log standings");
        //dialog.setIndeterminate(true);
        //dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                SuperSport superSport = new SuperSport();
                final Competition current = competition;
                final ArrayList<LogItem> results =superSport.GetLog(current);
                runOnUiThread(new Runnable() {
                    public void run() {
                        //dialog.hide();

                        //new LeagueSavedState(context).SaveLog(current, results);
                       // ArrayList<LogItem> log = new LeagueSavedState(context).getLog(current);
                        if(current == competition)
                        ShowLog(results, false);
                        worker.LogsDone = true;
                        UpdateAppState();
                    }
                });
            }
        });
        eThread.start();
    }
    private void InitializeScorers()
    {
        if(ShowError())
            return;

        //dialog = new ProgressDialog(summary_activity.this);
        //dialog.setMessage("Getting  top scorers ");
        //dialog.setIndeterminate(true);
        //dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {


                SuperSport superSport = new SuperSport();
                final Competition current = competition;
                final ArrayList<TopGoalScorer> results =superSport.GetScorers(current);
                runOnUiThread(new Runnable() {
                    public void run() {
                        //dialog.hide();
                        if(current == competition)
                            ShowScorers(results, false);
                        new LeagueSavedState(context).SaveTopScorers(current,results);
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

            ArrayList<TopGoalScorer> logItems = new LeagueSavedState(context).getScorers(competition);
            if(logItems != null)
                ShowScorers(logItems,true);
            //else utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
        }
        catch (Exception ex)
        {
            int x = 0;
        }
    }
    private void ShowScorers(ArrayList<TopGoalScorer> results, boolean saved)
    {
        if(results != null) {
            scorerResultsListView = (ListView) findViewById(R.id.scorerListView);
            ArrayList<TopGoalScorer> scorers = FilterScorers(results);
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
    private ArrayList<TopGoalScorer> FilterScorers(ArrayList<TopGoalScorer> bigList)
    {
        if (bigList == null) return null;
        try {
            return new ArrayList<TopGoalScorer>(Arrays.asList(new TopGoalScorer[]{bigList.get(0), bigList.get(1), bigList.get(2)}));
        }
        catch(Exception ex)
        {
            return null;
        }
    }
    private ArrayList<LogItem> FilterLog(ArrayList<LogItem> bigList)
    {
        if (bigList == null) return null;
        try {
        return new ArrayList<LogItem>(Arrays.asList(new LogItem[] {bigList.get(0),bigList.get(1),bigList.get(2),bigList.get(3)}));
        }
        catch(Exception ex)
        {
            return null;
        }
    }
    private ArrayList<Fixture> FilterFixture(ArrayList<Fixture> bigList)
    {
        if (bigList == null) return null;
        try {
        return new ArrayList<Fixture>(Arrays.asList(new Fixture[] {bigList.get(0)}));
        }
        catch(Exception ex)
        {
            return null;
        }
    }
    private ArrayList<Result> FilterResult(ArrayList<Result> bigList)
    {
        if (bigList == null) return null;
        try {
        return new ArrayList<Result>(Arrays.asList(new Result[] {bigList.get(0)}));
        }
        catch(Exception ex)
        {
            return null;
        }
    }
    private ArrayList<Live> FilterLive(ArrayList<Live> bigList)
    {
        if (bigList == null) return null;
        try {
        return new ArrayList<Live>(Arrays.asList(new Live[] {bigList.get(0)}));
        }
        catch(Exception ex)
        {
            return null;
        }
    }
    private void InitializeResults()
    {
        if(ShowError()) return;
        //dialog = new ProgressDialog(results_activity.this);
        //dialog.setMessage("Getting " + competition.toString() + " results");
        //dialog.setIndeterminate(true);
        //dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                SuperSport superSport = new SuperSport();
                final Competition current = competition;
                final ArrayList<Result> results =superSport.GetResults(current);
                runOnUiThread(new Runnable() {
                    public void run() {
                        //dialog.hide();
                        if(current == competition)
                            ShowResults(results,false);
                        new LeagueSavedState(context).SaveResults(current,results);
                        worker.ResultsDone = true;
                        UpdateAppState();
                    }
                });
            }
        });
        eThread.start();
    }
    private void ShowResults(final ArrayList<Result> results, boolean saved)
    {
        if(results != null) {
            if(results.size() > 0) {
                resultsListView = (ListView) findViewById(R.id.resultsListView);
                ArrayList<Result> currentResults = FilterResult(results);
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

            ArrayList<Result> logItems = new LeagueSavedState(context).getResults(competition);
            if(logItems != null)
                ShowResults(logItems,true);
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

        //dialog = new ProgressDialog(fixtures_activity.this);
        //dialog.setMessage("Getting " + competition.toString() + " fixtures");
        //dialog.setIndeterminate(true);
        //dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                SuperSport superSport = new SuperSport();
                final Competition current = competition;
                final ArrayList<Fixture> results =superSport.GetFixture(current);
                runOnUiThread(new Runnable() {
                    public void run() {
                        //dialog.hide();
                        if(current == competition)
                            ShowFixtures(results,false);
                        new LeagueSavedState(context).SaveFixtures(current,results);
                        worker.FixturesDone = true;
                        UpdateAppState();

                    }
                });
            }
        });
        eThread.start();
    }
    private void ShowFixtures(ArrayList<Fixture> results, boolean saved)
    {
        if(results != null) {
            if(results.size() > 0) {
                fixtureResultsListView = (ListView) findViewById(R.id.fixtureListView);
                ArrayList<Fixture> filtered = FilterFixture(results);
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
    private void InitializeLive()
    {
        if(ShowError())return;
        //dialog = new ProgressDialog(live_activity.this);
        //dialog.setMessage("Getting " + competition.toString() + " live games");
        //dialog.setIndeterminate(true);
        //dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                SuperSport superSport = new SuperSport();
                final Competition current = competition;
                final ArrayList<Live> results =superSport.GetLive(current);
                runOnUiThread(new Runnable() {
                    public void run() {
                        //dialog.hide();

                        if(results != null) {
                            liveReesultsListView = (ListView) findViewById(R.id.liveListView);
                            if(results.size() > 0) {
                                liveListAdapter = new liveListAdapter(context, FilterLive(results), "Live");
                                liveReesultsListView.setAdapter(liveListAdapter);
                                TextView empty = (TextView) findViewById(R.id.comm_empty);
                                liveReesultsListView.setEmptyView(empty);

                                liveReesultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                        //Live live = (Live) liveListAdapter.getItem(position);
                                        Live(null);
                                        //GoToCommentry(live);
                                    }
                                });
                            }

                            worker.LiveDone = true;

                            if(results.size() == 0) {
                                (findViewById(R.id.liveLayout)).setVisibility(View.GONE);
                                (findViewById(R.id.spacer_live)).setVisibility(View.GONE);
                            }
                            else if(current == competition)
                            {
                                (findViewById(R.id.liveLayout)).setVisibility(View.VISIBLE);
                                (findViewById(R.id.spacer_live)).setVisibility(View.VISIBLE);
                            }
                            //    utility.showDialog("There are no live games for " + competition.toString(), false, "No live games",getSupportFragmentManager());
                        }
                        {}//else utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());

                    }
                });
            }
        });
        eThread.start();
    }
    public void Fixtures(View view)
    {
        backUpFav = Competition.valueOf(profile.getFavourateLeague());
        profile.setFavourateLeague(competition.toString());
        startActivity(new Intent(this, fixtures_activity.class));
    }
    public void Logs(View view)
    {
        if(competition != Competition.UEFA &&
                competition != Competition.FA) {
            backUpFav = Competition.valueOf(profile.getFavourateLeague());
            profile.setFavourateLeague(competition.toString());
            startActivity(new Intent(this, log_activity.class));
        }
    }
    public void Live(View view)
    {
        backUpFav = Competition.valueOf(profile.getFavourateLeague());
        profile.setFavourateLeague(competition.toString());
        startActivity( new Intent(this,live_activity.class));
    }
    public void Scorers(View view)
    {
        backUpFav = Competition.valueOf(profile.getFavourateLeague());
        profile.setFavourateLeague(competition.toString());
        startActivity( new Intent(this,scorer_activity.class));
    }
    public void News(View view)
    {
        backUpFav = Competition.valueOf(profile.getFavourateLeague());
        profile.setFavourateLeague(competition.toString());
        startActivity( new Intent(this,news_activity.class));
    }
    public void Results(View view)
    {
        backUpFav = Competition.valueOf(profile.getFavourateLeague());
        profile.setFavourateLeague(competition.toString());
        startActivity( new Intent(this,results_activity.class));
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
    }
    public
    Competition backUpFav;
    public void SetImage(String Url)
    {
        loadImageFromURL(Url, (ImageView)findViewById(R.id.newsImage));
    }
    public boolean loadImageFromURL(String fileUrl,
    final ImageView iv){
        try {

            URL myFileUrl = new URL (fileUrl);
            HttpURLConnection conn =
                    (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();

            final InputStream is = conn.getInputStream();
            //runOnUiThread(new Runnable() {
            //  public void run() {
            iv.setImageBitmap(BitmapFactory.decodeStream(is));
            //  }});

            return true;

        }catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    private void ShowSavedNews()
    {
        try {

            ArrayList<NewsItem> logItems = new LeagueSavedState(context).getNews(competition);
            if(logItems != null)
                ShowNews(logItems,true);
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


        //dialog = new ProgressDialog(news_activity.this);
        //dialog.setMessage("Getting news for " + competition);
        //dialog.setIndeterminate(true);
        //dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                SuperSport superSport = new SuperSport();
                final Competition current = competition;
                final ArrayList<NewsItem> newsItems; ArrayList<NewsItem> holder = null;
                try {
                    holder = superSport.GetNews(current);

                }
                catch (Exception ex)
                {

                    //utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
                    return;
                }

                newsItems = holder;

                worker.NewsDone = true;
                UpdateAppState();


                if(newsItems != null) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            //dialog.hide();
                            ShowNews(newsItems,false);
                            new LeagueSavedState(context).SaveNews(current,newsItems);
                        }
                    });
                    if(newsItems != null && newsItems.size() > 0)
                        SetImage(newsItems.get(0).imgSrc);
                }
            }
        });
        eThread.start();
    }
}
