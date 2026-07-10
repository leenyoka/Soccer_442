using Soccer442.Api.Services;

namespace Soccer442.Api.Endpoints;

public static class NewsEndpoints
{
    public static void MapNewsEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGet("/api/news/{code}", async (string code, string? since, FootballCacheService svc) =>
        {
            var result = await svc.GetNewsAsync(code, SinceParsing.ParseSince(since));
            return result == null ? Results.NotFound() : Results.Ok(result);
        });
    }
}
