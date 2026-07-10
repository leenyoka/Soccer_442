using System.Text.Json;
using Microsoft.EntityFrameworkCore;
using Soccer442.Api.Data;
using Soccer442.Api.Data.Entities;
using Soccer442.Api.Models;
using Soccer442.Api.Sources;

namespace Soccer442.Api.Services;

/// <summary>
/// Read-through cache orchestrator - this project's equivalent of the Android app's
/// FootballData.java facade. On request: if the Postgres-cached row(s) for that resource are
/// fresh (within a per-resource TTL), served straight from the DB; if stale/missing, re-runs
/// the same ESPN -> OpenLigaDB -> BBC -> SportsDB fallback chain the Android app used to run
/// directly, upserts the result (only bumping LastUpdatedUtc on rows that actually changed,
/// so the incremental-sync contract stays meaningful), then serves it.
/// </summary>
public class FootballCacheService
{
    private static readonly TimeSpan LiveTtl = TimeSpan.FromSeconds(60);
    private static readonly TimeSpan FixtureResultTtl = TimeSpan.FromMinutes(5);
    private static readonly TimeSpan StandingsScorersTtl = TimeSpan.FromHours(1);
    private static readonly TimeSpan NewsTtl = TimeSpan.FromHours(6);
    private static readonly TimeSpan ProfileTtl = TimeSpan.FromDays(30);

    private readonly AppDbContext _db;
    private readonly EspnClient _espn;
    private readonly BbcClient _bbc;
    private readonly OpenLigaDbClient _openLigaDb;
    private readonly SportsDbClient _sportsDb;
    private readonly NewsClient _news;
    private readonly WikipediaClient _wikipedia;
    private readonly ILogger<FootballCacheService> _logger;

    public FootballCacheService(AppDbContext db, EspnClient espn, BbcClient bbc, OpenLigaDbClient openLigaDb,
        SportsDbClient sportsDb, NewsClient news, WikipediaClient wikipedia, ILogger<FootballCacheService> logger)
    {
        _db = db;
        _espn = espn;
        _bbc = bbc;
        _openLigaDb = openLigaDb;
        _sportsDb = sportsDb;
        _news = news;
        _wikipedia = wikipedia;
        _logger = logger;
    }

    public static List<Competition> GetCompetitions() => CompetitionMap.All()
        .Select(info => new Competition { Name = info.Name, Code = info.Code })
        .ToList();

    // --- Matches (fixture/result/live) ------------------------------------------

    public async Task<SyncEnvelope<FootballMatch>?> GetMatchesAsync(string code, string statusParam, DateTimeOffset since)
    {
        var info = CompetitionMap.ByCode(code);
        if (info == null) return null;
        var wanted = statusParam.ToLowerInvariant() switch
        {
            "fixture" => MatchStatus.Scheduled,
            "result" => MatchStatus.Finished,
            "live" => MatchStatus.Live,
            _ => MatchStatus.Other,
        };
        if (wanted == MatchStatus.Other) return null;

        var now = DateTimeOffset.UtcNow;
        var ttl = wanted == MatchStatus.Live ? LiveTtl : FixtureResultTtl;

        var groupNewest = await _db.Matches
            .Where(m => m.CompetitionCode == code && m.Status == statusParam)
            .OrderByDescending(m => m.LastUpdatedUtc)
            .Select(m => (DateTimeOffset?)m.LastUpdatedUtc)
            .FirstOrDefaultAsync();

        if (groupNewest == null || now - groupNewest.Value > ttl)
        {
            await RefreshMatchesAsync(info, wanted, statusParam, now);
        }

        var rows = await _db.Matches
            .Where(m => m.CompetitionCode == code && m.Status == statusParam && m.LastUpdatedUtc > since)
            .ToListAsync();

        return new SyncEnvelope<FootballMatch>
        {
            ServerTimeUtc = now,
            Items = rows.Select(r => JsonSerializer.Deserialize<FootballMatch>(r.RawJson, ApiJson.Options)!).ToList(),
        };
    }

    private async Task RefreshMatchesAsync(CompetitionMap.Info info, MatchStatus wanted, string statusParam, DateTimeOffset now)
    {
        var requestName = $"getMatches({info.Code},{wanted})";
        var matches = await _espn.GetMatchesAsync(info, wanted);
        if (matches.Count > 0) DataSourceHealth.LogServedBy(requestName, DataSourceHealth.Source.Espn, _logger);

        if (matches.Count == 0 && info.OpenLigaDbShortcut != null && DataSourceHealth.IsAvailable(DataSourceHealth.Source.OpenLigaDb, _logger))
        {
            var all = await _openLigaDb.GetMatchesAsync(info);
            matches = all.Where(m =>
                (wanted == MatchStatus.Finished && m.Status == "FINISHED") ||
                (wanted == MatchStatus.Scheduled && m.Status == "SCHEDULED")).ToList();
            if (matches.Count > 0) DataSourceHealth.LogServedBy(requestName, DataSourceHealth.Source.OpenLigaDb, _logger);
        }

        if (matches.Count == 0 && DataSourceHealth.IsAvailable(DataSourceHealth.Source.Bbc, _logger))
        {
            matches = await _bbc.GetMatchesAsync(info, wanted);
            if (matches.Count > 0) DataSourceHealth.LogServedBy(requestName, DataSourceHealth.Source.Bbc, _logger);
        }

        if (matches.Count == 0 && DataSourceHealth.IsAvailable(DataSourceHealth.Source.SportsDb, _logger))
        {
            matches = await _sportsDb.GetMatchesAsync(info, wanted);
            if (matches.Count > 0) DataSourceHealth.LogServedBy(requestName, DataSourceHealth.Source.SportsDb, _logger);
        }

        if (matches.Count == 0)
        {
            DataSourceHealth.LogNoSourceAvailable(requestName, _logger);
            return;
        }

        var existingRows = await _db.Matches.Where(m => m.CompetitionCode == info.Code && m.Status == statusParam).ToListAsync();
        var existingById = existingRows.ToDictionary(r => r.MatchId);

        foreach (var match in matches)
        {
            var matchId = match.Id.ToString();
            var json = JsonSerializer.Serialize(match, ApiJson.Options);
            if (existingById.TryGetValue(matchId, out var row))
            {
                if (row.RawJson != json) { row.RawJson = json; row.UtcDate = match.UtcDate; row.LastUpdatedUtc = now; }
            }
            else
            {
                _db.Matches.Add(new MatchEntity { CompetitionCode = info.Code, Status = statusParam, MatchId = matchId, UtcDate = match.UtcDate, RawJson = json, LastUpdatedUtc = now });
            }
        }
        await _db.SaveChangesAsync();
    }

    /// <summary>Live-only lookup (no DB cache table for single-match details, mirroring the
    /// Android app's GetMatchDetails which always fetched fresh) - ESPN find+enrich, falling
    /// back to BBC then SportsDB.</summary>
    public async Task<FootballMatch?> GetMatchDetailsAsync(string code, string matchId)
    {
        var info = CompetitionMap.ByCode(code);
        if (info == null) return null;
        var requestName = $"GetMatchDetails({code})";

        if (DataSourceHealth.IsAvailable(DataSourceHealth.Source.Espn, _logger))
        {
            var match = await _espn.FindMatchByIdAsync(info, matchId);
            if (match != null)
            {
                await _espn.EnrichWithLineupAndStatsAsync(info, matchId, match);
                DataSourceHealth.LogServedBy(requestName, DataSourceHealth.Source.Espn, _logger);
                return match;
            }
        }

        if (DataSourceHealth.IsAvailable(DataSourceHealth.Source.Bbc, _logger))
        {
            var match = _bbc.FindMatchById(matchId);
            if (match != null)
            {
                await _bbc.EnrichWithLineupAndStatsAsync(match);
                DataSourceHealth.LogServedBy(requestName, DataSourceHealth.Source.Bbc, _logger);
                return match;
            }
        }

        if (DataSourceHealth.IsAvailable(DataSourceHealth.Source.SportsDb, _logger))
        {
            var match = await _sportsDb.FindMatchByIdAsync(matchId);
            if (match != null)
            {
                DataSourceHealth.LogServedBy(requestName, DataSourceHealth.Source.SportsDb, _logger);
                return match;
            }
        }

        DataSourceHealth.LogNoSourceAvailable(requestName, _logger);
        return null;
    }

    /// <summary>ESPN-only, live (matches the Android app - the only source with a structured
    /// events feed at all).</summary>
    public async Task<List<Comment>> GetCommentaryAsync(string code, string matchId)
    {
        var info = CompetitionMap.ByCode(code);
        if (info == null || !DataSourceHealth.IsAvailable(DataSourceHealth.Source.Espn, _logger)) return new List<Comment>();
        return await _espn.GetCommentaryAsync(info, matchId);
    }

    public async Task<HeadToHeadSummary?> GetHeadToHeadAndFormAsync(string code, string matchId, string homeTeamName, string awayTeamName, string? utcDate)
    {
        var info = CompetitionMap.ByCode(code);
        if (info == null) return null;
        var requestName = $"GetHeadToHeadAndForm({code})";

        HeadToHeadSummary? summary = null;
        if (DataSourceHealth.IsAvailable(DataSourceHealth.Source.Espn, _logger))
        {
            summary = await _espn.GetHeadToHeadAndFormAsync(info, matchId, homeTeamName, awayTeamName);
            if (summary != null) DataSourceHealth.LogServedBy(requestName, DataSourceHealth.Source.Espn, _logger);
        }
        if (summary == null && utcDate != null && DataSourceHealth.IsAvailable(DataSourceHealth.Source.Bbc, _logger))
        {
            summary = await _bbc.GetHeadToHeadAndFormAsync(homeTeamName, awayTeamName, utcDate);
            if (summary != null) DataSourceHealth.LogServedBy(requestName, DataSourceHealth.Source.Bbc, _logger);
        }
        if (summary == null) DataSourceHealth.LogNoSourceAvailable(requestName, _logger);
        return summary;
    }

    // --- Standings ---------------------------------------------------------------

    public async Task<SyncEnvelope<StandingsResponse>?> GetStandingsAsync(string code, DateTimeOffset since)
    {
        var info = CompetitionMap.ByCode(code);
        if (info == null) return null;
        var now = DateTimeOffset.UtcNow;

        var row = await _db.Standings.FirstOrDefaultAsync(s => s.CompetitionCode == code);
        if (row == null || now - row.LastUpdatedUtc > StandingsScorersTtl)
        {
            row = await RefreshStandingsAsync(info, now, row);
        }

        var items = new List<StandingsResponse>();
        if (row != null && row.LastUpdatedUtc > since)
            items.Add(JsonSerializer.Deserialize<StandingsResponse>(row.RawJson, ApiJson.Options)!);

        return new SyncEnvelope<StandingsResponse> { ServerTimeUtc = now, Items = items };
    }

    private async Task<StandingsEntity?> RefreshStandingsAsync(CompetitionMap.Info info, DateTimeOffset now, StandingsEntity? existing)
    {
        var requestName = $"GetLog({info.Code})";
        StandingsResponse? standings = null;
        if (DataSourceHealth.IsAvailable(DataSourceHealth.Source.Espn, _logger))
        {
            standings = await _espn.GetStandingsAsync(info);
            if (standings != null) DataSourceHealth.LogServedBy(requestName, DataSourceHealth.Source.Espn, _logger);
        }
        if (standings == null && DataSourceHealth.IsAvailable(DataSourceHealth.Source.OpenLigaDb, _logger))
        {
            standings = await _openLigaDb.GetStandingsAsync(info);
            if (standings != null) DataSourceHealth.LogServedBy(requestName, DataSourceHealth.Source.OpenLigaDb, _logger);
        }
        if (standings == null && DataSourceHealth.IsAvailable(DataSourceHealth.Source.Bbc, _logger))
        {
            standings = await _bbc.GetStandingsAsync(info);
            if (standings != null) DataSourceHealth.LogServedBy(requestName, DataSourceHealth.Source.Bbc, _logger);
        }
        if (standings == null && DataSourceHealth.IsAvailable(DataSourceHealth.Source.SportsDb, _logger))
        {
            standings = await _sportsDb.GetStandingsAsync(info);
            if (standings != null) DataSourceHealth.LogServedBy(requestName, DataSourceHealth.Source.SportsDb, _logger);
        }
        if (standings == null)
        {
            DataSourceHealth.LogNoSourceAvailable(requestName, _logger);
            return existing;
        }

        var json = JsonSerializer.Serialize(standings, ApiJson.Options);
        if (existing == null)
        {
            existing = new StandingsEntity { CompetitionCode = info.Code, RawJson = json, LastUpdatedUtc = now };
            _db.Standings.Add(existing);
        }
        else if (existing.RawJson != json)
        {
            existing.RawJson = json;
            existing.LastUpdatedUtc = now;
        }
        await _db.SaveChangesAsync();
        return existing;
    }

    // --- Scorers -------------------------------------------------------------

    public async Task<SyncEnvelope<ScorerResponse>?> GetScorersAsync(string code, DateTimeOffset since)
    {
        var info = CompetitionMap.ByCode(code);
        if (info == null) return null;
        var now = DateTimeOffset.UtcNow;

        var row = await _db.Scorers.FirstOrDefaultAsync(s => s.CompetitionCode == code);
        if (row == null || now - row.LastUpdatedUtc > StandingsScorersTtl)
        {
            row = await RefreshScorersAsync(info, now, row);
        }

        var items = new List<ScorerResponse>();
        if (row != null && row.LastUpdatedUtc > since)
            items.Add(JsonSerializer.Deserialize<ScorerResponse>(row.RawJson, ApiJson.Options)!);

        return new SyncEnvelope<ScorerResponse> { ServerTimeUtc = now, Items = items };
    }

    private async Task<ScorerEntity?> RefreshScorersAsync(CompetitionMap.Info info, DateTimeOffset now, ScorerEntity? existing)
    {
        var scorers = await _espn.GetTopScorersAsync(info, 10);
        var response = new ScorerResponse { Competition = new Competition { Name = info.Name, Code = info.Code }, Scorers = scorers, Count = scorers.Count };
        var json = JsonSerializer.Serialize(response, ApiJson.Options);

        if (existing == null)
        {
            existing = new ScorerEntity { CompetitionCode = info.Code, RawJson = json, LastUpdatedUtc = now };
            _db.Scorers.Add(existing);
        }
        else if (existing.RawJson != json)
        {
            existing.RawJson = json;
            existing.LastUpdatedUtc = now;
        }
        await _db.SaveChangesAsync();
        return existing;
    }

    // --- News ------------------------------------------------------------------

    public async Task<SyncEnvelope<NewsItem>?> GetNewsAsync(string code, DateTimeOffset since)
    {
        var info = CompetitionMap.ByCode(code);
        if (info == null) return null;
        var now = DateTimeOffset.UtcNow;

        var newest = await _db.NewsItems
            .Where(n => n.CompetitionCode == code)
            .OrderByDescending(n => n.LastUpdatedUtc)
            .Select(n => (DateTimeOffset?)n.LastUpdatedUtc)
            .FirstOrDefaultAsync();

        if (newest == null || now - newest.Value > NewsTtl)
        {
            await RefreshNewsAsync(info, now);
        }

        var rows = await _db.NewsItems.Where(n => n.CompetitionCode == code && n.LastUpdatedUtc > since).ToListAsync();
        return new SyncEnvelope<NewsItem>
        {
            ServerTimeUtc = now,
            Items = rows.Select(r => new NewsItem(r.Title, r.Link, r.ImgSrc)).ToList(),
        };
    }

    private async Task RefreshNewsAsync(CompetitionMap.Info info, DateTimeOffset now)
    {
        var items = await _news.GetNewsAsync(info.Name);
        if (items.Count == 0) return;

        var existingRows = await _db.NewsItems.Where(n => n.CompetitionCode == info.Code).ToListAsync();
        var existingByLink = existingRows.ToDictionary(r => r.Link);

        foreach (var item in items)
        {
            if (item.Link == null) continue;
            if (existingByLink.TryGetValue(item.Link, out var row))
            {
                if (row.Title != item.Title || row.ImgSrc != item.ImgSrc) { row.Title = item.Title; row.ImgSrc = item.ImgSrc; row.LastUpdatedUtc = now; }
            }
            else
            {
                _db.NewsItems.Add(new NewsEntity { CompetitionCode = info.Code, Link = item.Link, Title = item.Title, ImgSrc = item.ImgSrc, LastUpdatedUtc = now });
            }
        }
        await _db.SaveChangesAsync();
    }

    // --- Profiles (Wikipedia) --------------------------------------------------

    public async Task<PlayerProfile?> GetPlayerProfileAsync(string name)
    {
        var key = name.Trim().ToLowerInvariant();
        var now = DateTimeOffset.UtcNow;
        var row = await _db.PlayerProfiles.FirstOrDefaultAsync(p => p.PlayerName == key);
        if (row != null && now - row.LastUpdatedUtc <= ProfileTtl)
            return JsonSerializer.Deserialize<PlayerProfile>(row.RawJson, ApiJson.Options);

        var profile = await _wikipedia.GetPlayerProfileAsync(name);
        if (profile == null) return row != null ? JsonSerializer.Deserialize<PlayerProfile>(row.RawJson, ApiJson.Options) : null;

        var json = JsonSerializer.Serialize(profile, ApiJson.Options);
        if (row == null) _db.PlayerProfiles.Add(new PlayerProfileEntity { PlayerName = key, RawJson = json, LastUpdatedUtc = now });
        else { row.RawJson = json; row.LastUpdatedUtc = now; }
        await _db.SaveChangesAsync();
        return profile;
    }

    public async Task<string?> GetPlayerPhotoUrlAsync(string name)
    {
        // Piggybacks on the same cached profile row as GetPlayerProfileAsync - a lineup chip
        // asking for just a photo and a later profile-screen open for the same player share
        // one cache entry instead of two.
        var profile = await GetPlayerProfileAsync(name);
        return profile?.PhotoUrl;
    }

    public async Task<TeamProfile?> GetTeamProfileAsync(string name)
    {
        var key = name.Trim().ToLowerInvariant();
        var now = DateTimeOffset.UtcNow;
        var row = await _db.TeamProfiles.FirstOrDefaultAsync(t => t.TeamName == key);
        if (row != null && now - row.LastUpdatedUtc <= ProfileTtl)
            return JsonSerializer.Deserialize<TeamProfile>(row.RawJson, ApiJson.Options);

        var profile = await _wikipedia.GetTeamProfileAsync(name);
        if (profile == null) return row != null ? JsonSerializer.Deserialize<TeamProfile>(row.RawJson, ApiJson.Options) : null;

        var json = JsonSerializer.Serialize(profile, ApiJson.Options);
        if (row == null) _db.TeamProfiles.Add(new TeamProfileEntity { TeamName = key, RawJson = json, LastUpdatedUtc = now });
        else { row.RawJson = json; row.LastUpdatedUtc = now; }
        await _db.SaveChangesAsync();
        return profile;
    }
}
