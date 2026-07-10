package com.nyoka.soccer_442.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nyoka.soccer_442.data.entity.CachedScorerEntity;

@Dao
public interface CachedScorerDao {
    @Query("SELECT * FROM cached_scorers WHERE competitionCode = :code")
    CachedScorerEntity get(String code);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(CachedScorerEntity entity);
}
