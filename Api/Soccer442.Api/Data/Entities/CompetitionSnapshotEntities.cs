namespace Soccer442.Api.Data.Entities;

/// <summary>One row per competition, upserted whenever standings are refreshed from upstream.</summary>
public class StandingsEntity
{
    public int Id { get; set; }
    public string CompetitionCode { get; set; } = "";
    public string RawJson { get; set; } = "";
    public DateTimeOffset LastUpdatedUtc { get; set; }
}

/// <summary>One row per competition, upserted whenever top scorers are refreshed from upstream.</summary>
public class ScorerEntity
{
    public int Id { get; set; }
    public string CompetitionCode { get; set; } = "";
    public string RawJson { get; set; } = "";
    public DateTimeOffset LastUpdatedUtc { get; set; }
}
