package com.nyoka.soccer_442.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "cached_news", primaryKeys = {"competitionCode", "link"})
public class CachedNewsEntity {
    @NonNull
    public String competitionCode;
    @NonNull
    public String link;
    public String title;
    public String imgSrc;
    public String updatedUtc;
}
