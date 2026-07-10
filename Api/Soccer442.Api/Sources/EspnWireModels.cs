namespace Soccer442.Api.Sources;

// C# port of EspnScoreboardResponse.java / EspnStandingsResponse.java / EspnTeam.java -
// the raw wire shapes of ESPN's site.api.espn.com JSON, deserialized case-insensitively
// (see EspnClient.JsonOptions) since ESPN's own field names are already camelCase.

public class EspnScoreboardResponse
{
    public List<EspnEvent>? Events { get; set; }

    public class EspnEvent
    {
        public string? Id { get; set; }
        public string? Date { get; set; }
        public List<EspnCompetition>? Competitions { get; set; }
        public EspnStatus? Status { get; set; }
    }

    public class EspnCompetition
    {
        public List<EspnCompetitor>? Competitors { get; set; }
        public EspnVenue? Venue { get; set; }
    }

    public class EspnCompetitor
    {
        public string? HomeAway { get; set; }
        public EspnTeam? Team { get; set; }
        public string? Score { get; set; }
        public bool? Winner { get; set; }
        public int? ShootoutScore { get; set; }
    }

    public class EspnVenue
    {
        public string? FullName { get; set; }
    }

    public class EspnStatus
    {
        public EspnStatusType? Type { get; set; }
        public string? DisplayClock { get; set; }
    }

    public class EspnStatusType
    {
        public string? State { get; set; } // "pre" | "in" | "post"
        public string? Detail { get; set; }
    }
}

public class EspnStandingsResponse
{
    public List<EspnStandingsChild>? Children { get; set; }

    public class EspnStandingsChild
    {
        public string? Name { get; set; }
        public string? Abbreviation { get; set; }
        public EspnStandingsBlock? Standings { get; set; }
    }

    public class EspnStandingsBlock
    {
        public List<EspnStandingsEntry>? Entries { get; set; }
    }

    public class EspnStandingsEntry
    {
        public EspnTeam? Team { get; set; }
        public List<EspnStat>? Stats { get; set; }

        public double Stat(string name)
        {
            if (Stats == null) return 0;
            var match = Stats.FirstOrDefault(s => s.Name == name);
            return match?.Value ?? 0;
        }
    }

    public class EspnStat
    {
        public string? Name { get; set; }
        public double Value { get; set; }
    }
}

public class EspnTeam
{
    public string? Id { get; set; }
    public string? DisplayName { get; set; }
    public string? Name { get; set; }
    public string? ShortDisplayName { get; set; }
    public string? Abbreviation { get; set; }
    public string? Logo { get; set; }
    public List<EspnLogo>? Logos { get; set; }

    public class EspnLogo
    {
        public string? Href { get; set; }
    }

    public string? BestLogoUrl()
    {
        if (!string.IsNullOrEmpty(Logo)) return Logo;
        if (Logos != null && Logos.Count > 0) return Logos[0].Href;
        return null;
    }

    public string? BestName() => DisplayName ?? Name;
}
