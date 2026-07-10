namespace Soccer442.Api.Data.Entities;

public class NewsEntity
{
    public int Id { get; set; }
    public string CompetitionCode { get; set; } = "";
    public string Link { get; set; } = ""; // natural dedupe key per competition
    public string? Title { get; set; }
    public string? ImgSrc { get; set; }
    public DateTimeOffset LastUpdatedUtc { get; set; }
}
