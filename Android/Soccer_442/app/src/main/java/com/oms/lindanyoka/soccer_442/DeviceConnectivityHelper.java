package com.oms.lindanyoka.soccer_442;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by linda.nyoka on 2015-03-01.
 */
public class DeviceConnectivityHelper {
    private static DeviceConnectivityHelper instance = null;
    private Context context;

    private DeviceConnectivityHelper(Context context) {
        this.context = context;
    }

    public static DeviceConnectivityHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DeviceConnectivityHelper(context);
        }
        return instance;
    }

    private ConnectivityManager createConnectivityManager(Context context)
    {
        if (context == null)
            context = context;
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public boolean isInternetOn(Context context) {
        ConnectivityManager connectivityManager = createConnectivityManager(context);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public boolean isWifiOn(Context context) {
        ConnectivityManager connectivityManager = createConnectivityManager( context);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public boolean dataConnection(Context context) {
        ConnectivityManager connectivityManager = createConnectivityManager(context);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = createConnectivityManager(context);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}
