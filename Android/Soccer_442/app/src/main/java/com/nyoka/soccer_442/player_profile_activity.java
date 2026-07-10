package com.nyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

/** Player profile screen (#6), sourced from Wikipedia, opened by tapping a lineup player. */
public class player_profile_activity extends AppCompatActivity {

    Context context;
    String playerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_profile);
        Utility.ApplyEdgeToEdgeInsets(this);
        getSupportActionBar().hide();
        context = this;

        playerName = getIntent().getStringExtra("playerName");
        ((TextView) findViewById(R.id.playerName)).setText(playerName);
        Initialize();
    }

    private void Initialize() {
        Utility.ShowLoading(this);

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                WikipediaClient wikipedia = new WikipediaClient();
                final PlayerProfile profile = wikipedia.getPlayerProfile(playerName);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Utility.HideLoading(player_profile_activity.this);
                        Show(profile);
                    }
                });
            }
        });
        thread.start();
    }

    private void Show(PlayerProfile profile) {
        if (profile == null) {
            findViewById(R.id.comm_empty).setVisibility(View.VISIBLE);
            return;
        }

        if (profile.name != null) ((TextView) findViewById(R.id.playerName)).setText(profile.name.replace('_', ' '));
        if (profile.extract != null) ((TextView) findViewById(R.id.playerExtract)).setText(profile.extract);
        if (profile.photoUrl != null) {
            Glide.with(context).load(profile.photoUrl).into((ImageView) findViewById(R.id.playerPhoto));
        }

        LinearLayout facts = (LinearLayout) findViewById(R.id.factsContainer);
        AddFact(facts, "Position", profile.position);
        AddFact(facts, "Born", profile.birthDate);
        AddFact(facts, "Birthplace", profile.birthPlace);
        AddFact(facts, "Height", profile.height);
        AddFact(facts, "Current Club", profile.currentClub);
        AddFact(facts, "National Team", profile.nationalTeam);
        AddFact(facts, "Caps", profile.caps);
        AddFact(facts, "International Goals", profile.goals);
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
