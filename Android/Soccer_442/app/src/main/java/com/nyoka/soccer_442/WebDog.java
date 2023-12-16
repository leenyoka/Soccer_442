package com.nyoka.soccer_442;
/*
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
*/
import com.nyoka.soccer_442.football_data.HttpUtils;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;

/**
 * Created by linda.nyoka on 2015-02-22.
 */
public class WebDog {
    public  String Fetch(String url)
    {
        String[] headers = {"X-Auth-Token", "4ae744243d1c42bf984fccfe16f3eb49", "Content-Type", "application/json"};
        String value =  HttpUtils.makeHttpGetRequest(url, headers);
        return value;
    }

    public String replaceAllSpacesInUrl(String url){
        return url.replaceAll(" ", "%20");
    }
}
