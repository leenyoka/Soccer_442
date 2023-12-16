package com.nyoka.soccer_442;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;

/**
 * Created by linda.nyoka on 2015-05-13.
 */
public class settings_activity  extends Activity {
    UserProfile profile;
    private StateGuy _stateGuy;

    private static final String[] competitions = {
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
    private WorkerAssist _worker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Start();
    }
    private void Start()
    {
        profile = new UserProfile(this.getApplication());
        _stateGuy = new StateGuy(this);
        HandleFavourite();
        HandleInitialLoad();
        HandleUpdate();
        InitializeOptions();
        SetCheckListeners();
    }
    public void InitializeOptions()
    {
        AppOptions options = profile.getAppOptions();

        if(options.Logs)
            ((CheckBox)findViewById(R.id.cmbLogs)).setChecked(true);
        if(options.Fixtures)
            ((CheckBox)findViewById(R.id.cmbFixtures)).setChecked(true);
        if(options.Results)
            ((CheckBox)findViewById(R.id.cmbResults)).setChecked(true);
        if(options.Live)
            ((CheckBox)findViewById(R.id.cmbLive)).setChecked(true);
        if(options.Scorers)
            ((CheckBox)findViewById(R.id.cmbScorers)).setChecked(true);
        if(options.News)
            ((CheckBox)findViewById(R.id.cmbNews)).setChecked(true);
    }
    public void SetCheckListeners()
    {
        CheckBox[] boxes = new CheckBox[]
                {(CheckBox)(findViewById(R.id.cmbLogs)),
                (CheckBox)(findViewById(R.id.cmbResults)),
                (CheckBox)(findViewById(R.id.cmbFixtures)),
                (CheckBox)(findViewById(R.id.cmbLive)),
                (CheckBox)(findViewById(R.id.cmbScorers)),
                (CheckBox)(findViewById(R.id.cmbNews))};

        for(CheckBox box:boxes)
            box.setOnClickListener(getListener());
    }
    private View.OnClickListener getListener()
    {
        View.OnClickListener lister = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                    AppOptions options = profile.getAppOptions();
                if (((CheckBox)v).isChecked()) {
                    //System.out.println("Checked");
                    switch (String.valueOf(v.getTag()))
                    {
                        case "0": options.Logs = true;break;
                        case "1": options.Results =true;break;
                        case "2": options.Fixtures = true;break;
                        case "3": options.Live = true; break;
                        case "4": options.Scorers =true;break;
                        case "5": options.News = true;break;
                    }
                    profile.setAppOptions(options);
                } else {
                    switch (String.valueOf(v.getTag()))
                    {
                        case "0": options.Logs = false;break;
                        case "1": options.Results =false;break;
                        case "2": options.Fixtures = false;break;
                        case "3": options.Live = false; break;
                        case "4": options.Scorers =false;break;
                        case "5": options.News = false;break;
                    }
                    profile.setAppOptions(options);
                }
            }
        };
        return lister;
    }

    private void HandleFavourite()
    {
        Spinner spinner = (Spinner) findViewById(R.id.spinnerFavourite);

        spinner.setSelection(getIndex(spinner, profile.getFavourateLeague()));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here

                try {
                    String competition = competitions[position];
                    profile.setFavourateLeague(competition.toString());
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
    private void HandleInitialLoad()
    {
        Spinner spinner = (Spinner) findViewById(R.id.spinnerInitialLoad);
        String key = _stateGuy.getKeyString("LoadFavourite");

        spinner.setSelection(getIndex(spinner, key != null && key.trim() !=""?key:"Yes"));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here

                try {
                    _stateGuy.InitializeKey("LoadFavourite", position == 0? "Yes":"No");
                    //Competition competition = Competition.values()[position];
                    //profile.setFavourateLeague(competition.toString());
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
    private void HandleUpdate()
    {
        Spinner spinner = (Spinner) findViewById(R.id.spinnerUpdate);
        String key = _stateGuy.getKeyString("Update_When");

        spinner.setSelection(getIndex(spinner, key != null && key.trim() != ""?key:"Always"));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here

                try {
                    Update update = Update.values()[position];
                    _stateGuy.InitializeKey("Update_When", update.toString());
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
}
