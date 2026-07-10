using System.Collections.Concurrent;
using System.Text.Json;
using Soccer442.Api.Models;

namespace Soccer442.Api.Sources;

/// <summary>
/// C# port of SportsDbClient.java - thesportsdb.com's free, key-less JSON REST API (using
/// their public "3" test key, the standard free-tier key documented for exactly this kind of
/// hobby/non-commercial use). A genuine documented API, not scraped page data - used last
/// because its free tier is rate-limited and doesn't expose lineups/stats/head-to-head, only
/// matches and standings. Fails soft like every other source here.
/// </summary>
public class SportsDbClient
{
    private const string Base = "https://www.thesportsdb.com/api/v1/json/3";

    private readonly HttpFetcher _dog;
    private readonly ILogger<SportsDbClient> _logger;

    public SportsDbClient(HttpFetcher dog, ILogger<SportsDbClient> logger)
    {
        _dog = dog;
        _logger = logger;
    }

    private static string CurrentSeasonLabel()
    {
        var now = DateTime.UtcNow;
        int startYear = now.Month >= 7 ? now.Year : now.Year - 1;
        return $"{startYear}-{startYear + 1}";
    }

    private async Task<JsonElement?> FetchJsonAsync(string url)
    {
        try
        {
            var raw = await _dog.FetchAsync(url);
            if (raw == null) return null;
            using var doc = JsonDocument.Parse(raw);
            return doc.RootElement.ValueKind == JsonValueKind.Object ? doc.RootElement.Clone() : null;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "fetchJson failed for {Url}", url);
            return null;
        }
    }

    // --- matchId -> raw event lookup (same pattern as BbcClient - no id shared with ESPN) ---
    private const int MaxCache = 300;
    private static readonly ConcurrentDictionary<int, string> EventIdCache = new();
    private static readonly ConcurrentQueue<int> EventIdOrder = new();

    private static void CacheEventId(int syntheticId, string realId)
    {
        EventIdCache[syntheticId] = realId;
        EventIdOrder.Enqueue(syntheticId);
        while (EventIdOrder.Count > MaxCache && EventIdOrder.TryDequeue(out var oldest))
            EventIdCache.TryRemove(oldest, out _);
    }

    public async Task<FootballMatch?> FindMatchByIdAsync(string matchId)
    {
        try
        {
            if (!int.TryParse(matchId, out var syntheticId) || !EventIdCache.TryGetValue(syntheticId, out var realId)) return null;
            var root = await FetchJsonAsync($"{Base}/lookupevent.php?id={realId}");
            if (root == null || !root.Value.TryGetProperty("events", out var events) || events.ValueKind != JsonValueKind.Array)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.SportsDb, "lookupevent returned no events", _logger);
                return null;
            }
            if (events.GetArrayLength() == 0 || events[0].ValueKind == JsonValueKind.Null) return null;
            var match = MapEvent(events[0], null);
            DataSourceHealth.RecordSuccess(DataSourceHealth.Source.SportsDb, _logger);
            return match;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "findMatchById failed for {MatchId}", matchId);
            DataSourceHealth.RecordFailure(DataSourceHealth.Source.SportsDb, $"findMatchById exception: {ex.Message}", _logger);
            return null;
        }
    }

    public async Task<List<FootballMatch>> GetMatchesAsync(CompetitionMap.Info info, MatchStatus status)
    {
        var results = new List<FootballMatch>();
        if (info.SportsDbLeagueId == null) return results;
        // No reliable free-tier livescore access on the "3" test key - LIVE is left to the
        // other sources rather than guessing from a near-kickoff-time heuristic.
        if (status == MatchStatus.Live) return results;

        var endpoint = status == MatchStatus.Finished ? "eventspastleague.php" : "eventsnextleague.php";
        try
        {
            var root = await FetchJsonAsync($"{Base}/{endpoint}?id={info.SportsDbLeagueId}");
            if (root == null)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.SportsDb, $"{endpoint} returned nothing", _logger);
                return results;
            }
            if (!root.Value.TryGetProperty("events", out var events) || events.ValueKind != JsonValueKind.Array)
            {
                // A genuinely empty season window (e.g. off-season) still parses fine as
                // {"events":null} - that's not a source failure, just no data right now.
                DataSourceHealth.RecordSuccess(DataSourceHealth.Source.SportsDb, _logger);
                return results;
            }

            foreach (var el in events.EnumerateArray())
            {
                if (el.ValueKind == JsonValueKind.Null) continue;
                var match = MapEvent(el, info);
                if (match != null) results.Add(match);
            }
            DataSourceHealth.RecordSuccess(DataSourceHealth.Source.SportsDb, _logger);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "getMatches failed for {Code}", info.Code);
            DataSourceHealth.RecordFailure(DataSourceHealth.Source.SportsDb, $"getMatches exception: {ex.Message}", _logger);
        }
        return results;
    }

    private static FootballMatch? MapEvent(JsonElement ev, CompetitionMap.Info? info)
    {
        try
        {
            var realId = EspnJsonUtil.StringField(ev, "idEvent", null);
            if (realId == null) return null;

            var match = new FootballMatch { Area = new Area(), Competition = new Competition() };
            if (info != null)
            {
                match.Area!.Name = info.AreaName;
                match.Competition!.Name = info.Name;
                match.Competition.Code = info.Code;
            }
            else
            {
                match.Competition!.Name = EspnJsonUtil.StringField(ev, "strLeague", "");
            }
            // Offset so SportsDB-sourced ids can't collide with ESPN/OpenLigaDB/BBC ids.
            match.Id = 4_000_000 + Math.Abs(realId.GetHashCode() % 1_000_000);
            CacheEventId(match.Id, realId);

            var dateEvent = EspnJsonUtil.StringField(ev, "dateEvent", null);
            var timeEvent = EspnJsonUtil.StringField(ev, "strTime", "00:00:00");
            match.UtcDate = dateEvent != null ? $"{dateEvent}T{timeEvent}Z" : null;

            var homeScoreRaw = EspnJsonUtil.StringField(ev, "intHomeScore", null);
            var awayScoreRaw = EspnJsonUtil.StringField(ev, "intAwayScore", null);
            match.Status = homeScoreRaw != null && awayScoreRaw != null ? "FINISHED" : "SCHEDULED";

            match.HomeTeam = new Team { Name = EspnJsonUtil.StringField(ev, "strHomeTeam", "Unknown"), Crest = EspnJsonUtil.StringField(ev, "strHomeTeamBadge", null) };
            match.AwayTeam = new Team { Name = EspnJsonUtil.StringField(ev, "strAwayTeam", "Unknown"), Crest = EspnJsonUtil.StringField(ev, "strAwayTeamBadge", null) };

            match.Score = new Score
            {
                Duration = "REGULAR",
                FullTime = new FullTime { Home = ParseIntSafe(homeScoreRaw), Away = ParseIntSafe(awayScoreRaw) },
                HalfTime = new HalfTime(),
            };
            return match;
        }
        catch
        {
            return null;
        }
    }

    private static int ParseIntSafe(string? s) => int.TryParse(s, out var v) ? v : 0;

    public async Task<StandingsResponse?> GetStandingsAsync(CompetitionMap.Info info)
    {
        if (info.SportsDbLeagueId == null) return null;
        try
        {
            var url = $"{Base}/lookuptable.php?l={info.SportsDbLeagueId}&s={CurrentSeasonLabel()}";
            var root = await FetchJsonAsync(url);
            if (root == null || !root.Value.TryGetProperty("table", out var tableEl) || tableEl.ValueKind != JsonValueKind.Array)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.SportsDb, "lookuptable returned no table", _logger);
                return null;
            }

            var table = new List<TableItem>();
            foreach (var row in tableEl.EnumerateArray())
            {
                table.Add(new TableItem
                {
                    Position = (int)EspnJsonUtil.NumberField(row, "intRank", 0),
                    Team = new Team { Name = EspnJsonUtil.StringField(row, "strTeam", "Unknown"), Crest = EspnJsonUtil.StringField(row, "strBadge", null) },
                    PlayedGames = (int)EspnJsonUtil.NumberField(row, "intPlayed", 0),
                    Won = (int)EspnJsonUtil.NumberField(row, "intWin", 0),
                    Draw = (int)EspnJsonUtil.NumberField(row, "intDraw", 0),
                    Lost = (int)EspnJsonUtil.NumberField(row, "intLoss", 0),
                    Points = (int)EspnJsonUtil.NumberField(row, "intPoints", 0),
                    GoalsFor = (int)EspnJsonUtil.NumberField(row, "intGoalsFor", 0),
                    GoalsAgainst = (int)EspnJsonUtil.NumberField(row, "intGoalsAgainst", 0),
                    GoalDifference = (int)EspnJsonUtil.NumberField(row, "intGoalDifference", 0),
                });
            }
            if (table.Count == 0)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.SportsDb, "lookuptable returned an empty table", _logger);
                return null;
            }

            DataSourceHealth.RecordSuccess(DataSourceHealth.Source.SportsDb, _logger);
            return new StandingsResponse
            {
                Area = new Area { Name = info.AreaName },
                Competition = new Competition { Name = info.Name, Code = info.Code },
                Standings = new List<StandingsItem> { new() { Stage = "REGULAR_SEASON", Type = "TOTAL", Table = table } },
            };
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "getStandings failed for {Code}", info.Code);
            DataSourceHealth.RecordFailure(DataSourceHealth.Source.SportsDb, $"getStandings exception: {ex.Message}", _logger);
            return null;
        }
    }
}
