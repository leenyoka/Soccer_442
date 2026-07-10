using Soccer442.Api.Models;
using Soccer442.Api.Services;

namespace Soccer442.Api.Endpoints;

public static class MatchesEndpoints
{
    public static void MapMatchesEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGet("/api/competitions/{code}/matches", async (string code, string status, string? since, FootballCacheService svc) =>
        {
            var result = await svc.GetMatchesAsync(code, status, SinceParsing.ParseSince(since));
            return result == null ? Results.NotFound() : Results.Ok(result);
        });

        app.MapGet("/api/competitions/{code}/matches/{matchId}", async (string code, string matchId, FootballCacheService svc) =>
        {
            var match = await svc.GetMatchDetailsAsync(code, matchId);
            return match == null ? Results.NotFound() : Results.Ok(match);
        });

        // No natural per-event id/timestamp in ESPN's keyEvents feed to diff against (see
        // FootballCacheService.GetCommentaryAsync) - `since` is accepted for contract
        // consistency with every other list endpoint but always returns the full current list.
        app.MapGet("/api/competitions/{code}/matches/{matchId}/commentary", async (string code, string matchId, string? since, FootballCacheService svc) =>
        {
            var items = await svc.GetCommentaryAsync(code, matchId);
            return Results.Ok(new SyncEnvelope<Comment> { ServerTimeUtc = DateTimeOffset.UtcNow, Items = items });
        });

        app.MapGet("/api/competitions/{code}/matches/{matchId}/head-to-head", async (string code, string matchId, string home, string away, string? utcDate, FootballCacheService svc) =>
        {
            var summary = await svc.GetHeadToHeadAndFormAsync(code, matchId, home, away, utcDate);
            return summary == null ? Results.NotFound() : Results.Ok(summary);
        });
    }
}
