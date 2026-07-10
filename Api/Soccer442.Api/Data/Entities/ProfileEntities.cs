namespace Soccer442.Api.Data.Entities;

public class PlayerProfileEntity
{
    public int Id { get; set; }
    public string PlayerName { get; set; } = ""; // normalized (lowercased) lookup key
    public string RawJson { get; set; } = "";
    public DateTimeOffset LastUpdatedUtc { get; set; }
}

public class TeamProfileEntity
{
    public int Id { get; set; }
    public string TeamName { get; set; } = ""; // normalized (lowercased) lookup key
    public string RawJson { get; set; } = "";
    public DateTimeOffset LastUpdatedUtc { get; set; }
}
