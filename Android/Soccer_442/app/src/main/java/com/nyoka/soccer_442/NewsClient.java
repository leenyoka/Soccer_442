package com.nyoka.soccer_442;

import android.content.Context;

import com.google.gson.Gson;
import com.nyoka.soccer_442.data.AppDatabase;
import com.nyoka.soccer_442.data.entity.CachedNewsEntity;
import com.nyoka.soccer_442.data.entity.SyncStateEntity;
import com.nyoka.soccer_442.football_data.ApiClient;
import com.nyoka.soccer_442.football_data.CompetitionMap;
import com.nyoka.soccer_442.football_data.FootballData;
import com.nyoka.soccer_442.football_data.SyncEnvelope;

import java.util.ArrayList;
import java.util.List;

/**
 * News now comes from Soccer442.Api (which itself pulls Google News' RSS feed and caches it)
 * instead of this class scraping the feed directly - same incremental-sync pattern as
 * FootballData: last-synced timestamp out of Room, `?since=` to the API, merge, read the full
 * current set back out of Room.
 */
public class NewsClient {
    private final Context context;
    private final ApiClient api = new ApiClient();
    private final Gson gson = new Gson();

    public NewsClient() {
        context = Soccer442Application.getAppContext();
    }

    public ArrayList<NewsItem> GetNews(String competition) {
        ArrayList<NewsItem> items = new ArrayList<>();
        CompetitionMap.Info info = CompetitionMap.byCode(FootballData.GetCompetitionCode(competition));
        if (info == null) return items;

        AppDatabase db = AppDatabase.getInstance(context);
        String key = "news:" + info.code;
        SyncStateEntity state = db.syncStateDao().get(key);
        String since = state != null ? state.lastSyncedUtc : null;

        SyncEnvelope<NewsItem> envelope = api.getNews(info.code, since);
        if (envelope != null && envelope.items != null) {
            for (NewsItem item : envelope.items) {
                if (item.link == null) continue;
                CachedNewsEntity row = new CachedNewsEntity();
                row.competitionCode = info.code;
                row.link = item.link;
                row.title = item.title;
                row.imgSrc = item.imgSrc;
                row.updatedUtc = envelope.serverTimeUtc;
                db.cachedNewsDao().upsert(row);
            }
            SyncStateEntity newState = new SyncStateEntity();
            newState.endpointKey = key;
            newState.lastSyncedUtc = envelope.serverTimeUtc;
            db.syncStateDao().upsert(newState);
        }

        List<CachedNewsEntity> rows = db.cachedNewsDao().getAll(info.code);
        for (CachedNewsEntity row : rows) {
            items.add(new NewsItem(row.title, row.link, row.imgSrc));
        }
        return items;
    }
}
