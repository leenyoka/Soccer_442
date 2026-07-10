using Soccer442.Api.Services;

namespace Soccer442.Api.Endpoints;

public static class CompetitionsEndpoints
{
    public static void MapCompetitionsEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGet("/api/competitions", () => Results.Ok(FootballCacheService.GetCompetitions()));
    }
}
