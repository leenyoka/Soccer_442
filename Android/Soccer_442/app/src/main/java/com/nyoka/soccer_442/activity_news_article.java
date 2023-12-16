package com.nyoka.soccer_442;

import android.app.ProgressDialog;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

/**
 * Created by linda.nyoka on 2015-03-27.
 */
public class activity_news_article  extends AppCompatActivity {
     NewsItem article;
    Utility utility = new Utility();
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_article);
        getSupportActionBar().hide();

        article = (NewsItem) getIntent().getSerializableExtra("article");
        ((TextView) findViewById(R.id.competitionName)).setText(article.title.toString());
        Initialize();
    }
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
    private void Initialize() {

        if(ShowError())
            return;


        dialog = new ProgressDialog(activity_news_article.this);
        dialog.setMessage("Please wait..." );
        dialog.setIndeterminate(true);
        dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                //SuperSport superSport = new SuperSport();
                final String comments; String holder = null;
                try {
                    //holder = superSport.GetNewsArticle(article.link);

                }
                catch (Exception ex)
                {

                    utility.showDialog("Error retrieving data. Please try again later", false, "Error", getSupportFragmentManager());
                    return;
                }

                comments = holder;


                if(comments != null) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (comments != null) {
                                ((TextView)findViewById(R.id.liveListView)).setText(comments);


                            }

                            dialog.hide();

                        }
                    });

                }
            }
        });
        eThread.start();
    }
}
