using System.IO.IsolatedStorage;
using _442.Api;

namespace _442
{
    public class SettingsManager
    {
        #region Properties

        readonly IsolatedStorageSettings _appSetting = IsolatedStorageSettings.ApplicationSettings;
        public bool   BPL { get; set; } 
        public bool LaLiga { get; set; }

        public bool Bundesliga { get; set; }

        public bool Absa { get; set; }

        public bool League1 { get; set; }

        public bool SerieA { get; set; }

        public bool UEFA { get; set; }

         public bool Log{ get; set; }
        public bool Live{ get; set; }
        public bool Fixture{ get; set; } 
        public bool Score{ get; set; }
        public bool Result { get; set; }

        #endregion Properties

        #region Constructor

        public SettingsManager()
        {


            if (_appSetting.Keys.Count == 0)
            {
                _appSetting.Add("BPL", "true");
                _appSetting.Add("LaLiga", "true");
                _appSetting.Add("Bundesliga", "true");
                _appSetting.Add("Absa", "true");
                _appSetting.Add("League1", "true");
                _appSetting.Add("SerieA", "true");
                _appSetting.Add("UEFA", "true");

                _appSetting.Add("Log", "true");
                _appSetting.Add("Live", "true");
                _appSetting.Add("Fixture", "true");
                _appSetting.Add("Score", "true");
                _appSetting.Add("Result", "true");

                BPL = true;
                LaLiga = true;
                Bundesliga = true;
                Absa = true;
                League1 = true;
                SerieA = true;
                UEFA = true;

                Log = true;
                Live = true;
                Fixture = true;
                Score = true;
                Result = true;
            }
            else
            {


                BPL = ShowingTeam(Competition.BPL);
                LaLiga = ShowingTeam(Competition.LaLiga);
                Bundesliga = ShowingTeam(Competition.Bundesliga);
                Absa = ShowingTeam(Competition.Absa);
                League1 = ShowingTeam(Competition.League1);
                SerieA = ShowingTeam(Competition.SerieA);
                UEFA = ShowingTeam(Competition.UEFA);

                Log = ShowingTeam("Log");
                Live = ShowingTeam("Live");
                Fixture = ShowingTeam("Fixture");
                Score = ShowingTeam("Score");
                Result = ShowingTeam("Result");

            }
        }

        #endregion Constructor

        #region Methods

        private bool ShowingTeam(Competition cmp)
        {
           var value = _appSetting[cmp.ToString()];
           return (string) value == "true";
        }
        private bool ShowingTeam(string cmp)
        {
            var value = _appSetting[cmp];
            return (string)value == "true";
        }
        public void Save()
        {
            _appSetting["BPL"] = BPL.ToString().ToLower();
            _appSetting["LaLiga"] = LaLiga.ToString().ToLower();
            _appSetting["Bundesliga"] = Bundesliga.ToString().ToLower();
            _appSetting["Absa"] = Absa.ToString().ToLower();
            _appSetting["League1"] = League1.ToString().ToLower();
            _appSetting["SerieA"] = SerieA.ToString().ToLower();
            _appSetting["UEFA"] = UEFA.ToString().ToLower();

          
            _appSetting["Log"] = Log.ToString().ToLower();
            _appSetting["Live"] = Live.ToString().ToLower();
            _appSetting["Fixture"] = Fixture.ToString().ToLower();
            _appSetting["Score"] = Score.ToString().ToLower();
            _appSetting["Result"] = Result.ToString().ToLower();
        }

        #endregion Methods
    }
}
