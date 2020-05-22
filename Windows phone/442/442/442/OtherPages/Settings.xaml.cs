namespace _442.OtherPages
{
    public partial class Settings
    {
        readonly SettingsManager _settings;
        public Settings()
        {
            InitializeComponent();
            _settings = new SettingsManager();
            Load();
        }

        private void Load()
        {
            BoxBpl.IsChecked = _settings.BPL;
            BoxLaLiga.IsChecked = _settings.LaLiga;
            BoxSerieA.IsChecked = _settings.SerieA;
            BoxBundesliga.IsChecked = _settings.Bundesliga;
            BoxLeague1.IsChecked = _settings.League1;
            BoxAbsa.IsChecked = _settings.Absa;
            BoxLive.IsChecked = _settings.Live;
            BoxFixture.IsChecked = _settings.Fixture;
            BoxResult.IsChecked = _settings.Result;
            BoxLog.IsChecked = _settings.Log;
            BoxScore.IsChecked = _settings.Score;
        }

        private void Save()
        {
            if (BoxBpl.IsChecked != null)
            {
                _settings.BPL = (bool) BoxBpl.IsChecked;
            }
            if (BoxLaLiga.IsChecked != null)
            {
                _settings.LaLiga = (bool) BoxLaLiga.IsChecked;
            }
            if (BoxSerieA.IsChecked != null)
            {
                _settings.SerieA = (bool) BoxSerieA.IsChecked;
            }
            if (BoxBundesliga.IsChecked != null)
            {
                _settings.Bundesliga = (bool) BoxBundesliga.IsChecked;
            }
            if (BoxLeague1.IsChecked != null)
            {
                _settings.League1 = (bool) BoxLeague1.IsChecked;
            }
            if (BoxAbsa.IsChecked != null)
            {
                _settings.Absa = (bool) BoxAbsa.IsChecked;
            }
            if (BoxLive.IsChecked != null)
            {
                _settings.Live = (bool) BoxLive.IsChecked;
            }
            if (BoxFixture.IsChecked != null)
            {
                _settings.Fixture = (bool) BoxFixture.IsChecked;
            }
            if (BoxResult.IsChecked != null)
            {
                _settings.Result = (bool) BoxResult.IsChecked;
            }
            if (BoxLog.IsChecked != null)
            {
                _settings.Log = (bool) BoxLog.IsChecked;
            }
            if (BoxScore.IsChecked != null)
            {
                _settings.Score = (bool) BoxScore.IsChecked;
            }


            _settings.Save();
        }

        private void PhoneApplicationPage_BackKeyPress(object sender, System.ComponentModel.CancelEventArgs e)
        {
            Save();
        }
    }
}