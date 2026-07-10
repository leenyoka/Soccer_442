package com.nyoka.soccer_442.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nyoka.soccer_442.data.entity.CachedStandingsEntity;

@Dao
public interface CachedStandingsDao {
    @Query("SELECT * FROM cached_standings WHERE competitionCode = :code")
    CachedStandingsEntity get(String code);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(CachedStandingsEntity entity);
}
