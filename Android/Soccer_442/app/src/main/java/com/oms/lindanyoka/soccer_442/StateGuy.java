package com.oms.lindanyoka.soccer_442;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by linda.nyoka on 2015-04-09.
 */
public class StateGuy {

   private Context _context;

    public StateGuy(Context context)
    {
        _context = context;
    }


    public void InitializeKey(String key, String value)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key,value);
        editor.apply();
    }
    public void InitializeKey(String key, int value)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key,value);
        editor.apply();
    }
    public void InitializeKey(String key, boolean value)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public int getKeyInt(String value)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
        int name = preferences.getInt(value,0);
        return name;
    }
    public boolean getKeyBool(String value)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
        Boolean name = preferences.getBoolean(value,false);
        return name;
    }
    public String getKeyString(String value)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
        String name = preferences.getString(value,"");
        return name;
    }
    public void Remove(String key)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
        preferences.edit().remove(key).commit();
    }
}
