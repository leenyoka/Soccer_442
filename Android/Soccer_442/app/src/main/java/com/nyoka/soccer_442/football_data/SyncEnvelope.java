package com.nyoka.soccer_442.football_data;

import java.util.List;

/**
 * Mirrors the API's SyncEnvelope&lt;T&gt; - every incremental-sync endpoint returns items
 * changed since the caller's last sync, plus the server's own clock so the caller can
 * persist a clock-skew-proof high water mark for its next request.
 */
public class SyncEnvelope<T> {
    public String serverTimeUtc;
    public List<T> items;
}
