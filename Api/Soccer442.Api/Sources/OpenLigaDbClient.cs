using System.Text.Json;
using Soccer442.Api.Models;

namespace Soccer442.Api.Sources;

/// <summary>
/// C# port of OpenLigaDbClient.java - fallback source used only when ESPN has no data for a
/// competition (mainly German leagues, where OpenLigaDB is well-maintained). Genuinely free,
/// key-less JSON API - api.openligadb.de.
/// </summary>
public class OpenLigaDbClient
{
    private const string Base = "https://api.openligadb.de";

    private readonly HttpFetcher _dog;
    private readonly ILogger<OpenLigaDbClient> _logger;

    public OpenLigaDbClient(HttpFetcher dog, ILogger<OpenLigaDbClient> logger)
    {
        _dog = dog;
        _logger = logger;
    }

    private static int CurrentSeasonYear()
    {
        var now = DateTime.UtcNow;
        return now.Month >= 7 ? now.Year : now.Year - 1;
    }

    public async Task<List<FootballMatch>> GetMatchesAsync(CompetitionMap.Info info)
    {
        var results = new List<FootballMatch>();
        if (info.OpenLigaDbShortcut == null) return results;

        try
        {
            var url = $"{Base}/getmatchdata/{info.OpenLigaDbShortcut}/{CurrentSeasonYear()}";
            var raw = await _dog.FetchAsync(url);
            if (raw == null)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.OpenLigaDb, $"network fetch returned null for {url}", _logger);
                return results;
            }

            var matches = JsonSerializer.Deserialize<List<OldbMatch>>(raw, EspnJsonUtil.CaseInsensitive);
            if (matches == null) return results;

            results.AddRange(matches.Select(m => Map(m, info)));
            DataSourceHealth.RecordSuccess(DataSourceHealth.Source.OpenLigaDb, _logger);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "getMatches failed for {Code}", info.Code);
            DataSourceHealth.RecordFailure(DataSourceHealth.Source.OpenLigaDb, $"getMatches exception: {ex.Message}", _logger);
        }
        return results;
    }

    private static FootballMatch Map(OldbMatch m, CompetitionMap.Info info)
    {
        var match = new FootballMatch
        {
            Area = new Area { Name = info.AreaName },
            Competition = new Competition { Name = info.Name, Code = info.Code },
            Id = 1_000_000 + m.MatchID, // offset so OpenLigaDB ids can't collide with ESPN event ids
            UtcDate = m.MatchDateTimeUTC,
            Status = m.MatchIsFinished ? "FINISHED" : "SCHEDULED",
            Venue = m.Location?.LocationStadium,
            HomeTeam = new Team { Name = m.Team1?.TeamName ?? "Unknown", Crest = m.Team1?.TeamIconUrl },
            AwayTeam = new Team { Name = m.Team2?.TeamName ?? "Unknown", Crest = m.Team2?.TeamIconUrl },
        };

        var score = new Score { Duration = "REGULAR", FullTime = new FullTime(), HalfTime = new HalfTime() };
        var finalResult = m.MatchResults?.FirstOrDefault(r => r.ResultTypeID == 2);
        if (finalResult != null)
        {
            score.FullTime.Home = finalResult.PointsTeam1;
            score.FullTime.Away = finalResult.PointsTeam2;
        }
        match.Score = score;
        return match;
    }

    public async Task<StandingsResponse?> GetStandingsAsync(CompetitionMap.Info info)
    {
        if (info.OpenLigaDbShortcut == null) return null;
        try
        {
            var url = $"{Base}/getbltable/{info.OpenLigaDbShortcut}/{CurrentSeasonYear()}";
            var raw = await _dog.FetchAsync(url);
            if (raw == null)
            {
                DataSourceHealth.RecordFailure(DataSourceHealth.Source.OpenLigaDb, $"network fetch returned null for {url}", _logger);
                return null;
            }

            var rows = JsonSerializer.Deserialize<List<OldbTableRow>>(raw, EspnJsonUtil.CaseInsensitive);
            if (rows == null || rows.Count == 0) return null;

            int position = 1;
            var table = rows.Select(row => new TableItem
            {
                Position = position++,
                Team = new Team { Name = row.TeamName ?? "Unknown", Crest = row.TeamIconUrl },
                PlayedGames = row.Matches,
                Won = row.Won,
                Draw = row.Draw,
                Lost = row.Lost,
                Points = row.Points,
                GoalsFor = row.Goals,
                GoalsAgainst = row.OpponentGoals,
                GoalDifference = row.GoalDiff,
            }).ToList();

            DataSourceHealth.RecordSuccess(DataSourceHealth.Source.OpenLigaDb, _logger);
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
            DataSourceHealth.RecordFailure(DataSourceHealth.Source.OpenLigaDb, $"getStandings exception: {ex.Message}", _logger);
            return null;
        }
    }
}
