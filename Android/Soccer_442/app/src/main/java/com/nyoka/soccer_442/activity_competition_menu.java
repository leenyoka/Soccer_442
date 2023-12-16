package com.nyoka.soccer_442;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
/**
 * Created by linda.nyoka on 2015-03-01.
 */
public class activity_competition_menu extends AppCompatActivity {
    Context context;
    String competition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.competition_menu);
        getSupportActionBar().hide();
        context = activity_competition_menu.this;
            competition =(String) getIntent().getSerializableExtra("competition");
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
        StartProcess( new Intent(activity_competition_menu.this,results_activity.class));
    }
    public void Fixtures(View view)
    {
        StartProcess( new Intent(activity_competition_menu.this,fixtures_activity.class));
    }
    public void Logs(View view)
    {
        if(competition.equals("UEFA Champions League")) {
            StartProcess( new Intent(activity_competition_menu.this,log_activity.class));
        }
    }
    public void Live(View view)
    {
        StartProcess( new Intent(activity_competition_menu.this,live_activity.class));
    }
    public void Scorers(View view)
    {
        StartProcess( new Intent(activity_competition_menu.this,scorer_activity.class));
    }
    public void News(View view)
    {
        StartProcess( new Intent(activity_competition_menu.this,news_activity.class));
    }
}
