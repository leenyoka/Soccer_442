namespace _442.Api.Models
{
    public class LogItem
    {
        #region Properties

        public string TeamName { get; set; }
        public int GamesPlayed { get; set; }
        public int GamesWon { get; set; }
        public int GoalDifference { get; set; }
        public int Points { get; set; }

        #endregion Properties

        #region Constructor

        public LogItem(string name, int played, int won, int goalDifference, int points)
        {
            TeamName = name;
            GamesPlayed = played;
            GamesWon = won;
            GoalDifference = goalDifference;
            Points = points;
        }

        #endregion Constructor
    }
}
