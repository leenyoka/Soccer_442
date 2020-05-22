package com.oms.lindanyoka.soccer_442;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by linda.nyoka on 2015-04-15.
 */
public class LeagueSavedState {
    private StateGuy _stateGuy;

    public LeagueSavedState(Context context) {
        _stateGuy = new StateGuy(context);
    }

    public void SaveLog(Competition competition, ArrayList<LogItem> log) {
        if (log != null && log.size() > 0) {
            ArrayList<LogItem> logItems = getLog(competition);

            _stateGuy.InitializeKey(competition + "_team_count", log.size());

            for (LogItem item : log) {
                item.Movement = getMovement(item,logItems);
                _stateGuy.InitializeKey(competition + "_log_" + String.valueOf(item.Position), item.Compress());
            }
        }
    }
    public int getMovement(LogItem item, ArrayList<LogItem> oldLog)
    {
        if(oldLog == null) return 0;

        for(LogItem current :oldLog)
        {
            if(current.TeamName.equals(item.TeamName))
            {
                if (item.Position < current.Position)
                    return 1;
                if(item.Position > current.Position)
                    return -1;
                return 0;
            }
        }
        return 0;
    }
    public ArrayList<LogItem> getLog(Competition competition)
    {
        try {
            ArrayList<LogItem> log = new ArrayList<>();
            int number = _stateGuy.getKeyInt(competition + "_team_count");

            if (number == 0) return null;

            for (int i = 1; i <= number; i++) {
                String key = competition + "_log_" + i;
                log.add(new LogItem(_stateGuy.getKeyString(key)));
            }
            return log;
        }
        catch (Exception ex)
        {
            return  null;
        }
    }
    public void SaveTopScorers(Competition competition, ArrayList<TopGoalScorer> scorers) {
        if (scorers != null && scorers.size() > 0) {


            _stateGuy.InitializeKey(competition + "_scorers_count", scorers.size());
            int count = 1;
            for (TopGoalScorer item : scorers) {
                _stateGuy.InitializeKey(competition + "_scorers_" + String.valueOf(count), item.Compress());
                count++;
            }
        }
    }
    public ArrayList<TopGoalScorer> getScorers(Competition competition)
    {
        try {
            ArrayList<TopGoalScorer> log = new ArrayList<>();
            int number = _stateGuy.getKeyInt(competition + "_scorers_count");

            if (number == 0) return null;

            for (int i = 1; i <= number; i++) {
                String key = competition + "_scorers_" + i;
                log.add(new TopGoalScorer(_stateGuy.getKeyString(key)));
            }
            return log;
        }
        catch (Exception ex)
        {
            return  null;
        }
    }



    public void SaveFixtures(Competition competition, ArrayList<Fixture> fixtures) {
        if (fixtures != null && fixtures.size() > 0) {
           RemoveSaveFixtures(competition);

            _stateGuy.InitializeKey(competition + "_fixture_count", fixtures.size());
            int count = 1;
            for (Fixture item : fixtures) {
                _stateGuy.InitializeKey(competition + "_fixtures_" + String.valueOf(count), item.Compress());
                count++;
            }
        }
    }
    public ArrayList<Fixture> getFixtures(Competition competition)
    {
        try {
            ArrayList<Fixture> fixtures = new ArrayList<>();
            int number = _stateGuy.getKeyInt(competition + "_fixture_count");

            if (number == 0) return null;

            for (int i = 1; i <= number; i++) {
                String key = competition + "_fixtures_" + i;
                fixtures.add(new Fixture(_stateGuy.getKeyString(key)));
            }
            return fixtures;
        }
        catch (Exception ex)
        {
            return  null;
        }
    }
    private boolean RemoveSaveFixtures(Competition competition)
    {
        try {

            int number = _stateGuy.getKeyInt(competition + "_fixture_count");

            for (int i = 1; i <= number; i++) {
                String key = competition + "_fixtures_" + i;
                _stateGuy.Remove(key);
            }
            _stateGuy.InitializeKey(competition + "_fixture_count",0);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public void SaveResults(Competition competition, ArrayList<Result> fixtures) {
        if (fixtures != null && fixtures.size() > 0) {
            RemoveSaveResults(competition);

            _stateGuy.InitializeKey(competition + "_result_count", fixtures.size());
            int count = 1;
            for (Result item : fixtures) {
                _stateGuy.InitializeKey(competition + "_results_" + String.valueOf(count), item.Compress());
                count++;
            }
        }
    }
    public ArrayList<Result> getResults(Competition competition)
    {
        try {
            ArrayList<Result> fixtures = new ArrayList<>();
            int number = _stateGuy.getKeyInt(competition + "_result_count");

            if (number == 0) return null;

            for (int i = 1; i <= number; i++) {
                String key = competition + "_results_" + i;
                fixtures.add(new Result(_stateGuy.getKeyString(key)));
            }
            return fixtures;
        }
        catch (Exception ex)
        {
            return  null;
        }
    }
    private boolean RemoveSaveResults(Competition competition)
    {
        try {

            int number = _stateGuy.getKeyInt(competition + "_result_count");

            for (int i = 1; i <= number; i++) {
                String key = competition + "_results_" + i;
                _stateGuy.Remove(key);
            }
            _stateGuy.InitializeKey(competition + "_result_count",0);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public void SaveNews(Competition competition, ArrayList<NewsItem> fixtures) {
        if (fixtures != null && fixtures.size() > 0) {
            RemoveSaveNews(competition);

            _stateGuy.InitializeKey(competition + "_news_count", fixtures.size());
            int count = 1;
            for (NewsItem item : fixtures) {
                _stateGuy.InitializeKey(competition + "_news_" + String.valueOf(count), item.Compress());
                count++;
            }
        }
    }
    public ArrayList<NewsItem> getNews(Competition competition)
    {
        try {
            ArrayList<NewsItem> fixtures = new ArrayList<>();
            int number = _stateGuy.getKeyInt(competition + "_news_count");

            if (number == 0) return null;

            for (int i = 1; i <= number; i++) {
                String key = competition + "_news_" + i;
                fixtures.add(new NewsItem(_stateGuy.getKeyString(key)));
            }
            return fixtures;
        }
        catch (Exception ex)
        {
            return  null;
        }
    }
    private boolean RemoveSaveNews(Competition competition)
    {
        try {

            int number = _stateGuy.getKeyInt(competition + "_news_count");

            for (int i = 1; i <= number; i++) {
                String key = competition + "_news_" + i;
                _stateGuy.Remove(key);
            }
            _stateGuy.InitializeKey(competition + "_news_count",0);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }
}
