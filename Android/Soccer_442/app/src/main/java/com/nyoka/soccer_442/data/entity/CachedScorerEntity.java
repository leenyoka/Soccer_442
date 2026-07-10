package com.nyoka.soccer_442.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** One row per competition - the whole ScorerResponse, upserted whenever it changes. */
@Entity(tableName = "cached_scorers")
public class CachedScorerEntity {
    @NonNull
    @PrimaryKey
    public String competitionCode;
    public String rawJson;
    public String updatedUtc;
}
