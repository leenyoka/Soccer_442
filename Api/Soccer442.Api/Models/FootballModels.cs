namespace Soccer442.Api.Models;

// Mirrors the Android app's football_data POJOs, trimmed of fields that were never
// populated by any of the actual data sources (a football-data.org-shaped legacy left
// over from before this app talked to ESPN/BBC/OpenLigaDB/SportsDB directly) - Filters,
// ResultSet, Referee, Odds, Coach, Winner, CurrentSeason, Season are all dropped for
// that reason. Field names are camelCase to match the Android app's existing Gson POJOs
// 1:1 (Program.cs configures System.Text.Json for camelCase output).

public class Area
{
    public string? Name { get; set; }
}

public class Competition
{
    public string? Name { get; set; }
    public string? Code { get; set; }
}

public class Player
{
    public int Id { get; set; }
    public string? Name { get; set; }
    public string? ShortName { get; set; }
    public string? Position { get; set; }
    public int ShirtNumber { get; set; }
    public string? PhotoUrl { get; set; }
    public string? Section { get; set; }
}

public class Statistics
{
    public int CornerKicks { get; set; }
    public int FreeKicks { get; set; }
    public int GoalKicks { get; set; }
    public int Offsides { get; set; }
    public int Fouls { get; set; }
    public int BallPossession { get; set; }
    public int Saves { get; set; }
    public int ThrowIns { get; set; }
    public int Shots { get; set; }
    public int ShotsOnGoal { get; set; }
    public int ShotsOffGoal { get; set; }
    public int YellowCards { get; set; }
    public int YellowRedCards { get; set; }
    public int RedCards { get; set; }
}

public class Team
{
    public int Id { get; set; }
    public string? Name { get; set; }
    public string? ShortName { get; set; }
    public string? Tla { get; set; }
    public string? Crest { get; set; }
    public List<Player>? Lineup { get; set; }
    public List<Player>? Bench { get; set; }
    public Statistics? Statistics { get; set; }
}

public class FullTime
{
    public int Home { get; set; }
    public int Away { get; set; }
}

public class HalfTime
{
    public int Home { get; set; }
    public int Away { get; set; }
}

public class Score
{
    public string? Winner { get; set; }
    public string? Duration { get; set; }
    public FullTime FullTime { get; set; } = new();
    public HalfTime HalfTime { get; set; } = new();
    public int? PenaltyHome { get; set; }
    public int? PenaltyAway { get; set; }
}

public class FootballMatch
{
    public Area? Area { get; set; }
    public Competition? Competition { get; set; }
    public int Id { get; set; }
    public string? UtcDate { get; set; }
    public string? Status { get; set; }
    public string? Venue { get; set; }
    public Team? HomeTeam { get; set; }
    public Team? AwayTeam { get; set; }
    public Score? Score { get; set; }
}

public class MatchResponse
{
    public Competition? Competition { get; set; }
    public List<FootballMatch> Matches { get; set; } = new();
}

public class TableItem
{
    public int Position { get; set; }
    public Team? Team { get; set; }
    public int PlayedGames { get; set; }
    public int Won { get; set; }
    public int Draw { get; set; }
    public int Lost { get; set; }
    public int Points { get; set; }
    public int GoalsFor { get; set; }
    public int GoalsAgainst { get; set; }
    public int GoalDifference { get; set; }
}

public class StandingsItem
{
    public string? Stage { get; set; }
    public string? Type { get; set; }
    public string? Group { get; set; }
    public List<TableItem> Table { get; set; } = new();
}

public class StandingsResponse
{
    public Area? Area { get; set; }
    public Competition? Competition { get; set; }
    public List<StandingsItem> Standings { get; set; } = new();
}

public class Scorer
{
    public Player? Player { get; set; }
    public Team? Team { get; set; }
    public int Goals { get; set; }
}

public class ScorerResponse
{
    public Competition? Competition { get; set; }
    public List<Scorer> Scorers { get; set; } = new();
    public int Count { get; set; }
}

public class Comment
{
    public string? Time { get; set; }
    public string? Text { get; set; }

    public Comment() { }
    public Comment(string? time, string? text) { Time = time; Text = text; }
}

public class GameResult
{
    public string? Date { get; set; }
    public string? OpponentName { get; set; }
    public string? ScoreLine { get; set; }
    public string? Result { get; set; } // "W" | "L" | "D"
    public string? CompetitionName { get; set; }
}

public class HeadToHeadSummary
{
    public List<GameResult>? HeadToHead { get; set; }
    public List<GameResult>? HomeForm { get; set; }
    public List<GameResult>? AwayForm { get; set; }
}
