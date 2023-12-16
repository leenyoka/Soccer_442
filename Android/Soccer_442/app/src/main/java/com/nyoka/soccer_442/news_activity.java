package com.nyoka.soccer_442;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by linda.nyoka on 2015-03-26.
 */
public class news_activity extends AppCompatActivity {

    newsListAdapter commentaryListAdapter;
    ListView commentaryListView;
    Context context;
    String competition;
    Utility utility = new Utility();
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        getSupportActionBar().hide();
        context = this;
        SetMatchId();
        ShowSaved();
        Initialize();
        competition = new UserProfile(this).getFavourateLeague();
        ((TextView) findViewById(R.id.competitionName)).setText(competition.toString());
    }
    private void SetMatchId() {
        Intent intent = getIntent();
        competition = (String) intent.getSerializableExtra("competition");

    }
    public boolean ShowError()
    {
        if(utility.Connected(getApplicationContext()))
            return false;

        else
        {
            utility.ShowNetworkError((TextView)findViewById(R.id.title), "News");
            return true;
        }
    }
    private void Initialize() {

        if(ShowError())
            return;


        dialog = new ProgressDialog(news_activity.this);
        dialog.setMessage("Getting news");
        dialog.setIndeterminate(true);
        dialog.show();
        final Thread eThread = new Thread(new Runnable() {
            @Override
            public void run() {

                //SuperSport superSport = new SuperSport();
                final ArrayList<NewsItem> comments; ArrayList<NewsItem> holder = null;
                try {
                    //holder = superSport.GetNews( competition);

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
                            dialog.hide();
                            ShowNews(comments,false);
                            new LeagueSavedState(context).SaveNews(competition,comments);
                        }
                    });
                    if(comments != null && comments.size() > 0) {
                        SetImage(comments.get(0).imgSrc);
                    }
                }
            }
        });
        eThread.start();
    }
    private void ShowNews(ArrayList<NewsItem> results, boolean saved)
    {
        if (results != null && results.size() > 1) {
            commentaryListView = (ListView) findViewById(R.id.logListView);
            commentaryListAdapter = new newsListAdapter(context, results);
            commentaryListView.setAdapter(commentaryListAdapter);
            TextView empty = (TextView) findViewById(R.id.comm_empty);
            commentaryListView.setEmptyView(empty);
            if(!saved) {
                ((TextView)findViewById(R.id.title)).setText("Log(online)");
            }

            commentaryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    NewsItem live = (NewsItem) commentaryListAdapter.getItem(position);

                    GoToCommentry(live);
                }
            });


        }
    }
    private void ShowSaved()
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
    private void GoToCommentry(NewsItem match)
    {
        Intent intent = new Intent(this, activity_news_article.class);
        intent.putExtra("article", match);


        startActivity(intent);
    }
}
