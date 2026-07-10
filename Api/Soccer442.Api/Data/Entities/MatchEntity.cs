namespace Soccer442.Api.Data.Entities;

public class MatchEntity
{
    public int Id { get; set; }
    public string CompetitionCode { get; set; } = "";
    public string MatchId { get; set; } = "";
    public string Status { get; set; } = ""; // "fixture" | "result" | "live" - the wantedStatus this row was cached under
    public string? UtcDate { get; set; }
    public string RawJson { get; set; } = "";
    public DateTimeOffset LastUpdatedUtc { get; set; }
}
