using Soccer442.Api.Services;

namespace Soccer442.Api.Endpoints;

public static class ProfilesEndpoints
{
    public static void MapProfilesEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGet("/api/players/{name}/profile", async (string name, FootballCacheService svc) =>
        {
            var profile = await svc.GetPlayerProfileAsync(name);
            return profile == null ? Results.NotFound() : Results.Ok(profile);
        });

        app.MapGet("/api/players/{name}/photo", async (string name, FootballCacheService svc) =>
        {
            var url = await svc.GetPlayerPhotoUrlAsync(name);
            return url == null ? Results.NotFound() : Results.Ok(new { photoUrl = url });
        });

        app.MapGet("/api/teams/{name}/profile", async (string name, FootballCacheService svc) =>
        {
            var profile = await svc.GetTeamProfileAsync(name);
            return profile == null ? Results.NotFound() : Results.Ok(profile);
        });
    }
}
