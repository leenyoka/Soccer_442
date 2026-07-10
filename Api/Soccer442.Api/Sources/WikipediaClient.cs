using System.Text.Json;
using System.Text.RegularExpressions;
using System.Web;
using Soccer442.Api.Models;

namespace Soccer442.Api.Sources;

/// <summary>
/// C# port of WikipediaClient.java - player/team profile screens sourced from Wikipedia -
/// free, key-less, and Wikimedia explicitly permits automated access to these endpoints. Two
/// calls per profile: the REST summary API (photo + short bio) and the action API's wikitext
/// (for the infobox's structured fields). Name matching is best-effort: try the name
/// directly first, fall back to Wikipedia's own search API on a miss.
///
/// The Android app's old in-memory PHOTO_CACHE is superseded here by Postgres-backed caching
/// in FootballCacheService (persistent, shared across every device instead of per-process).
/// </summary>
public class WikipediaClient
{
    private readonly HttpFetcher _dog;
    private readonly ILogger<WikipediaClient> _logger;

    public WikipediaClient(HttpFetcher dog, ILogger<WikipediaClient> logger)
    {
        _dog = dog;
        _logger = logger;
    }

    /// <summary>Lightweight sibling of GetPlayerProfileAsync for lineup chips, which only need
    /// a photo - skips the second wikitext fetch entirely.</summary>
    public async Task<string?> GetPlayerPhotoUrlAsync(string playerName)
    {
        if (string.IsNullOrEmpty(playerName)) return null;
        var summary = await ResolveSummaryAsync(playerName, new[] { "footballer" });
        return summary != null ? ThumbnailUrl(summary.Value) : null;
    }

    public async Task<PlayerProfile?> GetPlayerProfileAsync(string playerName)
    {
        var summary = await ResolveSummaryAsync(playerName, new[] { "footballer" });
        if (summary == null) return null;

        var title = StringField(summary.Value, "title");
        var profile = new PlayerProfile
        {
            Name = title ?? playerName,
            Extract = StringField(summary.Value, "extract"),
            PhotoUrl = ThumbnailUrl(summary.Value),
        };

        var wikitext = await FetchLeadWikitextAsync(profile.Name!);
        if (wikitext != null)
        {
            profile.BirthDate = CleanWikitext(ExtractParam(wikitext, "birth_date"));
            profile.BirthPlace = CleanWikitext(ExtractParam(wikitext, "birth_place"));
            profile.Height = CleanWikitext(ExtractParam(wikitext, "height"));
            profile.Position = CleanWikitext(ExtractParam(wikitext, "position"));
            profile.CurrentClub = CleanWikitext(ExtractParam(wikitext, "currentclub"));
            profile.NationalTeam = CleanWikitext(ExtractLastNumberedParam(wikitext, "nationalteam"));
            profile.Caps = CleanWikitext(ExtractLastNumberedParam(wikitext, "nationalcaps"));
            profile.Goals = CleanWikitext(ExtractLastNumberedParam(wikitext, "nationalgoals"));
        }
        return profile;
    }

    public async Task<TeamProfile?> GetTeamProfileAsync(string teamName)
    {
        // National teams (World Cup, Euros...) share their name with a country article
        // ("France" -> the country, not "France national football team"), so both hints are
        // tried and whichever result actually looks football-related wins.
        var summary = await ResolveSummaryAsync(teamName, new[] { "national football team", "football club" });
        if (summary == null) return null;

        var title = StringField(summary.Value, "title");
        var profile = new TeamProfile
        {
            Name = title ?? teamName,
            Extract = StringField(summary.Value, "extract"),
            CrestUrl = ThumbnailUrl(summary.Value),
        };

        var wikitext = await FetchLeadWikitextAsync(profile.Name!);
        if (wikitext != null)
        {
            profile.Ground = CleanWikitext(ExtractParam(wikitext, "ground"));
            profile.Capacity = CleanWikitext(ExtractParam(wikitext, "capacity"));
            profile.Manager = CleanWikitext(ExtractParam(wikitext, "manager"));
            profile.Founded = CleanWikitext(ExtractParam(wikitext, "founded"));
            profile.League = CleanWikitext(ExtractParam(wikitext, "league"));
        }
        return profile;
    }

    // ---- Resolution + fetching ----

    private async Task<JsonElement?> ResolveSummaryAsync(string name, string[] searchHints)
    {
        var direct = await FetchSummaryAsync(name.Replace(" ", "_"));
        if (direct != null && LooksFootballRelated(direct.Value)) return direct;

        foreach (var hint in searchHints)
        {
            var resolvedTitle = await SearchTopTitleAsync(name, hint);
            if (resolvedTitle == null) continue;
            var searched = await FetchSummaryAsync(resolvedTitle.Replace(" ", "_"));
            if (searched != null && LooksFootballRelated(searched.Value)) return searched;
        }
        // Nothing confirmed football-related - fall back to the direct hit if there was one
        // at all (better than nothing), otherwise give up.
        return direct;
    }

    private static bool LooksFootballRelated(JsonElement summary)
    {
        var description = StringField(summary, "description");
        if (description == null) return false;
        var lower = description.ToLowerInvariant();
        return lower.Contains("football") || lower.Contains("soccer");
    }

    private async Task<JsonElement?> FetchSummaryAsync(string titleUnderscored)
    {
        try
        {
            var url = $"https://en.wikipedia.org/api/rest_v1/page/summary/{EncodeTitle(titleUnderscored)}";
            var raw = await _dog.FetchAsync(url);
            if (raw == null) return null;
            using var doc = JsonDocument.Parse(raw);
            var obj = doc.RootElement;
            if (obj.TryGetProperty("type", out var type) && type.GetString() == "disambiguation") return null;
            if (!obj.TryGetProperty("title", out _)) return null;
            return obj.Clone();
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "fetchSummary failed for {Title}", titleUnderscored);
            return null;
        }
    }

    private async Task<string?> SearchTopTitleAsync(string query, string hint)
    {
        try
        {
            var encoded = HttpUtility.UrlEncode($"{query} {hint}");
            var url = $"https://en.wikipedia.org/w/api.php?action=query&list=search&srlimit=1&format=json&srsearch={encoded}";
            var raw = await _dog.FetchAsync(url);
            if (raw == null) return null;
            using var doc = JsonDocument.Parse(raw);
            var results = doc.RootElement.GetProperty("query").GetProperty("search");
            if (results.GetArrayLength() == 0) return null;
            return StringField(results[0], "title");
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "searchTopTitle failed for {Query}", query);
            return null;
        }
    }

    private async Task<string?> FetchLeadWikitextAsync(string titleUnderscored)
    {
        try
        {
            var url = $"https://en.wikipedia.org/w/api.php?action=parse&prop=wikitext&section=0&format=json&formatversion=2&page={EncodeTitle(titleUnderscored)}";
            var raw = await _dog.FetchAsync(url);
            if (raw == null) return null;
            using var doc = JsonDocument.Parse(raw);
            if (!doc.RootElement.TryGetProperty("parse", out var parse)) return null;
            return StringField(parse, "wikitext");
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "fetchLeadWikitext failed for {Title}", titleUnderscored);
            return null;
        }
    }

    // Player/team names routinely contain non-ASCII characters (Mbappé, Díaz, Koundé...) -
    // sending them raw in a URL is invalid per RFC 3986 and Wikipedia's API returns a plain
    // HTTP 400 for it.
    private static string EncodeTitle(string titleUnderscored)
    {
        try { return Uri.EscapeDataString(titleUnderscored); }
        catch { return titleUnderscored; }
    }

    // ---- Infobox wikitext parsing ----

    // A field's value normally ends at the next top-level "|" or a newline, but values are
    // often themselves a {{template|...}} or [[link|...]] containing their own internal
    // pipes. Trying the block alternatives before the single-char fallback at every position
    // forces the whole {{...}} or [[...]] to be consumed atomically wherever one starts.
    private const string ValuePattern = @"((?:\{\{[^}]*\}\}|\[\[[^\]]*\]\]|[^\n|])*)";

    private static string? ExtractParam(string wikitext, string paramName)
    {
        var pattern = new Regex(@"\|\s*" + Regex.Escape(paramName) + @"\s*=\s*" + ValuePattern);
        var match = pattern.Match(wikitext);
        return match.Success ? match.Groups[1].Value.Trim() : null;
    }

    // Numbered params (nationalteam1, nationalteam2, ...) list career history in order - the
    // last one present is the most recent/current entry.
    private static string? ExtractLastNumberedParam(string wikitext, string paramPrefix)
    {
        var pattern = new Regex(@"\|\s*" + Regex.Escape(paramPrefix) + @"\d*\s*=\s*" + ValuePattern);
        string? last = null;
        foreach (Match m in pattern.Matches(wikitext))
        {
            var val = m.Groups[1].Value.Trim();
            if (val.Length > 0) last = val;
        }
        return last;
    }

    private static string? CleanWikitext(string? raw)
    {
        if (raw == null) return null;
        var cleaned = raw;
        cleaned = Regex.Replace(cleaned, @"\{\{convert\|([0-9.]+)\|([a-zA-Z]+)[^}]*\}\}", "$1 $2");
        cleaned = Regex.Replace(cleaned, @"\{\{birth date and age\|(\d+)\|(\d+)\|(\d+)[^}]*\}\}", "$1-$2-$3");
        cleaned = Regex.Replace(cleaned, @"\{\{flagicon[^}]*\}\}", "");
        cleaned = Regex.Replace(cleaned, @"\{\{[^{}]*\}\}", "");
        cleaned = Regex.Replace(cleaned, @"\[\[(?:[^\]|]*\|)?([^\]]+)\]\]", "$1");
        cleaned = Regex.Replace(cleaned, "'''?", "");
        cleaned = Regex.Replace(cleaned, "<[^>]+>", "");
        cleaned = cleaned.Trim();
        return cleaned.Length == 0 ? null : cleaned;
    }

    private static string? StringField(JsonElement obj, string field)
    {
        if (!obj.TryGetProperty(field, out var val) || val.ValueKind == JsonValueKind.Null) return null;
        return val.ValueKind == JsonValueKind.String ? val.GetString() : val.ToString();
    }

    private static string? ThumbnailUrl(JsonElement summary)
    {
        if (!summary.TryGetProperty("thumbnail", out var thumb) || thumb.ValueKind != JsonValueKind.Object) return null;
        return StringField(thumb, "source");
    }
}
