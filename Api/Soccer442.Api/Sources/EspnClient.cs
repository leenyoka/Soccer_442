using System.Text.Json;
using Soccer442.Api.Models;

namespace Soccer442.Api.Sources;

public enum MatchStatus { Scheduled, Live, Finished, Other }

/// <summary>
/// C# port of EspnClient.java - talks to ESPN's public (undocumented, but key-less and not
/// bot-walled) JSON API - site.api.espn.com. No account, no subscription. Every method fails
/// soft (returns null/empty) so the caller (FootballCacheService) can fall back to a
/// secondary source instead of erroring on a bad/changed response.
/// </summary>
public class EspnClient
{
    private const string SiteBase = "https://site.api.espn.com/apis/site/v2/sports/soccer";
    private const string StandingsBase = "https://site.api.espn.com/apis/v2/sports/soccer";
    // Separate host - ESPN's "core" hypermedia API, distinct from site.api.espn.com. This is
    // where per-season stat leaders (top scorers) live; the site API has no such endpoint.
    private const string CoreBase = "https://sports.core.api.espn.com/v2/sports/soccer/leagues";

    private readonly HttpFetcher _dog;
    private readonly ILogger<EspnClient> _logger;

    public EspnClient(HttpFetcher dog, ILogger<EspnClient> logger)
    {
        _dog = dog;
        _logger = logger;
    }

    public static MatchStatus Classify(EspnScoreboardResponse.EspnEvent ev)
        => ev.Status?.Type?.State switch
        {
            "in" => MatchStatus.Live,
            "post" => MatchStatus.Finished,
            "pre" => MatchStatus.Scheduled,
            _ => MatchStatus.Other,
        };

    private static string ToFdStatus(MatchStatus status, string? detail) => status switch
    {
        MatchStatus.Live => detail == "Halftime" ? "PAUSED" : "IN_PLAY",
        MatchStatus.Finished => "FINISHED",
        _ => "SCHEDULED",
    };

    // ESPN's /scoreboard endpoint only ever returns a single matchweek's worth of events,
    // even when a wide "dates=start-end" range is requested - a full fixtures/results list
    // needs paging through several weekly windows and merging.
    private const int WeeksForward = 10;
    private const int WeeksBack = 10;

    public async Task<List<FootballMatch>> GetMatchesAsync(CompetitionMap.Info info, MatchStatus wantedStatus)
    {
        var results = new List<FootballMatch>();

        if (wantedStatus == MatchStatus.Live)
        {
            await FetchWindowAsync(info, null, wantedStatus, results, new HashSet<string>());
            return results;
        }

        int weeks = wantedStatus == MatchStatus.Scheduled ? WeeksForward : WeeksBack;
        int direction = wantedStatus == MatchStatus.Scheduled ? 1 : -1;
        var seenIds = new HashSet<string>();

        for (int i = 0; i < weeks; i++)
        {
            // A real outage (not just one bad window) trips this partway through the loop -
            // no point burning the remaining requests against a host that's already confirmed down.
            if (!DataSourceHealth.IsAvailable(DataSourceHealth.Source.Espn, _logger)) break;

            var boundaryA = DateTime.UtcNow.AddDays(direction * i * 7);
            var boundaryB = boundaryA.AddDays(direction * 6);
            var (start, end) = direction == 1 ? (boundaryA, boundaryB) : (boundaryB, boundaryA);
            var range = $"{start:yyyyMMdd}-{end:yyyyMMdd}";
            await FetchWindowAsync(info, range, wantedStatus, results, seenIds);
        }
        return results;
    }

    private async Task FetchWindowAsync(CompetitionMap.Info info, string? dateRange, MatchStatus wantedStatus, List<FootballMatch> results, HashSet<string> seenIds)
    {
        try
        {
            var url = $"{SiteBase}/{info.EspnSlug}/scoreboard" + (dateRange != null ? $"?dates={dateRange}" : "");
            var raw = await _dog.FetchAsync(url);
            if (raw == null)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"network fetch returned null for {url}", _logger);
                return;
            }

            var response = JsonSerializer.Deserialize<EspnScoreboardResponse>(raw, EspnJsonUtil.CaseInsensitive);
            if (response?.Events == null) return;
            DataSourceHealth.RecordSuccess(DataSourceHealth.Source.Espn, _logger);

            foreach (var ev in response.Events)
            {
                if (Classify(ev) != wantedStatus) continue;
                if (ev.Id == null || !seenIds.Add(ev.Id)) continue; // already collected from an overlapping window
                var match = MapEventToMatch(ev, info);
                if (match != null) results.Add(match);
            }
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "getMatches window failed for {Code} [{Range}]", info.Code, dateRange);
            DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"getMatches window exception: {ex.Message}", _logger);
        }
    }

    private FootballMatch? MapEventToMatch(EspnScoreboardResponse.EspnEvent ev, CompetitionMap.Info info)
    {
        if (ev.Competitions == null || ev.Competitions.Count == 0) return null;
        var comp = ev.Competitions[0];
        if (comp.Competitors == null) return null;

        var home = comp.Competitors.FirstOrDefault(c => c.HomeAway == "home");
        var away = comp.Competitors.FirstOrDefault(c => c.HomeAway == "away");
        if (home == null || away == null) return null;

        var match = new FootballMatch
        {
            Area = new Area { Name = info.AreaName },
            Competition = new Competition { Name = info.Name, Code = info.Code },
            Id = SafeParseInt(ev.Id),
            UtcDate = ev.Date,
            Venue = comp.Venue?.FullName,
            HomeTeam = ToTeam(home.Team),
            AwayTeam = ToTeam(away.Team),
        };
        var status = Classify(ev);
        match.Status = ToFdStatus(status, ev.Status?.Type?.Detail);

        match.Score = new Score
        {
            Duration = "REGULAR",
            FullTime = new FullTime { Home = SafeParseInt(home.Score), Away = SafeParseInt(away.Score) },
            HalfTime = new HalfTime(),
            Winner = home.Winner == true ? "HOME_TEAM" : away.Winner == true ? "AWAY_TEAM" : null,
            PenaltyHome = home.ShootoutScore,
            PenaltyAway = away.ShootoutScore,
        };

        return match;
    }

    private static Team ToTeam(EspnTeam? espnTeam)
    {
        if (espnTeam == null) return new Team { Name = "Unknown" };
        return new Team
        {
            Id = SafeParseInt(espnTeam.Id),
            Name = espnTeam.BestName() ?? "Unknown",
            ShortName = espnTeam.ShortDisplayName,
            Tla = espnTeam.Abbreviation,
            Crest = espnTeam.BestLogoUrl(),
        };
    }

    /// <summary>
    /// Looks a specific match up by id. Deliberately narrower than GetMatchesAsync's full
    /// multi-week paging - a match worth looking up is almost always live, just finished, or
    /// about to kick off.
    /// </summary>
    public async Task<FootballMatch?> FindMatchByIdAsync(CompetitionMap.Info info, string matchId)
    {
        var live = await FindInWindowAsync(info, null, matchId);
        if (live != null) return live;

        int weeks = Math.Max(WeeksBack, WeeksForward);
        foreach (var direction in new[] { 1, -1 })
        {
            for (int i = 0; i < weeks; i++)
            {
                if (!DataSourceHealth.IsAvailable(DataSourceHealth.Source.Espn, _logger)) return null;
                var boundaryA = DateTime.UtcNow.AddDays(direction * i * 7);
                var boundaryB = boundaryA.AddDays(direction * 6);
                var (start, end) = direction == 1 ? (boundaryA, boundaryB) : (boundaryB, boundaryA);
                var range = $"{start:yyyyMMdd}-{end:yyyyMMdd}";
                var found = await FindInWindowAsync(info, range, matchId);
                if (found != null) return found;
            }
        }
        return null;
    }

    private async Task<FootballMatch?> FindInWindowAsync(CompetitionMap.Info info, string? dateRange, string matchId)
    {
        try
        {
            var url = $"{SiteBase}/{info.EspnSlug}/scoreboard" + (dateRange != null ? $"?dates={dateRange}" : "");
            var raw = await _dog.FetchAsync(url);
            if (raw == null)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"network fetch returned null for {url}", _logger);
                return null;
            }

            var response = JsonSerializer.Deserialize<EspnScoreboardResponse>(raw, EspnJsonUtil.CaseInsensitive);
            if (response?.Events == null) return null;
            DataSourceHealth.RecordSuccess(DataSourceHealth.Source.Espn, _logger);

            var ev = response.Events.FirstOrDefault(e => e.Id == matchId);
            return ev != null ? MapEventToMatch(ev, info) : null;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "findMatchById failed for {Code} [{Range}]", info.Code, dateRange);
            DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"findMatchById exception: {ex.Message}", _logger);
            return null;
        }
    }

    public async Task<StandingsResponse?> GetStandingsAsync(CompetitionMap.Info info)
    {
        try
        {
            var url = $"{StandingsBase}/{info.EspnSlug}/standings";
            var raw = await _dog.FetchAsync(url);
            if (raw == null)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"network fetch returned null for {url}", _logger);
                return null;
            }

            var response = JsonSerializer.Deserialize<EspnStandingsResponse>(raw, EspnJsonUtil.CaseInsensitive);
            if (response?.Children == null || response.Children.Count == 0) return null;

            // ESPN returns one "children" entry per group/table (e.g. "Group A".."Group H" for a
            // World Cup) - a normal single-table league just has one child, handled uniformly.
            var stages = new List<StandingsItem>();
            foreach (var child in response.Children)
            {
                var block = child.Standings;
                if (block?.Entries == null || block.Entries.Count == 0) continue;

                var table = block.Entries.Select(entry => new TableItem
                {
                    Position = (int)entry.Stat("rank"),
                    Team = ToTeam(entry.Team),
                    PlayedGames = (int)entry.Stat("gamesPlayed"),
                    Won = (int)entry.Stat("wins"),
                    Draw = (int)entry.Stat("ties"),
                    Lost = (int)entry.Stat("losses"),
                    Points = (int)entry.Stat("points"),
                    GoalsFor = (int)entry.Stat("pointsFor"),
                    GoalsAgainst = (int)entry.Stat("pointsAgainst"),
                    GoalDifference = (int)entry.Stat("pointDifferential"),
                }).ToList();

                stages.Add(new StandingsItem
                {
                    Stage = "REGULAR_SEASON",
                    Type = "TOTAL",
                    Group = child.Name ?? child.Abbreviation,
                    Table = table,
                });
            }
            if (stages.Count == 0) return null;

            DataSourceHealth.RecordSuccess(DataSourceHealth.Source.Espn, _logger);
            return new StandingsResponse
            {
                Area = new Area { Name = info.AreaName },
                Competition = new Competition { Name = info.Name, Code = info.Code },
                Standings = stages,
            };
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "getStandings failed for {Code}", info.Code);
            DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"getStandings exception: {ex.Message}", _logger);
            return null;
        }
    }

    /// <summary>
    /// Top goal scorers for a competition's season, from ESPN's "core" API - a separate
    /// hypermedia-style host from the one used everywhere else. Each leader entry only
    /// carries "athlete"/"team": {"$ref": "..."} links rather than embedded data, so each
    /// needs a follow-up fetch to resolve. Capped at `limit` follow-ups per screen.
    /// </summary>
    public async Task<List<Scorer>> GetTopScorersAsync(CompetitionMap.Info info, int limit)
    {
        int endYear = CurrentSeasonEndYear();
        var scorers = await FetchLeadersAsync(info, endYear, limit);
        if (scorers.Count == 0) scorers = await FetchLeadersAsync(info, endYear - 1, limit);
        return scorers;
    }

    private static int CurrentSeasonEndYear()
    {
        var now = DateTime.UtcNow;
        // Before a new season typically kicks off (~August), the most relevant "current"
        // season is the one that just finished in the spring, still labelled with this year.
        return now.Month >= 8 ? now.Year + 1 : now.Year;
    }

    private async Task<List<Scorer>> FetchLeadersAsync(CompetitionMap.Info info, int seasonEndYear, int limit)
    {
        var scorers = new List<Scorer>();
        try
        {
            var url = $"{CoreBase}/{info.EspnSlug}/seasons/{seasonEndYear}/types/1/leaders";
            var raw = await _dog.FetchAsync(url);
            if (raw == null)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"network fetch returned null for {url}", _logger);
                return scorers;
            }

            using var doc = JsonDocument.Parse(raw);
            var root = doc.RootElement;
            if (!root.TryGetProperty("categories", out var categories) || categories.ValueKind != JsonValueKind.Array) return scorers;

            JsonElement? goalsCategory = null;
            foreach (var cat in categories.EnumerateArray())
            {
                if (EspnJsonUtil.StringField(cat, "name", "") == "goalsLeaders") { goalsCategory = cat; break; }
            }
            if (goalsCategory == null || !goalsCategory.Value.TryGetProperty("leaders", out var leaders) || leaders.ValueKind != JsonValueKind.Array)
                return scorers;

            foreach (var leader in leaders.EnumerateArray())
            {
                if (scorers.Count >= limit) break;
                var athleteRef = EspnJsonUtil.Prop(leader, "athlete");
                if (athleteRef == null || EspnJsonUtil.StringField(athleteRef, "$ref", null) == null) continue;

                var athleteUrl = EspnJsonUtil.StringField(athleteRef, "$ref", null)!;
                var athlete = await FetchRefAsync(athleteUrl);
                if (athlete == null) continue;

                var scorer = new Scorer
                {
                    Player = new Player
                    {
                        Id = (int)EspnJsonUtil.NumberField(athlete, "id", 0),
                        Name = EspnJsonUtil.StringField(athlete, "displayName", "Unknown"),
                    },
                    Team = new Team(),
                };
                // team.name was never populated on the Java client either, at first - the
                // "athlete" $ref was resolved but the sibling "team" $ref was silently dropped,
                // which broke My Teams' scorer filtering (matches on this field). Ported here
                // with that fix already applied: resolve "team" too.
                var teamRef = EspnJsonUtil.Prop(leader, "team");
                if (teamRef != null && EspnJsonUtil.StringField(teamRef, "$ref", null) is { } teamUrl)
                {
                    var team = await FetchRefAsync(teamUrl);
                    scorer.Team.Name = EspnJsonUtil.StringField(team, "displayName", null);
                }
                scorer.Goals = (int)EspnJsonUtil.NumberField(leader, "value", 0);
                scorers.Add(scorer);
            }
            DataSourceHealth.RecordSuccess(DataSourceHealth.Source.Espn, _logger);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "getTopScorers failed for {Code} season {Year}", info.Code, seasonEndYear);
            DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"getTopScorers exception: {ex.Message}", _logger);
        }
        return scorers;
    }

    private async Task<JsonElement?> FetchRefAsync(string url)
    {
        try
        {
            var raw = await _dog.FetchAsync(url);
            if (raw == null)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"network fetch returned null for {url}", _logger);
                return null;
            }
            using var doc = JsonDocument.Parse(raw);
            return doc.RootElement.Clone();
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "fetchRef failed for {Url}", url);
            return null;
        }
    }

    /// <summary>Mutates match.HomeTeam/AwayTeam in place, adding lineup/bench/statistics if a summary is available.</summary>
    public async Task<bool> EnrichWithLineupAndStatsAsync(CompetitionMap.Info info, string matchId, FootballMatch match)
    {
        try
        {
            var url = $"{SiteBase}/{info.EspnSlug}/summary?event={matchId}";
            var raw = await _dog.FetchAsync(url);
            if (raw == null)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"network fetch returned null for {url}", _logger);
                return false;
            }

            using var doc = JsonDocument.Parse(raw);
            var root = doc.RootElement;

            bool HasRosterShape(JsonElement item) =>
                item.TryGetProperty("roster", out var r) && r.ValueKind == JsonValueKind.Array
                && item.TryGetProperty("homeAway", out var ha) && (ha.GetString() == "home" || ha.GetString() == "away");
            bool HasStatsShape(JsonElement item) =>
                item.TryGetProperty("statistics", out var s) && s.ValueKind == JsonValueKind.Array
                && item.TryGetProperty("homeAway", out var ha) && (ha.GetString() == "home" || ha.GetString() == "away");

            var rosterGroups = EspnJsonUtil.FindArrayByShape(root, HasRosterShape, 6);
            var statGroups = EspnJsonUtil.FindArrayByShape(root, HasStatsShape, 6);

            var homeRoster = FindByHomeAway(rosterGroups, "home", "roster");
            var awayRoster = FindByHomeAway(rosterGroups, "away", "roster");
            var homeStats = FindByHomeAway(statGroups, "home", "statistics");
            var awayStats = FindByHomeAway(statGroups, "away", "statistics");

            bool homeEmpty = (homeRoster == null || homeRoster.Value.GetArrayLength() == 0);
            bool homeStatsEmpty = (homeStats == null || homeStats.Value.GetArrayLength() == 0);
            if (homeEmpty && homeStatsEmpty) return false;

            ApplyLineup(match.HomeTeam!, homeRoster);
            ApplyLineup(match.AwayTeam!, awayRoster);
            match.HomeTeam!.Statistics = ExtractStats(homeStats);
            match.AwayTeam!.Statistics = ExtractStats(awayStats);
            DataSourceHealth.RecordSuccess(DataSourceHealth.Source.Espn, _logger);
            return true;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "enrichWithLineupAndStats failed for match {MatchId}", matchId);
            DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"enrichWithLineupAndStats exception: {ex.Message}", _logger);
            return false;
        }
    }

    /// <summary>
    /// Goal/card/substitution timeline for a match, from the same summary endpoint used for
    /// lineups/stats/head-to-head - "keyEvents" (only present once a match has kicked off).
    /// </summary>
    public async Task<List<Comment>> GetCommentaryAsync(CompetitionMap.Info info, string eventId)
    {
        var events = new List<Comment>();
        try
        {
            var url = $"{SiteBase}/{info.EspnSlug}/summary?event={eventId}";
            var raw = await _dog.FetchAsync(url);
            if (raw == null)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"network fetch returned null for {url}", _logger);
                return events;
            }

            using var doc = JsonDocument.Parse(raw);
            var root = doc.RootElement;
            if (!root.TryGetProperty("keyEvents", out var keyEvents) || keyEvents.ValueKind != JsonValueKind.Array)
            {
                DataSourceHealth.RecordSuccess(DataSourceHealth.Source.Espn, _logger); // valid response, just no events yet
                return events;
            }

            foreach (var ev in keyEvents.EnumerateArray())
            {
                var clock = EspnJsonUtil.Prop(ev, "clock");
                var minute = clock != null ? EspnJsonUtil.StringField(clock, "displayValue", "") : "";
                var typeObj = EspnJsonUtil.Prop(ev, "type");
                var typeText = typeObj != null ? EspnJsonUtil.StringField(typeObj, "text", "") : "";
                var text = EspnJsonUtil.StringField(ev, "text", "");
                var combined = (!string.IsNullOrEmpty(typeText) ? typeText + ": " : "") + (text ?? "");
                events.Add(new Comment(minute, combined));
            }
            // ESPN returns keyEvents chronologically (kickoff first) - live-commentary convention
            // is newest first, so the most recent event is what's on screen without scrolling.
            events.Reverse();
            DataSourceHealth.RecordSuccess(DataSourceHealth.Source.Espn, _logger);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "getCommentary failed for event {EventId}", eventId);
            DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"getCommentary exception: {ex.Message}", _logger);
        }
        return events;
    }

    /// <summary>
    /// Head-to-head history and each team's recent form, from the same summary endpoint used
    /// for lineups/stats. "headToHeadGames" carries the same match list from each team's
    /// perspective, so only one side is needed; "form" is per-team recent results, matched to
    /// home/away by team name since ESPN doesn't order it home-first/away-second.
    /// </summary>
    public async Task<HeadToHeadSummary?> GetHeadToHeadAndFormAsync(CompetitionMap.Info info, string eventId, string homeTeamName, string awayTeamName)
    {
        try
        {
            var url = $"{SiteBase}/{info.EspnSlug}/summary?event={eventId}";
            var raw = await _dog.FetchAsync(url);
            if (raw == null)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"network fetch returned null for {url}", _logger);
                return null;
            }

            using var doc = JsonDocument.Parse(raw);
            var root = doc.RootElement;
            var summary = new HeadToHeadSummary();

            if (root.TryGetProperty("headToHeadGames", out var h2h) && h2h.ValueKind == JsonValueKind.Array && h2h.GetArrayLength() > 0)
            {
                summary.HeadToHead = ExtractEvents(h2h[0]);
            }

            if (root.TryGetProperty("form", out var form) && form.ValueKind == JsonValueKind.Array)
            {
                foreach (var teamEntry in form.EnumerateArray())
                {
                    var teamObj = EspnJsonUtil.Prop(teamEntry, "team");
                    var teamName = teamObj != null ? EspnJsonUtil.StringField(teamObj, "displayName", "") : "";
                    var games = ExtractEvents(teamEntry);
                    if (NamesLikelyMatch(teamName, homeTeamName)) summary.HomeForm = games;
                    else if (NamesLikelyMatch(teamName, awayTeamName)) summary.AwayForm = games;
                }
            }

            if (summary.HeadToHead == null && summary.HomeForm == null && summary.AwayForm == null) return null;
            DataSourceHealth.RecordSuccess(DataSourceHealth.Source.Espn, _logger);
            return summary;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "getHeadToHeadAndForm failed for event {EventId}", eventId);
            DataSourceHealth.RecordFailure(DataSourceHealth.Source.Espn, $"getHeadToHeadAndForm exception: {ex.Message}", _logger);
            return null;
        }
    }

    private static List<GameResult> ExtractEvents(JsonElement teamEntry)
    {
        var games = new List<GameResult>();
        if (!teamEntry.TryGetProperty("events", out var events) || events.ValueKind != JsonValueKind.Array) return games;
        foreach (var ev in events.EnumerateArray())
        {
            var opponent = EspnJsonUtil.Prop(ev, "opponent");
            games.Add(new GameResult
            {
                Date = EspnJsonUtil.StringField(ev, "gameDate", ""),
                ScoreLine = EspnJsonUtil.StringField(ev, "score", ""),
                Result = EspnJsonUtil.StringField(ev, "gameResult", ""),
                CompetitionName = EspnJsonUtil.StringField(ev, "competitionName", ""),
                OpponentName = EspnJsonUtil.StringField(opponent, "displayName", "Unknown"),
            });
        }
        return games;
    }

    private static bool NamesLikelyMatch(string? a, string? b)
    {
        if (a == null || b == null) return false;
        var na = Normalize(a);
        var nb = Normalize(b);
        if (na.Length == 0 || nb.Length == 0) return false;
        return na == nb || na.Contains(nb) || nb.Contains(na);
    }

    private static string Normalize(string s) => System.Text.RegularExpressions.Regex.Replace(s.ToLowerInvariant(), "[^a-z0-9]", "");

    private static JsonElement? FindByHomeAway(JsonElement? groups, string homeAway, string childArrayField)
    {
        if (groups == null) return null;
        foreach (var el in groups.Value.EnumerateArray())
        {
            if (el.TryGetProperty("homeAway", out var ha) && ha.GetString() == homeAway && el.TryGetProperty(childArrayField, out var child))
                return child;
        }
        return null;
    }

    private static void ApplyLineup(Team team, JsonElement? roster)
    {
        if (roster == null) return;
        var starters = new List<Player>();
        var bench = new List<Player>();
        foreach (var entry in roster.Value.EnumerateArray())
        {
            var athlete = EspnJsonUtil.Prop(entry, "athlete");
            var player = new Player
            {
                Name = EspnJsonUtil.StringField(athlete, "displayName", "Unknown"),
            };
            player.ShortName = EspnJsonUtil.StringField(athlete, "shortName", player.Name);
            player.Id = (int)EspnJsonUtil.NumberField(athlete, "id", 0);
            var position = EspnJsonUtil.Prop(entry, "position");
            player.Position = EspnJsonUtil.StringField(position, "abbreviation", null);
            player.ShirtNumber = (int)EspnJsonUtil.NumberField(entry, "jersey", 0);
            var headshot = athlete != null ? EspnJsonUtil.Prop(athlete.Value, "headshot") : null;
            player.PhotoUrl = EspnJsonUtil.StringField(headshot, "href", null);

            bool starter = entry.TryGetProperty("starter", out var starterEl) && starterEl.ValueKind == JsonValueKind.True;
            if (starter) { player.Section = "Starting XI"; starters.Add(player); }
            else { player.Section = "Substitute"; bench.Add(player); }
        }
        team.Lineup = starters;
        team.Bench = bench;
    }

    private static Statistics ExtractStats(JsonElement? statItems)
    {
        var stats = new Statistics();
        if (statItems == null) return stats;
        stats.CornerKicks = (int)StatByName(statItems.Value, "wonCorners");
        stats.Offsides = (int)StatByName(statItems.Value, "offsides");
        stats.Fouls = (int)StatByName(statItems.Value, "foulsCommitted");
        stats.BallPossession = (int)StatByName(statItems.Value, "possessionPct");
        stats.Saves = (int)StatByName(statItems.Value, "saves");
        stats.Shots = (int)StatByName(statItems.Value, "totalShots");
        stats.ShotsOnGoal = (int)StatByName(statItems.Value, "shotsOnTarget");
        stats.YellowCards = (int)StatByName(statItems.Value, "yellowCards");
        stats.RedCards = (int)StatByName(statItems.Value, "redCards");
        return stats;
    }

    private static double StatByName(JsonElement statItems, string name)
    {
        foreach (var el in statItems.EnumerateArray())
        {
            if (el.TryGetProperty("name", out var n) && n.GetString() == name)
                return EspnJsonUtil.NumberField(el, "displayValue", 0);
        }
        return 0;
    }

    private static int SafeParseInt(string? value)
    {
        if (value == null) return 0;
        return double.TryParse(value, out var d) ? (int)d : 0;
    }
}
