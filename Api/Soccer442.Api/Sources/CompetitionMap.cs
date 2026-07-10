namespace Soccer442.Api.Sources;

/// <summary>
/// Maps the football-data.org-style competition codes the API's DTOs use to each free
/// data source's own identifier for that competition. A code missing a given source's id
/// simply means that source is skipped for that competition.
/// </summary>
public static class CompetitionMap
{
    public record Info(
        string Code,
        string Name,
        string AreaName,
        string EspnSlug,
        string? OpenLigaDbShortcut,
        string? SportsDbLeagueId);

    private static readonly Dictionary<string, Info> Competitions = new();

    static CompetitionMap()
    {
        Add("PL", "Premier League", "England", "eng.1", null, "4328");
        Add("ELC", "Championship", "England", "eng.2", null, "4329");
        Add("CL", "UEFA Champions League", "Europe", "uefa.champions", null, "4480");
        Add("EC", "European Championship", "Europe", "uefa.euro", null, null);
        Add("BL1", "Bundesliga", "Germany", "ger.1", "bl1", "4331");
        Add("SA", "Serie A", "Italy", "ita.1", null, "4332");
        Add("FL1", "Ligue 1", "France", "fra.1", null, "4334");
        Add("PD", "Primera Division", "Spain", "esp.1", null, "4335");
        Add("DED", "Eredivisie", "Netherlands", "ned.1", null, "4337");
        Add("PPL", "Primeira Liga", "Portugal", "por.1", null, "4344");
        Add("BSA", "Campeonato Brasileiro Série A", "Brazil", "bra.1", null, "4351");
        Add("CLI", "Copa Libertadores", "South America", "conmebol.libertadores", null, null);
        Add("WC", "FIFA World Cup", "World", "fifa.world", null, "4429");
    }

    private static void Add(string code, string name, string area, string espnSlug, string? openLigaDbShortcut, string? sportsDbLeagueId)
        => Competitions[code] = new Info(code, name, area, espnSlug, openLigaDbShortcut, sportsDbLeagueId);

    public static Info? ByCode(string code) => Competitions.GetValueOrDefault(code);

    public static IReadOnlyCollection<Info> All() => Competitions.Values;
}
