namespace Soccer442.Api.Models;

public class NewsItem
{
    public string? Title { get; set; }
    public string? Link { get; set; }
    public string? ImgSrc { get; set; }

    public NewsItem() { }
    public NewsItem(string? title, string? link, string? imgSrc = null) { Title = title; Link = link; ImgSrc = imgSrc; }
}

public class PlayerProfile
{
    public string? Name { get; set; }
    public string? Extract { get; set; }
    public string? PhotoUrl { get; set; }
    public string? BirthDate { get; set; }
    public string? BirthPlace { get; set; }
    public string? Height { get; set; }
    public string? Position { get; set; }
    public string? CurrentClub { get; set; }
    public string? NationalTeam { get; set; }
    public string? Caps { get; set; }
    public string? Goals { get; set; }
}

public class TeamProfile
{
    public string? Name { get; set; }
    public string? Extract { get; set; }
    public string? CrestUrl { get; set; }
    public string? Ground { get; set; }
    public string? Capacity { get; set; }
    public string? Manager { get; set; }
    public string? Founded { get; set; }
    public string? League { get; set; }
}

/// <summary>Envelope every incremental-sync endpoint returns: items changed since the caller's
/// last sync, plus the server's own clock so the caller can persist a clock-skew-proof high
/// water mark for its next request.</summary>
public class SyncEnvelope<T>
{
    public DateTimeOffset ServerTimeUtc { get; set; }
    public List<T> Items { get; set; } = new();
}
