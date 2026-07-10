package com.nyoka.soccer_442;

import android.app.Application;
import android.content.Context;

/**
 * FootballData/NewsClient/WikipediaClient are instantiated with `new` all over the app (no
 * constructor Context param anywhere) but now need a Context to reach the Room database - this
 * gives them one without changing any of those existing call sites.
 */
public class Soccer442Application extends Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return appContext;
    }
}
