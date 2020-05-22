package com.oms.lindanyoka.soccer_442;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by linda.nyoka on 2015-03-01.
 */
public class activity_competition_menu extends ActionBarActivity {
    Context context;
    Competition competition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.competition_menu);
        getSupportActionBar().hide();
        context = this;
            competition = (Competition) getIntent().getSerializableExtra("competition");
            //((TextView) findViewById(R.id.competitionName)).setText(competition.toString());
    }
    public void StartProcess(Intent intent)
    {
        //if(utility.Connected(getApplicationContext())) {
            intent.putExtra("competition", competition);
            startActivity(intent);
        // }
        //else
        //{
        //    utility.ShowNetworkError(getSupportFragmentManager());
        //}
    }
    Utility utility = new Utility();
    public void Results(View view)
    {
        StartProcess( new Intent(this,results_activity.class));
    }
    public void Fixtures(View view)
    {
        StartProcess( new Intent(this,fixtures_activity.class));
    }
    public void Logs(View view)
    {
        if(competition != Competition.UEFA &&
                competition != Competition.FA)
        StartProcess( new Intent(this,log_activity.class));
    }
    public void Live(View view)
    {
        StartProcess( new Intent(this,live_activity.class));
    }
    public void Scorers(View view)
    {
        StartProcess( new Intent(this,scorer_activity.class));
    }
    public void News(View view)
    {
        StartProcess( new Intent(this,news_activity.class));
    }
}
