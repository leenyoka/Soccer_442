using System.Globalization;

namespace _442.Api
{
    public class AppConfig
    {

        #region Properties

        const string BPL = "barclays-premier-league";
        const string FA = "fa-cup";
        const string AFCON = "africa-cup-of-nations";
        const string LaLiga = "spain";
        const string Bundasliga = "germany";
        const string Absa = "absa-premiership";
        const string League1 = "france";
        const string serieA = "italy";
        const string UEFA = "uefa-champions-league";
        const string UEFA_Euro = "uefa-euro";
        const string copa = "copa-del-rey";

        public const string LogUri = "mobi.supersport.com/football/{0}/logs";
        public const string MatchDetails = "mobi.supersport.com/football/{0}/match/";//"{1}/commentary";
        public const string LiveUri = "mobi.supersport.com/football/{0}";
        public const string FixtureUri = "www.supersport.com/football/{0}/fixtures";
        public const string ResultUri = "www.supersport.com/football/{0}/Results";
        public const string ScorersUri = "www.supersport.com/football/{0}/scorers";
        #endregion Properties

        #region Methods
        public string GetMatchDetails(Competition cometition, long matchId)
        {
            return GetUri(MatchDetails, cometition) + matchId.ToString
                (CultureInfo.InvariantCulture) + "/commentary";
        }
        public string GetFixtureUri(Competition competition)
        {
            return GetUri(FixtureUri, competition);
        }
        public string GetResultsUri(Competition competition)
        {
            return GetUri(ResultUri, competition);
        }
        public string GetScorersUr(Competition competition)
        {
            return GetUri(ScorersUri, competition);
        }
        public string GetLogUri(Competition competition)
        {
            return GetUri(LogUri, competition);
        }
        public string GetLiveUri(Competition competition)
        {
            return GetUri(LiveUri, competition);
        }
        private string GetUri(string uri, Competition competition)
        {
            switch (competition)
            {
                case Competition.Absa: return string.Format(uri, Absa);
                case Competition.AFCON: return string.Format(uri, AFCON);
                case Competition.BPL: return string.Format(uri, BPL);
                case Competition.Bundesliga: return string.Format(uri, Bundasliga);
                case Competition.Copa: return string.Format(uri, copa);
                case Competition.FA: return string.Format(uri, FA);
                case Competition.LaLiga: return string.Format(uri, LaLiga);
                case Competition.League1: return string.Format(uri, League1);
                case Competition.SerieA: return string.Format(uri, serieA);
                case Competition.UEFA: return string.Format(uri, UEFA);
                case Competition.UEFA_Euro: return string.Format(uri, UEFA_Euro);
            }
            return null;
        }

        #endregion Methods
    }
}
