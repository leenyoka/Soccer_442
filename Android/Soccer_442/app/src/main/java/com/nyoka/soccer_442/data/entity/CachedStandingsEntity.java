package com.nyoka.soccer_442.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** One row per competition - the whole StandingsResponse, upserted whenever it changes. */
@Entity(tableName = "cached_standings")
public class CachedStandingsEntity {
    @NonNull
    @PrimaryKey
    public String competitionCode;
    public String rawJson;
    public String updatedUtc;
}
