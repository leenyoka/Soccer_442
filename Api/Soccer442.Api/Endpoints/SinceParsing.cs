namespace Soccer442.Api.Endpoints;

internal static class SinceParsing
{
    /// <summary>Parses the `since` query param (ISO8601 UTC) - absent/unparseable means
    /// "beginning of time", i.e. the caller's first sync, so everything comes back.</summary>
    public static DateTimeOffset ParseSince(string? since)
        => since != null && DateTimeOffset.TryParse(since, out var parsed) ? parsed : DateTimeOffset.MinValue;
}
