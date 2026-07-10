using Microsoft.EntityFrameworkCore;
using Soccer442.Api.Data.Entities;

namespace Soccer442.Api.Data;

/// <summary>
/// The API's read-through cache store. Rich per-request payloads (matches, standings,
/// scorers, profiles) are stored as a RawJson text column (the DTO serialized as-is) rather
/// than fully normalized - this is a cache in front of ESPN/BBC/OpenLigaDB/SportsDB/News/
/// Wikipedia, not a system of record, so keeping the schema small matters more than being
/// able to query into the payload's internals.
/// </summary>
public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

    public DbSet<MatchEntity> Matches => Set<MatchEntity>();
    public DbSet<StandingsEntity> Standings => Set<StandingsEntity>();
    public DbSet<ScorerEntity> Scorers => Set<ScorerEntity>();
    public DbSet<NewsEntity> NewsItems => Set<NewsEntity>();
    public DbSet<PlayerProfileEntity> PlayerProfiles => Set<PlayerProfileEntity>();
    public DbSet<TeamProfileEntity> TeamProfiles => Set<TeamProfileEntity>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<MatchEntity>(e =>
        {
            e.HasIndex(m => new { m.CompetitionCode, m.Status, m.LastUpdatedUtc });
            e.HasIndex(m => new { m.CompetitionCode, m.Status, m.MatchId }).IsUnique();
        });

        modelBuilder.Entity<StandingsEntity>(e => e.HasIndex(s => s.CompetitionCode).IsUnique());
        modelBuilder.Entity<ScorerEntity>(e => e.HasIndex(s => s.CompetitionCode).IsUnique());
        modelBuilder.Entity<NewsEntity>(e => e.HasIndex(n => new { n.CompetitionCode, n.Link }).IsUnique());
        modelBuilder.Entity<PlayerProfileEntity>(e => e.HasIndex(p => p.PlayerName).IsUnique());
        modelBuilder.Entity<TeamProfileEntity>(e => e.HasIndex(t => t.TeamName).IsUnique());
    }
}
