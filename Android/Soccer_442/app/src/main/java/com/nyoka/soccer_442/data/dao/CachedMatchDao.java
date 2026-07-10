package com.nyoka.soccer_442.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nyoka.soccer_442.data.entity.CachedMatchEntity;

import java.util.List;

@Dao
public interface CachedMatchDao {
    @Query("SELECT * FROM cached_matches WHERE competitionCode = :code AND status = :status")
    List<CachedMatchEntity> getAll(String code, String status);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(CachedMatchEntity entity);
}
