using System;

namespace _442.Api.Models
{
    public class Fixture
    {

        #region Properties

        public string HomeTeamName { get; set; }
        public string AwayTeamName { get; set; }
        public DateTime FixtureDate { get; set; }
        public string Stadium { get; set; }
        public string Time { get; set; }

        #endregion Properties

        #region Constructor

        public Fixture(string home, string away, DateTime date, string stadium, string time)
        {
            HomeTeamName = home;
            AwayTeamName = away;
            FixtureDate = date;
            Stadium = stadium;
            Time = time;
        }

        #endregion Constructor


    }
}
