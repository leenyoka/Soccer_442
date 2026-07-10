package com.nyoka.soccer_442.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nyoka.soccer_442.data.entity.SyncStateEntity;

@Dao
public interface SyncStateDao {
    @Query("SELECT * FROM sync_state WHERE endpointKey = :key")
    SyncStateEntity get(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SyncStateEntity entity);
}
