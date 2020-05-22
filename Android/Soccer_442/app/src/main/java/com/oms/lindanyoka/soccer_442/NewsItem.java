package com.oms.lindanyoka.soccer_442;

import java.io.Serializable;

/**
 * Created by linda.nyoka on 2015-03-24.
 */
public class NewsItem implements Serializable {
    public String title;
    public String link;
    public String imgSrc;

    public NewsItem(String title, String link)
    {
        this.title = title;
        this.link = link;
        this.imgSrc = "";
    }
    public NewsItem(String title, String link, String imgSrc)
    {
        this.title = title;
        this.link = link;
        this.imgSrc = imgSrc;
    }
    public NewsItem(String value)
    {
        String[] values = value.split("_");

        title = values[0];
        link = values[1];
        imgSrc = values[2];
    }
    public String Compress() {
        String value = String.valueOf(title);
        value += "_" + String.valueOf(link);
        value += "_" + String.valueOf(imgSrc);
        return value;
    }
}
