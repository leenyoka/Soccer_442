using System.Collections.Generic;

namespace _442.Api.Models
{
    public class Live
    {
        #region Properties
        public string HomeTeamName { get; set; }
        public string AwayTeamName { get; set; }
        public int HomeTeamScore { get; set; }
        public int AwayTeamScore { get; set; }
        public long MatchId { get; set; }
        public string MatchStatus { get; set; }
        public List<string> HomeTeamGoalScorers { get; set; }
        public List<string> AwayTeamGoalScorers { get; set; }
        public List<Comment> Commentry { get; set; }
        #endregion Properties

        #region Constructor

        public Live(string home, string away, int homeScore, int awayScore, long matchId)
        {
            HomeTeamName = home;
            AwayTeamName = away;
            HomeTeamScore = homeScore;
            AwayTeamScore = awayScore;
            MatchId = matchId;
        }

        #endregion Constructor
    }
}
