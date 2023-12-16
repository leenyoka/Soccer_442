package com.nyoka.soccer_442;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * Created by linda.nyoka on 2015-02-23.
 */
public class Utility {
    public String FixName(String word)
    {
        word = word.toLowerCase();

        String[] numbers = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8","9", ".", "-", " "};

        for (String number :numbers)
            word = word.replace(number, "");

        word = word.trim();

        word = Trim("_",word);

        String newName = "";
        for(char value : word.toCharArray()) {
            if (IsEnglishLetter(value))
                newName += value;
        }

        return newName;
    }
    public static boolean IsEnglishLetter(char c)
    {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }
    public boolean GetMeAnImage(ImageView view, String Name) {

        try  {
            /*
            final R.drawable drawableResources = new R.drawable();
            final Class<R.drawable> c = R.drawable.class;
            final Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                String name = field.getName();


                Name = FixName(Name);

                if(Name.toLowerCase().trim().equals(name.toLowerCase().trim())){
                  //  ||(Name.toLowerCase().trim().contains(name.trim().toLowerCase()))
                //||(name.toLowerCase().trim().contains(Name.toLowerCase().trim()))) {
                    int resourceId = field.getInt(drawableResources);
                    view.setImageResource(resourceId);
                    return true;
                }
            }
             */
        }
        catch (Exception ex){
        }
        return false;
    }
    public boolean GetMeAnImage(TextView view, String Name) {

        try  {
            /*
            final R.drawable drawableResources = new R.drawable();
            final Class<R.drawable> c = R.drawable.class;
            final Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                String name = field.getName();


                Name = FixName(Name);

                if(Name.equals(name)){
                    int resourceId = field.getInt(drawableResources);
                    view.setBackgroundResource(resourceId);
                    view.setText("");
                    return true;
                }
            }
             */
        }
        catch (Exception ex){
        }
        //view.setBackgroundResource(R.drawable.team_name_back);
        view.setText(Name.toUpperCase().substring(0,3));
        return false;
    }
    public boolean GetMeAnImage(TextView view, String Name, boolean notTeamImg) {

        try  {
            /*
            final R.drawable drawableResources = new R.drawable();
            final Class<R.drawable> c = R.drawable.class;
            final Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                String name = field.getName();


                //Name = FixName(Name);

                if(Name.equals(name)){
                    int resourceId = field.getInt(drawableResources);
                    view.setBackgroundResource(resourceId);
                    view.setText("");
                    return true;
                }
            }
             */
        }
        catch (Exception ex){
        }
        view.setBackgroundResource(R.drawable.invisible);
        //view.setText(Name.toUpperCase().substring(0,3));
        return false;
    }
    public String Trim(String value, String host)
    {
        while (host.endsWith(value))
            host = host.substring(0, host.length()-1);

        while (host.startsWith(value))
            host = host.substring(1);

        return host;
    }
    public void showDialog(String message, boolean saved, String heading, FragmentManager manager)
    {
        FragmentManager fm = manager;
        activity_msg acceptTermsDialogFragment = new activity_msg();
        Bundle args = new Bundle();
        args.putString("message", message);
        args.putBoolean("post", saved);
        args.putString("heading",heading);
        acceptTermsDialogFragment.setArguments(args);
        acceptTermsDialogFragment.show(fm, "");
    }

    public void ShowNetworkError(FragmentManager manager)
    {
        showDialog("No active network connection found. please enable data or connect to wifi"
                , false, "No Internet Connection", manager);
    }

    public void ShowNetworkError(TextView view, String txt)
    {
        view.setText(txt + "(offline)");
    }
    public boolean Connected(Context context)
    {
        DeviceConnectivityHelper connectivityHelper = DeviceConnectivityHelper.getInstance(context);
        if(!connectivityHelper.isInternetOn(context) && !WifiConnected(context)) {
            return false;
        }
        return true;
    }
    public boolean WifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            return true;
        }
        return false;
    }
    public boolean IsInt(String value)
    {
        try {
            int x = Integer.parseInt(value);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }
}
