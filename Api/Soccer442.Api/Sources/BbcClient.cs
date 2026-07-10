using System.Collections.Concurrent;
using System.Text.Json;
using System.Text.RegularExpressions;
using Soccer442.Api.Models;

namespace Soccer442.Api.Sources;

/// <summary>
/// C# port of BbcClient.java - third data source, used only when both ESPN and OpenLigaDB
/// have nothing for a given competition/query. BBC Sport's pages embed a real, structured
/// JSON payload in a "window.__INITIAL_DATA__ = "...";" script tag - a JS string literal
/// containing escaped JSON, so it needs decoding twice (unescape the string literal, then
/// parse the JSON text it contains). Far more stable than scraping rendered HTML: it breaks
/// only if BBC changes their data shape, not their page design.
///
/// Every entry point here fails soft (null/empty) on any problem, same as every other source.
/// </summary>
public class BbcClient
{
    private const string FixturesBase = "https://www.bbc.com/sport/football/scores-fixtures";
    private const string TablesUrl = "https://www.bbc.com/sport/football/tables";
    private const string SiteBase = "https://www.bbc.com";

    private readonly HttpFetcher _dog;
    private readonly ILogger<BbcClient> _logger;

    public BbcClient(HttpFetcher dog, ILogger<BbcClient> logger)
    {
        _dog = dog;
        _logger = logger;
    }

    private static readonly Regex InitialData = new(@"window\.__INITIAL_DATA__\s*=\s*""(.*?)"";", RegexOptions.Singleline | RegexOptions.Compiled);

    // BBC's tournament labels don't always match 1:1 - extra tokens tried when matching a
    // competition against a BBC event's grouping label or tournament name.
    private static readonly Dictionary<string, string[]> NameAliases = new()
    {
        ["PD"] = new[] { "la liga", "laliga" },
        ["EC"] = new[] { "euro" },
        ["BSA"] = new[] { "brasileiro", "brazilian" },
        ["CL"] = new[] { "champions league" },
        ["ELC"] = new[] { "championship" },
        ["CLI"] = new[] { "libertadores" },
        ["WC"] = new[] { "world cup" },
    };

    // --- embedded-JSON plumbing -------------------------------------------------

    // Single point every other method funnels through to reach BBC's page data, so it's also
    // the single point that knows whether BBC is actually working right now.
    private async Task<JsonElement?> FetchDataRootAsync(string url)
    {
        try
        {
            var html = await _dog.FetchAsync(url);
            if (html == null)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.Bbc, $"network fetch returned null for {url}", _logger);
                return null;
            }
            var m = InitialData.Match(html);
            if (!m.Success)
            {
                // The page loaded but no longer has the embedded data blob at all - exactly
                // the "BBC changed their page" scenario this client exists to survive.
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.Bbc, $"__INITIAL_DATA__ not found in {url}", _logger);
                return null;
            }

            // m.Groups[1] is a JS string literal's contents (already stripped of its
            // surrounding quotes by the regex) - wrapping it back in quotes and running it
            // through a JSON string parse undoes the JS-string escaping, yielding the real
            // JSON text, which is then parsed a second time to get the actual data.
            string? jsonText;
            try { jsonText = JsonSerializer.Deserialize<string>("\"" + m.Groups[1].Value + "\""); }
            catch { jsonText = null; }
            if (jsonText == null)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.Bbc, $"could not decode data blob for {url}", _logger);
                return null;
            }
            using var doc = JsonDocument.Parse(jsonText);
            if (!doc.RootElement.TryGetProperty("data", out var data) || data.ValueKind != JsonValueKind.Object)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.Bbc, $"decoded blob had no 'data' object for {url}", _logger);
                return null;
            }
            DataSourceHealth.RecordSuccess(DataSourceHealth.Source.Bbc, _logger);
            return data.Clone();
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "fetchDataRoot failed for {Url}", url);
            DataSourceHealth.RecordFailure(DataSourceHealth.Source.Bbc, $"fetchDataRoot exception: {ex.Message}", _logger);
            return null;
        }
    }

    // Every data key on a BBC page is a base name plus a query string of the request params
    // that produced it - those params change on every request, so matching by prefix instead
    // of an exact key is what survives that churn.
    private static JsonElement? FindByKeyPrefix(JsonElement? dataRoot, string prefix)
    {
        if (dataRoot == null) return null;
        foreach (var prop in dataRoot.Value.EnumerateObject())
        {
            if (!prop.Name.StartsWith(prefix)) continue;
            if (prop.Value.ValueKind == JsonValueKind.Object && prop.Value.TryGetProperty("data", out var data) && data.ValueKind == JsonValueKind.Object)
                return data;
        }
        return null;
    }

    private static string Normalize(string? s) => s == null ? "" : Regex.Replace(s.ToLowerInvariant(), "[^a-z0-9]", "");

    private static bool NamesLikelyMatch(string? a, string? b)
    {
        var na = Normalize(a);
        var nb = Normalize(b);
        if (na.Length == 0 || nb.Length == 0) return false;
        return na == nb || na.Contains(nb) || nb.Contains(na);
    }

    private static bool MatchesCompetition(string? label, CompetitionMap.Info info)
    {
        if (label == null) return false;
        var normLabel = Normalize(label);
        var normArea = Normalize(info.AreaName);
        if (normArea.Length > 0 && !normLabel.Contains(normArea)) return false;

        if (normLabel.Contains(Normalize(info.Name))) return true;
        if (NameAliases.TryGetValue(info.Code, out var aliases))
        {
            foreach (var alias in aliases)
                if (normLabel.Contains(Normalize(alias))) return true;
        }
        return false;
    }

    private static int ParseIntSafe(string? s) => int.TryParse(s?.Trim(), out var v) ? v : 0;

    // --- matchId -> raw event lookup --------------------------------------------

    // BBC has no id shared with ESPN, so a match tapped from a BBC-sourced list row is looked
    // up here by the synthetic id assigned in MapEvent() - populated as a side effect of every
    // GetMatchesAsync/CollectDay call. Bounded so long uptime doesn't grow this unboundedly.
    private const int MaxCache = 300;
    private record CachedEvent(JsonElement Event, CompetitionMap.Info Info);
    private static readonly ConcurrentDictionary<int, CachedEvent> EventCache = new();
    private static readonly ConcurrentQueue<int> EventCacheOrder = new();

    private static void CacheEvent(int id, JsonElement ev, CompetitionMap.Info info)
    {
        EventCache[id] = new CachedEvent(ev.Clone(), info);
        EventCacheOrder.Enqueue(id);
        while (EventCacheOrder.Count > MaxCache && EventCacheOrder.TryDequeue(out var oldest))
            EventCache.TryRemove(oldest, out _);
    }

    public FootballMatch? FindMatchById(string matchId)
    {
        try
        {
            if (!int.TryParse(matchId, out var id) || !EventCache.TryGetValue(id, out var cached)) return null;
            return MapEvent(cached.Event, cached.Info);
        }
        catch
        {
            return null;
        }
    }

    // --- fixtures / results / live ---------------------------------------------

    public async Task<List<FootballMatch>> GetMatchesAsync(CompetitionMap.Info info, MatchStatus wantedStatus)
    {
        var results = new List<FootballMatch>();
        int daysBack = wantedStatus == MatchStatus.Finished ? 6 : 0;
        int daysForward = wantedStatus == MatchStatus.Scheduled ? 6 : 0;

        var day = DateTime.UtcNow.AddDays(-daysBack);
        int totalDays = daysBack + daysForward + 1;

        for (int i = 0; i < totalDays; i++)
        {
            await CollectDayAsync(day.ToString("yyyy-MM-dd"), info, wantedStatus, results);
            day = day.AddDays(1);
        }
        return results;
    }

    private async Task CollectDayAsync(string isoDate, CompetitionMap.Info info, MatchStatus wantedStatus, List<FootballMatch> outList)
    {
        try
        {
            var root = await FetchDataRootAsync($"{FixturesBase}/{isoDate}");
            var fixtures = FindByKeyPrefix(root, "sport-data-scores-fixtures");
            if (fixtures == null || !fixtures.Value.TryGetProperty("eventGroups", out var eventGroups)) return;

            foreach (var eg in eventGroups.EnumerateArray())
            {
                if (!eg.TryGetProperty("secondaryGroups", out var secondaryGroups)) continue;
                foreach (var sg in secondaryGroups.EnumerateArray())
                {
                    if (!sg.TryGetProperty("events", out var events)) continue;
                    foreach (var ev in events.EnumerateArray())
                    {
                        var label = EspnJsonUtil.StringField(ev, "eventGroupingLabel", null) ?? EspnJsonUtil.StringField(eg, "displayLabel", "");
                        if (!MatchesCompetition(label, info)) continue;

                        var match = MapEvent(ev, info);
                        if (match == null) continue;
                        if (Classify(match.Status) != wantedStatus) continue;
                        outList.Add(match);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "collectDay failed for {IsoDate} {Code}", isoDate, info.Code);
        }
    }

    private static MatchStatus Classify(string? fdStatus)
    {
        if (fdStatus == "FINISHED") return MatchStatus.Finished;
        if (fdStatus == "SCHEDULED") return MatchStatus.Scheduled;
        if (fdStatus != null && fdStatus.StartsWith("IN_PLAY")) return MatchStatus.Live;
        if (fdStatus == "PAUSED") return MatchStatus.Live;
        return MatchStatus.Other;
    }

    private static FootballMatch? MapEvent(JsonElement ev, CompetitionMap.Info info)
    {
        try
        {
            var id = EspnJsonUtil.StringField(ev, "id", null);
            if (id == null) return null;
            if (!ev.TryGetProperty("home", out var home) || !ev.TryGetProperty("away", out var away)) return null;

            var match = new FootballMatch
            {
                Area = new Area { Name = info.AreaName },
                Competition = new Competition { Name = info.Name, Code = info.Code },
                // Offset so BBC-sourced ids can't collide with ESPN's or OpenLigaDB's own ids.
                Id = 3_000_000 + Math.Abs(id.GetHashCode() % 1_000_000),
                UtcDate = EspnJsonUtil.StringField(ev, "startDateTime", null),
            };

            var bbcStatus = EspnJsonUtil.StringField(ev, "status", "");
            match.Status = bbcStatus == "PostEvent" ? "FINISHED" : bbcStatus == "MidEvent" ? "IN_PLAY" : "SCHEDULED";

            match.HomeTeam = MapTeamBasic(home);
            match.AwayTeam = MapTeamBasic(away);

            match.Score = new Score
            {
                Duration = "REGULAR",
                FullTime = new FullTime { Home = ParseIntSafe(EspnJsonUtil.StringField(home, "score", null)), Away = ParseIntSafe(EspnJsonUtil.StringField(away, "score", null)) },
                HalfTime = new HalfTime(),
            };

            CacheEvent(match.Id, ev, info);
            return match;
        }
        catch
        {
            return null;
        }
    }

    private static Team MapTeamBasic(JsonElement t)
    {
        var name = EspnJsonUtil.StringField(t, "fullName", "Unknown")!;
        // BBC's fixtures listing doesn't carry a crest URL - the Android app already falls
        // back to a placeholder badge + team-code label when crest is empty.
        return new Team { Name = name, ShortName = EspnJsonUtil.StringField(t, "shortName", name) };
    }

    // --- lineup / stats enrichment ----------------------------------------------

    /// <summary>
    /// Follows the cached event's own page link to pull real lineups and match stats. BBC's
    /// pitchLayout is already grouped top (goalkeeper) to bottom (forwards) in genuine
    /// left-to-right pitch order - unlike ESPN's flat roster, there's no need to infer
    /// left/right here.
    /// </summary>
    public async Task EnrichWithLineupAndStatsAsync(FootballMatch match)
    {
        try
        {
            if (!EventCache.TryGetValue(match.Id, out var cached)) return;
            var link = EspnJsonUtil.StringField(cached.Event, "onwardJourneyLink", null);
            if (link == null) return;

            var root = await FetchDataRootAsync(SiteBase + link);

            var lineups = FindByKeyPrefix(root, "match-lineups");
            if (lineups != null)
            {
                ApplyLineup(match.HomeTeam!, lineups.Value.TryGetProperty("homeTeam", out var h) ? h : null);
                ApplyLineup(match.AwayTeam!, lineups.Value.TryGetProperty("awayTeam", out var a) ? a : null);
            }

            var stats = FindByKeyPrefix(root, "match-stats");
            if (stats != null)
            {
                match.HomeTeam!.Statistics = MapStatistics(stats.Value.TryGetProperty("homeTeam", out var hs) ? hs : null);
                match.AwayTeam!.Statistics = MapStatistics(stats.Value.TryGetProperty("awayTeam", out var aws) ? aws : null);
            }
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "enrichWithLineupAndStats failed for match {MatchId}", match.Id);
        }
    }

    private static readonly Dictionary<string, string> PositionAbbr = new()
    {
        ["goalkeeper"] = "G",
        ["defender"] = "CD",
        ["defensive midfielder"] = "DM",
        ["midfielder"] = "CM",
        ["attacking midfielder"] = "AM",
        ["striker"] = "CF",
        ["forward"] = "CF",
        ["winger"] = "CF",
    };

    private static void ApplyLineup(Team team, JsonElement? teamNode)
    {
        if (teamNode == null || !teamNode.Value.TryGetProperty("players", out var players) || players.ValueKind != JsonValueKind.Object) return;

        var positionByUrn = new Dictionary<string, string>();
        var fullNameByUrn = new Dictionary<string, string>();
        var startersArr = players.TryGetProperty("starters", out var st) ? st : (JsonElement?)null;
        var subsArr = players.TryGetProperty("substitutes", out var sub) ? sub : (JsonElement?)null;
        CollectPositions(startersArr, positionByUrn);
        CollectPositions(subsArr, positionByUrn);
        CollectFullNames(startersArr, fullNameByUrn);
        CollectFullNames(subsArr, fullNameByUrn);

        var starters = new List<Player>();
        if (teamNode.Value.TryGetProperty("pitchLayout", out var pitchLayout) && pitchLayout.ValueKind == JsonValueKind.Array)
        {
            foreach (var row in pitchLayout.EnumerateArray())
            {
                if (row.ValueKind != JsonValueKind.Array) continue;
                // Verified against a real match: each row needs reversing to line up with
                // the renderer's left-to-right expectation (BBC lists RB..LB, backwards).
                var rowPlayers = row.EnumerateArray().Reverse();
                foreach (var p in rowPlayers)
                {
                    // pitchLayout's own "name" is just a bare surname - fine for a compact
                    // chip label, but fullNameByUrn (from the richer starters/substitutes
                    // list) gives a real full name when available.
                    var bareName = EspnJsonUtil.StringField(p, "name", "Unknown")!;
                    var urn = EspnJsonUtil.StringField(p, "urn", "") ?? "";
                    fullNameByUrn.TryGetValue(urn, out var fullName);
                    var player = new Player
                    {
                        ShortName = bareName,
                        Name = fullName ?? bareName,
                        ShirtNumber = (int)EspnJsonUtil.NumberField(p, "shirtNumber", 0),
                    };
                    positionByUrn.TryGetValue(urn, out var category);
                    player.Position = category != null && PositionAbbr.TryGetValue(category, out var abbr) ? abbr : "CM"; // safe mid-table default
                    starters.Add(player);
                }
            }
        }
        if (starters.Count > 0) team.Lineup = starters;

        var bench = new List<Player>();
        if (subsArr != null && subsArr.Value.ValueKind == JsonValueKind.Array)
        {
            foreach (var p in subsArr.Value.EnumerateArray())
            {
                var fullName = FullNameFromEntry(p);
                var displayName = EspnJsonUtil.StringField(p, "displayName", "Unknown")!;
                bench.Add(new Player
                {
                    Name = fullName ?? displayName,
                    ShortName = EspnJsonUtil.StringField(p, "displayName", fullName ?? displayName),
                    ShirtNumber = (int)EspnJsonUtil.NumberField(p, "shirtNumber", 0),
                    Position = EspnJsonUtil.StringField(p, "position", null),
                });
            }
        }
        if (bench.Count > 0) team.Bench = bench;
    }

    private static void CollectPositions(JsonElement? players, Dictionary<string, string> outMap)
    {
        if (players == null) return;
        foreach (var p in players.Value.EnumerateArray())
        {
            var urn = EspnJsonUtil.StringField(p, "urn", null);
            var position = EspnJsonUtil.StringField(p, "position", null);
            if (urn != null && position != null) outMap[urn] = position.ToLowerInvariant();
        }
    }

    private static void CollectFullNames(JsonElement? players, Dictionary<string, string> outMap)
    {
        if (players == null) return;
        foreach (var p in players.Value.EnumerateArray())
        {
            var urn = EspnJsonUtil.StringField(p, "urn", null);
            var fullName = FullNameFromEntry(p);
            if (urn != null && fullName != null) outMap[urn] = fullName;
        }
    }

    // players.starters/substitutes entries carry name.{first,last} - a real full name, unlike
    // pitchLayout's bare surname or the abbreviated displayName.
    private static string? FullNameFromEntry(JsonElement p)
    {
        if (!p.TryGetProperty("name", out var nameObj) || nameObj.ValueKind != JsonValueKind.Object) return null;
        var first = EspnJsonUtil.StringField(nameObj, "first", null);
        var last = EspnJsonUtil.StringField(nameObj, "last", null);
        return first != null && last != null ? $"{first} {last}" : null;
    }

    private static Statistics? MapStatistics(JsonElement? teamNode)
    {
        if (teamNode == null || !teamNode.Value.TryGetProperty("stats", out var s) || s.ValueKind != JsonValueKind.Object) return null;
        return new Statistics
        {
            Shots = (int)StatTotal(s, "shotsTotal"),
            ShotsOnGoal = (int)StatTotal(s, "shotsOnTarget"),
            Fouls = (int)StatTotal(s, "foulsCommitted"),
            CornerKicks = (int)StatTotal(s, "cornersWon"),
            BallPossession = (int)StatTotal(s, "possessionPercentage"),
            // BBC's match-stats endpoint doesn't expose offsides or card counts - those stay
            // 0 rather than guessing.
        };
    }

    private static double StatTotal(JsonElement stats, string field)
    {
        if (!stats.TryGetProperty(field, out var f) || f.ValueKind != JsonValueKind.Object) return 0;
        return EspnJsonUtil.NumberField(f, "total", 0);
    }

    // --- standings ----------------------------------------------------------

    public async Task<StandingsResponse?> GetStandingsAsync(CompetitionMap.Info info)
    {
        try
        {
            var root = await FetchDataRootAsync(TablesUrl);
            var tablesData = FindByKeyPrefix(root, "football-table");
            if (tablesData == null || !tablesData.Value.TryGetProperty("tournaments", out var tournaments)) return null;

            JsonElement? tournament = null;
            foreach (var t in tournaments.EnumerateArray())
            {
                var name = EspnJsonUtil.StringField(t, "disambiguatedName", EspnJsonUtil.StringField(t, "name", ""));
                if (MatchesCompetition(info.AreaName + " " + name, info) || NamesLikelyMatch(name, info.Name)) { tournament = t; break; }
            }
            if (tournament == null || !tournament.Value.TryGetProperty("stages", out var stagesEl)) return null;

            var stages = new List<StandingsItem>();
            foreach (var stage in stagesEl.EnumerateArray())
            {
                if (!stage.TryGetProperty("rounds", out var rounds)) continue;
                foreach (var round in rounds.EnumerateArray())
                {
                    if (!round.TryGetProperty("participants", out var participants)) continue;

                    var table = new List<TableItem>();
                    foreach (var p in participants.EnumerateArray())
                    {
                        table.Add(new TableItem
                        {
                            Position = (int)EspnJsonUtil.NumberField(p, "rank", 0),
                            Team = new Team { Name = EspnJsonUtil.StringField(p, "name", EspnJsonUtil.StringField(p, "shortName", "Unknown")) },
                            PlayedGames = (int)EspnJsonUtil.NumberField(p, "matchesPlayed", 0),
                            Won = (int)EspnJsonUtil.NumberField(p, "wins", 0),
                            Draw = (int)EspnJsonUtil.NumberField(p, "draws", 0),
                            Lost = (int)EspnJsonUtil.NumberField(p, "losses", 0),
                            Points = (int)EspnJsonUtil.NumberField(p, "points", 0),
                            GoalsFor = (int)EspnJsonUtil.NumberField(p, "goalsScoredFor", 0),
                            GoalsAgainst = (int)EspnJsonUtil.NumberField(p, "goalsScoredAgainst", 0),
                            GoalDifference = (int)EspnJsonUtil.NumberField(p, "goalDifference", 0),
                        });
                    }
                    if (table.Count == 0) continue;

                    stages.Add(new StandingsItem { Stage = "REGULAR_SEASON", Type = "TOTAL", Group = EspnJsonUtil.StringField(round, "name", null), Table = table });
                }
            }
            if (stages.Count == 0) return null;

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
            return null;
        }
    }

    // --- head-to-head / form --------------------------------------------------

    /// <summary>Locates the match by team names on its own date (BBC has no id shared with
    /// ESPN), then follows its page link to pull real previous-meetings and recent-form data.</summary>
    public async Task<HeadToHeadSummary?> GetHeadToHeadAndFormAsync(string homeTeamName, string awayTeamName, string? utcDate)
    {
        try
        {
            var isoDate = ToIsoDate(utcDate);
            if (isoDate == null) return null;

            var matchLink = await FindMatchLinkAsync(isoDate, homeTeamName, awayTeamName);
            // BBC's date-for-an-event can land a day off ESPN's UTC date depending on kickoff
            // time near midnight - try the adjacent days before giving up.
            matchLink ??= await FindMatchLinkAsync(ShiftDate(isoDate, -1), homeTeamName, awayTeamName);
            matchLink ??= await FindMatchLinkAsync(ShiftDate(isoDate, 1), homeTeamName, awayTeamName);
            if (matchLink == null) return null;

            var root = await FetchDataRootAsync(SiteBase + matchLink);
            var preview = FindByKeyPrefix(root, "football-match-preview");
            if (preview == null) return null;

            var summary = new HeadToHeadSummary
            {
                HeadToHead = ExtractPreviousMeetings(preview.Value, homeTeamName),
            };

            var homeTeam = preview.Value.TryGetProperty("homeTeam", out var ht) ? ht : (JsonElement?)null;
            var awayTeam = preview.Value.TryGetProperty("awayTeam", out var at) ? at : (JsonElement?)null;
            summary.HomeForm = ExtractFormGuide(homeTeam);
            summary.AwayForm = ExtractFormGuide(awayTeam);

            if (summary.HeadToHead == null && summary.HomeForm == null && summary.AwayForm == null) return null;
            return summary;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "getHeadToHeadAndForm failed for {Home} vs {Away}", homeTeamName, awayTeamName);
            return null;
        }
    }

    private async Task<string?> FindMatchLinkAsync(string? isoDate, string homeTeamName, string awayTeamName)
    {
        if (isoDate == null) return null;
        var root = await FetchDataRootAsync($"{FixturesBase}/{isoDate}");
        var fixtures = FindByKeyPrefix(root, "sport-data-scores-fixtures");
        if (fixtures == null || !fixtures.Value.TryGetProperty("eventGroups", out var eventGroups)) return null;

        foreach (var eg in eventGroups.EnumerateArray())
        {
            if (!eg.TryGetProperty("secondaryGroups", out var secondaryGroups)) continue;
            foreach (var sg in secondaryGroups.EnumerateArray())
            {
                if (!sg.TryGetProperty("events", out var events)) continue;
                foreach (var ev in events.EnumerateArray())
                {
                    if (!ev.TryGetProperty("home", out var home) || !ev.TryGetProperty("away", out var away)) continue;
                    var h = EspnJsonUtil.StringField(home, "fullName", "");
                    var a = EspnJsonUtil.StringField(away, "fullName", "");
                    bool straight = NamesLikelyMatch(h, homeTeamName) && NamesLikelyMatch(a, awayTeamName);
                    bool swapped = NamesLikelyMatch(h, awayTeamName) && NamesLikelyMatch(a, homeTeamName);
                    if (straight || swapped) return EspnJsonUtil.StringField(ev, "onwardJourneyLink", null);
                }
            }
        }
        return null;
    }

    private static List<GameResult>? ExtractPreviousMeetings(JsonElement preview, string perspectiveTeamName)
    {
        if (!preview.TryGetProperty("previousScores", out var previousScores) || previousScores.ValueKind != JsonValueKind.Array) return null;
        var games = new List<GameResult>();
        foreach (var tournamentBlock in previousScores.EnumerateArray())
        {
            if (!tournamentBlock.TryGetProperty("matches", out var matches)) continue;
            var tournamentName = tournamentBlock.TryGetProperty("tournament", out var t) ? EspnJsonUtil.StringField(t, "name", "") : "";
            foreach (var match in matches.EnumerateArray())
            {
                var game = ToGameResult(match, perspectiveTeamName, tournamentName);
                if (game != null) games.Add(game);
            }
        }
        return games.Count == 0 ? null : games;
    }

    private static List<GameResult>? ExtractFormGuide(JsonElement? team)
    {
        if (team == null || !team.Value.TryGetProperty("formGuide", out var formGuide) || formGuide.ValueKind != JsonValueKind.Array) return null;
        // "name" is a nested {fullName,shortName,code} object here (preview.homeTeam/awayTeam),
        // unlike the plain string "fullName" field used everywhere else in BBC's payloads.
        string? teamName = null;
        if (team.Value.TryGetProperty("name", out var nameObj) && nameObj.ValueKind == JsonValueKind.Object)
            teamName = EspnJsonUtil.StringField(nameObj, "fullName", null);

        var games = new List<GameResult>();
        foreach (var entry in formGuide.EnumerateArray())
        {
            var tournamentName = entry.TryGetProperty("tournament", out var t) ? EspnJsonUtil.StringField(t, "name", "") : "";
            var game = ToGameResultFromHomeAway(entry, teamName, tournamentName);
            if (game != null) games.Add(game);
        }
        return games.Count == 0 ? null : games;
    }

    private static GameResult? ToGameResult(JsonElement match, string perspectiveTeamName, string? tournamentName)
    {
        if (!match.TryGetProperty("participants", out var participants)) return null;
        return ToGameResultFromHomeAway(participants, perspectiveTeamName, tournamentName, EspnJsonUtil.StringField(match, "date", ""));
    }

    private static GameResult? ToGameResultFromHomeAway(JsonElement entry, string? perspectiveTeamName, string? tournamentName)
        => ToGameResultFromHomeAway(entry, perspectiveTeamName, tournamentName, EspnJsonUtil.StringField(entry, "date", ""));

    private static GameResult? ToGameResultFromHomeAway(JsonElement entry, string? perspectiveTeamName, string? tournamentName, string? date)
    {
        if (!entry.TryGetProperty("home", out var home) || !entry.TryGetProperty("away", out var away)) return null;
        var homeName = EspnJsonUtil.StringField(home, "fullName", "");
        var awayName = EspnJsonUtil.StringField(away, "fullName", "");
        int homeScore = ParseIntSafe(EspnJsonUtil.StringField(home, "score", null));
        int awayScore = ParseIntSafe(EspnJsonUtil.StringField(away, "score", null));

        bool perspectiveIsHome = NamesLikelyMatch(homeName, perspectiveTeamName);
        bool perspectiveIsAway = !perspectiveIsHome && NamesLikelyMatch(awayName, perspectiveTeamName);
        if (!perspectiveIsHome && !perspectiveIsAway) return null;

        var opponent = perspectiveIsHome ? awayName : homeName;
        int mine = perspectiveIsHome ? homeScore : awayScore;
        int theirs = perspectiveIsHome ? awayScore : homeScore;

        return new GameResult
        {
            Date = date,
            ScoreLine = $"{homeScore}-{awayScore}",
            Result = mine > theirs ? "W" : (mine < theirs ? "L" : "D"),
            OpponentName = opponent,
            CompetitionName = tournamentName,
        };
    }

    private static string? ToIsoDate(string? utcDate)
    {
        if (utcDate == null) return null;
        string[] patterns = { "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm'Z'", "yyyy-MM-dd" };
        foreach (var p in patterns)
        {
            if (DateTime.TryParseExact(utcDate, p, System.Globalization.CultureInfo.InvariantCulture,
                    System.Globalization.DateTimeStyles.AssumeUniversal | System.Globalization.DateTimeStyles.AdjustToUniversal, out var parsed))
                return parsed.ToString("yyyy-MM-dd");
        }
        return null;
    }

    private static string ShiftDate(string isoDate, int days)
    {
        try { return DateTime.Parse(isoDate).AddDays(days).ToString("yyyy-MM-dd"); }
        catch { return isoDate; }
    }
}
