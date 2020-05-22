namespace _442.Api.Models
{
    public class TopGoalScorer
    {
        #region Properties

        public string Player { get; set; }
        public string Team { get; set; }
        public int Goals { get; set; }

        #endregion Properties

        #region Constructor

        public TopGoalScorer(string player, string team, int goals)
        {
            Player = player;
            Team = team;
            Goals = goals;
        }

        #endregion Constructor
    }
}
