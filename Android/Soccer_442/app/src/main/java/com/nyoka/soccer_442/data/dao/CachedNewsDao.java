package com.nyoka.soccer_442.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nyoka.soccer_442.data.entity.CachedNewsEntity;

import java.util.List;

@Dao
public interface CachedNewsDao {
    @Query("SELECT * FROM cached_news WHERE competitionCode = :code ORDER BY updatedUtc DESC")
    List<CachedNewsEntity> getAll(String code);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(CachedNewsEntity entity);
}
