package com.nyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.nyoka.soccer_442.football_data.FootballData;

/**
 * Shows head-to-head history and each team's recent form for a fixture - #13.
 * Reachable by tapping an upcoming fixture row.
 */
public class head_to_head_activity extends AppCompatActivity {

    Context context;
    Utility utility = new Utility();
    String competition;
    String matchId;
    String homeTeamName;
    String awayTeamName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_head_to_head);
        Utility.ApplyEdgeToEdgeInsets(this);
        getSupportActionBar().hide();
        context = this;

        Intent intent = getIntent();
        competition = intent.getStringExtra("competition");
        matchId = intent.getStringExtra("matchId");
        homeTeamName = intent.getStringExtra("homeTeamName");
        awayTeamName = intent.getStringExtra("awayTeamName");

        TextView homeLink = (TextView) findViewById(R.id.homeTeamNameLink);
        homeLink.setText(homeTeamName);
        homeLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent teamIntent = new Intent(context, team_profile_activity.class);
                teamIntent.putExtra("teamName", homeTeamName);
                startActivity(teamIntent);
            }
        });

        TextView awayLink = (TextView) findViewById(R.id.awayTeamNameLink);
        awayLink.setText(awayTeamName);
        awayLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent teamIntent = new Intent(context, team_profile_activity.class);
                teamIntent.putExtra("teamName", awayTeamName);
                startActivity(teamIntent);
            }
        });

        Initialize();
    }

    private void Initialize() {
        Utility.ShowLoading(this);

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FootballData footballData = new FootballData();
                final HeadToHeadSummary summary = footballData.GetHeadToHeadAndForm(competition, matchId, homeTeamName, awayTeamName);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Utility.HideLoading(head_to_head_activity.this);
                        Show(summary);
                    }
                });
            }
        });
        thread.start();
    }

    private void Show(HeadToHeadSummary summary) {
        if (HeadToHeadRenderer.isEmpty(summary)) {
            findViewById(R.id.comm_empty).setVisibility(View.VISIBLE);
            return;
        }
        LinearLayout container = (LinearLayout) findViewById(R.id.contentContainer);
        HeadToHeadRenderer.render(context, container, summary, homeTeamName, awayTeamName);
    }
}
