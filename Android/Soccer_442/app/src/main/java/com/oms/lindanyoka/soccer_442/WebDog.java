package com.oms.lindanyoka.soccer_442;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
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
        String value = httpGetResponse(url);
        return value;
    }
    private  String httpGetResponse(String url) {
        String result = "";
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters,30000);
        HttpConnectionParams.setSoTimeout(httpParameters, 30000);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        HttpGet httpget = new HttpGet(replaceAllSpacesInUrl(String.format("http://%s",url)));
        HttpResponse response;
        try {
            response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(instream));
                StringBuilder sb = new StringBuilder();

                while (result != null) {
                    sb.append(result);
                    try {
                        result = br.readLine();
                    } catch (SocketTimeoutException e) {
                        result = null;
                    }
                }
                result = sb.toString();
                instream.close();
            }
        } catch (Exception e) {
            int x  = 0;
            //FlurryAgent.onError("Synchronization", "Error while attempting to contact api for data synchronisation.", e);
        }
        return result;
    }
    public String replaceAllSpacesInUrl(String url){
        return url.replaceAll(" ", "%20");
    }
}
