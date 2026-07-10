package com.nyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

/** Team profile screen (#7), sourced from Wikipedia, opened by tapping a team name. */
public class team_profile_activity extends AppCompatActivity {

    Context context;
    String teamName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_profile);
        Utility.ApplyEdgeToEdgeInsets(this);
        getSupportActionBar().hide();
        context = this;

        teamName = getIntent().getStringExtra("teamName");
        ((TextView) findViewById(R.id.teamName)).setText(teamName);
        Initialize();
    }

    private void Initialize() {
        Utility.ShowLoading(this);

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                WikipediaClient wikipedia = new WikipediaClient();
                final TeamProfile profile = wikipedia.getTeamProfile(teamName);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Utility.HideLoading(team_profile_activity.this);
                        Show(profile);
                    }
                });
            }
        });
        thread.start();
    }

    private void Show(TeamProfile profile) {
        if (profile == null) {
            findViewById(R.id.comm_empty).setVisibility(View.VISIBLE);
            return;
        }

        if (profile.name != null) ((TextView) findViewById(R.id.teamName)).setText(profile.name.replace('_', ' '));
        if (profile.extract != null) ((TextView) findViewById(R.id.teamExtract)).setText(profile.extract);
        if (profile.crestUrl != null) {
            Glide.with(context).load(profile.crestUrl).into((ImageView) findViewById(R.id.teamCrest));
        }

        LinearLayout facts = (LinearLayout) findViewById(R.id.factsContainer);
        AddFact(facts, "Ground", profile.ground);
        AddFact(facts, "Capacity", profile.capacity);
        AddFact(facts, "Manager", profile.manager);
        AddFact(facts, "Founded", profile.founded);
        AddFact(facts, "League", profile.league);
    }

    private void AddFact(LinearLayout container, String label, String value) {
        if (value == null || value.trim().isEmpty()) return;
        TextView row = new TextView(context);
        row.setText(label + ": " + value);
        row.setTextColor(Color.parseColor("#FBF5EF"));
        row.setTextSize(14);
        row.setPadding(0, 4, 0, 4);
        container.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
}
