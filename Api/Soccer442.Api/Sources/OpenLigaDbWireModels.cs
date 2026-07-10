namespace Soccer442.Api.Sources;

// C# port of OldbMatch.java / OldbTableRow.java - raw wire shapes of api.openligadb.de JSON.

public class OldbMatch
{
    public int MatchID { get; set; }
    public string? MatchDateTimeUTC { get; set; }
    public bool MatchIsFinished { get; set; }
    public OldbTeam? Team1 { get; set; }
    public OldbTeam? Team2 { get; set; }
    public List<OldbResult>? MatchResults { get; set; }
    public OldbLocation? Location { get; set; }

    public class OldbTeam
    {
        public int TeamId { get; set; }
        public string? TeamName { get; set; }
        public string? TeamIconUrl { get; set; }
    }

    public class OldbResult
    {
        public int ResultTypeID { get; set; }
        public int PointsTeam1 { get; set; }
        public int PointsTeam2 { get; set; }
    }

    public class OldbLocation
    {
        public string? LocationStadium { get; set; }
    }
}

public class OldbTableRow
{
    public int TeamInfoId { get; set; }
    public string? TeamName { get; set; }
    public string? TeamIconUrl { get; set; }
    public int Matches { get; set; }
    public int Won { get; set; }
    public int Draw { get; set; }
    public int Lost { get; set; }
    public int Points { get; set; }
    public int Goals { get; set; }
    public int OpponentGoals { get; set; }
    public int GoalDiff { get; set; }
}
