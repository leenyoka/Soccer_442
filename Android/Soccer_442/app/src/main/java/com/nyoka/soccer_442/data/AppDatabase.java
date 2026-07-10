package com.nyoka.soccer_442.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.nyoka.soccer_442.data.dao.CachedMatchDao;
import com.nyoka.soccer_442.data.dao.CachedNewsDao;
import com.nyoka.soccer_442.data.dao.CachedScorerDao;
import com.nyoka.soccer_442.data.dao.CachedStandingsDao;
import com.nyoka.soccer_442.data.dao.SyncStateDao;
import com.nyoka.soccer_442.data.entity.CachedMatchEntity;
import com.nyoka.soccer_442.data.entity.CachedNewsEntity;
import com.nyoka.soccer_442.data.entity.CachedScorerEntity;
import com.nyoka.soccer_442.data.entity.CachedStandingsEntity;
import com.nyoka.soccer_442.data.entity.SyncStateEntity;

/**
 * The client half of the incremental-sync design (see ApiClient/SyncEnvelope): tracks, per
 * resource, the last server time this device has synced up to (SyncStateEntity) and holds the
 * last-known-good full dataset locally (the Cached*Entity tables) so the UI always has a
 * complete list to render even though the wire transfer itself is delta-only. Also gives the
 * app basic offline resilience as a side effect - if the API is unreachable, whatever was
 * cached from the last successful sync still renders.
 */
@Database(entities = {
        SyncStateEntity.class,
        CachedMatchEntity.class,
        CachedStandingsEntity.class,
        CachedScorerEntity.class,
        CachedNewsEntity.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract SyncStateDao syncStateDao();
    public abstract CachedMatchDao cachedMatchDao();
    public abstract CachedStandingsDao cachedStandingsDao();
    public abstract CachedScorerDao cachedScorerDao();
    public abstract CachedNewsDao cachedNewsDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "soccer442.db")
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return instance;
    }
}
