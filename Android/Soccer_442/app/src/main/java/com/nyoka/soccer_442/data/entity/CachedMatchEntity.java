package com.nyoka.soccer_442.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

/** One row per match within a (competition, status) group - status is "fixture"/"result"/
 * "live", matching FootballData's three GetFixture/GetResults/GetLive calls. */
@Entity(tableName = "cached_matches", primaryKeys = {"competitionCode", "status", "matchId"})
public class CachedMatchEntity {
    @NonNull
    public String competitionCode;
    @NonNull
    public String status;
    @NonNull
    public String matchId;
    public String rawJson;
    public String updatedUtc;
}
