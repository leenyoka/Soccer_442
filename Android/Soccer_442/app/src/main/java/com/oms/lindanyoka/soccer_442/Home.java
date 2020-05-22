package com.oms.lindanyoka.soccer_442;

import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class Home extends ActionBarActivity implements ListView.OnItemClickListener {
    private String[] mNavigationDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private void setUpMenu()
    {
        mNavigationDrawerItemTitles= getResources().getStringArray(R.array.navigation_drawer_items_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        ObjectDrawerItem[] drawerItem = new ObjectDrawerItem[3];

        drawerItem[0] = new ObjectDrawerItem(R.drawable.absa, "Absa");
        drawerItem[1] = new ObjectDrawerItem(R.drawable.epl, "EPL");
        drawerItem[2] = new ObjectDrawerItem(R.drawable.laliga, "La Liga");


        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.listview_item_row, drawerItem);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(this);
    }
    public void MenuIconClick(View view)
    {
        mDrawerLayout.openDrawer(mDrawerList);
    }
    @Override
    public void onBackPressed()
    {
       // if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
       // {
       //     mDrawerLayout.closeDrawer(mDrawerList);
       // }else
         super.onBackPressed();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }
    private void selectItem(int position) {

        mDrawerLayout.closeDrawer(mDrawerList);
        //Fragment fragment = null;

        switch (position) {
            case 0:
                //fragment = new CreateFragment();
                break;
            case 1:
                //fragment = new ReadFragment();
                break;
            case 2:
                //fragment = new HelpFragment();
                break;

            default:
                break;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);
        getSupportActionBar().hide();
        //setUpMenu();
        //final Thread eThread = new Thread(new Runnable() {
            //@Override
           // public void run() {
                //new SuperSport().GetStats("10028055");
                //new SuperSport().GetLineUp("10026506");
              //ArrayList<NewsItem> answer =   new SuperSport().GetNews(Competition.BPL);
              //String article = new SuperSport().GetNewsArticle(answer.get(0).link);
              //int x = 0;
                //GetNews

            //}
    //});
    //eThread.start();

          //UserProfile profile = new UserProfile(this.getApplication());

            //profile.setTeamName("Arsenal");
        //int x = 0;
        GoToSummary(null);
        //Intent intent = new Intent(this,log_activity.class);
        //intent.putExtra("competition", Competition.BPL);
        //startActivity(intent);

        //Intent intent = new Intent(this, scorer_activity.class);
        //intent.putExtra("competition", Competition.LaLiga);
        //startActivity(intent);


        //Intent intent = new Intent(this, live_activity.class);
        //intent.putExtra("competition", Competition.BPL);
        //startActivity(intent);


        //live commentry

        //main page

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void GoToMenu(Competition competition)
    {
        //if(utility.Connected(getApplicationContext())) {
            Intent intent = new Intent(this, activity_competition_menu.class);
            intent.putExtra("competition", competition);
            startActivity(intent);
        //}
        //else
        //{
        //    utility.ShowNetworkError(getSupportFragmentManager());
        //}
    }
    public void GoToSummary(Competition competition)
    {
        //if(utility.Connected(getApplicationContext())) {
        Intent intent = new Intent(this, summary_activity.class);
        //intent.putExtra("competition", competition);
        startActivity(intent);
        //}
        //else
        //{
        //    utility.ShowNetworkError(getSupportFragmentManager());
        //}
    }
    Utility utility = new Utility();
    public void BPL(View view)
    {
        GoToMenu( Competition.BPL);
    }
    public void LaLiga(View view)
    {
        GoToMenu( Competition.LaLiga);
    }
    public void Bundesliga(View view)
    {
        GoToMenu( Competition.Bundesliga);
    }
    public void SerieA(View view)
    {
        GoToMenu( Competition.SerieA);
    }
    public void Ucl(View view)
    {
        GoToMenu( Competition.UEFA);
    }
    public void Absa(View view)
    {
        GoToMenu( Competition.Absa);
    }
    public void League1(View view)
    {
        GoToMenu( Competition.League1);
    }
    public void Fa(View view)
    {
        GoToMenu( Competition.FA);
    }
}
