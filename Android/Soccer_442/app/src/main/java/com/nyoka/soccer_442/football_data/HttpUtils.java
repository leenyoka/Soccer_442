package com.nyoka.soccer_442.football_data;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtils {

    public static String makeHttpGetRequest(String url, String[] headers) {
        OkHttpClient client = new OkHttpClient.Builder().build();

        Request.Builder requestBuilder = new Request.Builder().url(url);

        if (headers != null) {
            for (int i = 0; i < headers.length; i += 2) {
                requestBuilder.addHeader(headers[i], headers[i + 1]);
            }
        }

        Request request = requestBuilder.build();

        try {
            okhttp3.Response response = client.newCall(request).execute();
            //if (response.isSuccessful()) {
                return response.body().string();
            //}

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

