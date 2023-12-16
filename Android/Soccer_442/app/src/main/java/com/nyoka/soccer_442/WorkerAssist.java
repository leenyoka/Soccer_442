package com.nyoka.soccer_442;

/**
 * Created by linda.nyoka on 2015-05-16.
 */
public class WorkerAssist {
    public boolean News;
    public boolean Logs;
    public boolean Results;
    public boolean Fixtures;
    public boolean Live;
    public boolean Scorers;

    public boolean NewsDone;
    public boolean LogsDone;
    public boolean ResultsDone;
    public boolean FixturesDone;
    public boolean LiveDone;
    public boolean ScorersDone;

    public boolean Done() {

        if (    (!News && !NewsDone) &&
                (!Logs && !LogsDone) &&
                (!Results && !ResultsDone) &&
                (!Fixtures && !FixturesDone) &&
                (!Live && !LiveDone) &&
                (!Scorers && !ScorersDone))
                    return false;


        if (    (!News || NewsDone) &&
                (!Logs || LogsDone) &&
                (!Results || ResultsDone) &&
                (!Fixtures || FixturesDone) &&
                (!Live || LiveDone) &&
                (!Scorers || ScorersDone))
            return true;
        return false;
    }
}
