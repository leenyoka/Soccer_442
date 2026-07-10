using System.Collections.Concurrent;

namespace Soccer442.Api.Sources;

/// <summary>
/// Tracks whether each upstream data source has been failing, so the cache service can skip
/// straight past a source that's currently broken instead of waiting on (and retrying) it on
/// every request. In-memory only, process-lifetime - a fresh deploy/restart gives every source
/// a fair try again since site/API status changes over time.
///
/// "Failing" means the source's own client hit an exception or unusable response while talking
/// to it (a real breakage), not "returned zero results" - an empty result can be entirely
/// legitimate (no live matches right now) and isn't on its own evidence a source is down.
/// </summary>
public static class DataSourceHealth
{
    public enum Source { Espn, OpenLigaDb, Bbc, SportsDb }

    private const int FailureThreshold = 3;
    private static readonly TimeSpan Cooldown = TimeSpan.FromMinutes(5);

    private static readonly ConcurrentDictionary<Source, int> ConsecutiveFailures = new();
    private static readonly ConcurrentDictionary<Source, DateTimeOffset> LastFailureAt = new();

    public static bool IsAvailable(Source source, ILogger logger)
    {
        if (!ConsecutiveFailures.TryGetValue(source, out var failures) || failures < FailureThreshold) return true;
        if (!LastFailureAt.TryGetValue(source, out var lastFailure)) return true;
        var cooledDown = DateTimeOffset.UtcNow - lastFailure > Cooldown;
        if (cooledDown) logger.LogInformation("{Source} cooldown elapsed - giving it another try", source);
        return cooledDown;
    }

    public static void RecordSuccess(Source source, ILogger logger)
    {
        if (ConsecutiveFailures.TryGetValue(source, out var prev) && prev > 0)
            logger.LogInformation("{Source} recovered after {Prev} consecutive failures", source, prev);
        ConsecutiveFailures[source] = 0;
    }

    public static void RecordFailure(Source source, string reason, ILogger logger)
    {
        var failures = ConsecutiveFailures.AddOrUpdate(source, 1, (_, v) => v + 1);
        LastFailureAt[source] = DateTimeOffset.UtcNow;
        if (failures == FailureThreshold)
            logger.LogWarning("{Source} marked unhealthy after {Failures} consecutive failures ({Reason}) - skipping it for {Cooldown}s",
                source, failures, reason, Cooldown.TotalSeconds);
        else
            logger.LogWarning("{Source} failure {Failures}/{Threshold}: {Reason}", source, failures, FailureThreshold, reason);
    }

    public static void LogServedBy(string request, Source source, ILogger logger)
        => logger.LogInformation("{Request} served by {Source}", request, source);

    public static void LogNoSourceAvailable(string request, ILogger logger)
        => logger.LogWarning("{Request} - no source returned usable data", request);
}
