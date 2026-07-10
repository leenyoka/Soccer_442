using Soccer442.Api.Services;

namespace Soccer442.Api.Endpoints;

public static class StandingsAndScorersEndpoints
{
    public static void MapStandingsAndScorersEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGet("/api/competitions/{code}/standings", async (string code, string? since, FootballCacheService svc) =>
        {
            var result = await svc.GetStandingsAsync(code, SinceParsing.ParseSince(since));
            return result == null ? Results.NotFound() : Results.Ok(result);
        });

        app.MapGet("/api/competitions/{code}/scorers", async (string code, string? since, FootballCacheService svc) =>
        {
            var result = await svc.GetScorersAsync(code, SinceParsing.ParseSince(since));
            return result == null ? Results.NotFound() : Results.Ok(result);
        });
    }
}
