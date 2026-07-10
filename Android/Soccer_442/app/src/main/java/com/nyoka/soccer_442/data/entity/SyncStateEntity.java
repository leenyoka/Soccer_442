package com.nyoka.soccer_442.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * One row per synced resource (e.g. "matches:PL:fixture", "standings:PL", "news:PL") - the
 * high water mark this device last synced up to. Sent back to the API as `?since=` on the
 * next request for that resource, so only what changed comes back over the wire; the full
 * current set is still read back out of the matching Cached*Entity table below for the UI.
 */
@Entity(tableName = "sync_state")
public class SyncStateEntity {
    @NonNull
    @PrimaryKey
    public String endpointKey;
    public String lastSyncedUtc;
}
