using System.Collections.Generic;

namespace _442.Api.Models
{
    public class Result
    {
        #region Properties

        public string HomeTeamName { get; set; }
        public string AwayTeamName { get; set; }
        public int HomeTeamScore { get; set; }
        public int AwayTeamScore { get; set; }
        public string Date { get; set; }
        public List<string> HomeTeamGoalScorers { get; set; }
        public List<string> AwayTeamGoalScorers { get; set; }

        #endregion Properties

        #region Constructor

        public Result(string home, string away, int homeScore, int awayScore,
            string date, string homeScorers, string awayScorers)
        {
            HomeTeamName = home;
            AwayTeamName = away;
            HomeTeamScore = homeScore;
            AwayTeamScore = awayScore;
            Date = date;
            HomeTeamGoalScorers = GetScorers(homeScorers);
            AwayTeamGoalScorers = GetScorers(awayScorers);
        }

        #endregion Constructor

        #region Methods

        public List<string> GetScorers(string value)
        {
            List<string> scorers = new List<string>();
            string[] values = value.Split(')');

            foreach (string myValue in values)
                if (myValue.Trim().Length > 3)
                    scorers.Add(myValue.Trim().Trim(',') + ")");

            return scorers;
        }

        #endregion Methods

    }
}
