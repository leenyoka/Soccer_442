using Microsoft.EntityFrameworkCore;
using Soccer442.Api.Data;
using Soccer442.Api.Endpoints;
using Soccer442.Api.Services;
using Soccer442.Api.Sources;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("Default")));

// camelCase everywhere, matching the Android app's existing Gson POJO field names 1:1.
builder.Services.ConfigureHttpJsonOptions(options =>
{
    options.SerializerOptions.PropertyNamingPolicy = System.Text.Json.JsonNamingPolicy.CamelCase;
});

builder.Services.AddHttpClient<HttpFetcher>(client =>
{
    client.Timeout = TimeSpan.FromSeconds(20);
});

builder.Services.AddScoped<EspnClient>();
builder.Services.AddScoped<BbcClient>();
builder.Services.AddScoped<OpenLigaDbClient>();
builder.Services.AddScoped<SportsDbClient>();
builder.Services.AddScoped<NewsClient>();
builder.Services.AddScoped<WikipediaClient>();
builder.Services.AddScoped<FootballCacheService>();

var app = builder.Build();

// Applies pending migrations on startup so `docker compose up` needs no manual migration step.
using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
    db.Database.Migrate();
}

app.MapGet("/", () => "Soccer442.Api");
app.MapCompetitionsEndpoints();
app.MapMatchesEndpoints();
app.MapStandingsAndScorersEndpoints();
app.MapNewsEndpoints();
app.MapProfilesEndpoints();

app.Run();
