package com.oms.lindanyoka.soccer_442;

/**
 * Created by linda.nyoka on 2015-05-15.
 */
public class AppOptions {
    public boolean News;
    public boolean Logs;
    public boolean Results;
    public boolean Fixtures;
    public boolean Live;
    public boolean Scorers;


    public AppOptions(String value) {

        if(value != null && value.trim().length() > 0) {
            String[] values = value.split("_");

            News = values[0].toLowerCase().equals("true");
            Logs = values[1].toLowerCase().equals("true");
            Results = values[2].toLowerCase().equals("true");
            Fixtures = values[3].toLowerCase().equals("true");
            Live = values[4].toLowerCase().equals("true");
            Scorers = values[5].toLowerCase().equals("true");
        }
        else
        {
            Logs = true;
            Results = true;
            Fixtures = true;
            Live = true;
            Scorers = true;
            News = false;
        }
    }

    public String Compress() {
        String value = String.valueOf(News);
        value += "_" + String.valueOf(Logs);
        value += "_" + String.valueOf(Results);
        value += "_" + String.valueOf(Fixtures);
        value += "_" + String.valueOf(Live);
        value += "_" + String.valueOf(Scorers);
        return value;
    }
}
