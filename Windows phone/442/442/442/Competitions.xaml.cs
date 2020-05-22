using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using _442.Api;
using _442.Api.Models;

namespace _442
{
    public partial class Competitions
    {
        readonly SuperSport _superSport = new SuperSport();
        SettingsManager _settings;
        private int _errors;
        public  Competitions()
        {
            InitializeComponent();
            App.Handler = BPL_SelectionChanged;
        }


        private bool IsInLive(Fixture game)
        {
            if (game == null) throw new ArgumentNullException("game");
            var boxes = new[] {BPL, LaLiga, SerieA, Bundesliga, League1, Absa};

            foreach (var item in boxes.SelectMany(listBox => listBox.Items))
            {
                try
                {
                    var view = (IView) item;

                    if (view.GetViewType() != ViewType.Live) continue;
                    var live = (LiveView) view;

                    if (live.MyContent.Any(life => life.HomeTeamName.Trim() == game.HomeTeamName.Trim()
                                                   && life.AwayTeamName.Trim() == game.AwayTeamName.Trim()))
                    {
                        return true;
                    }
                }
                catch (Exception)
                {
                    _errors++;
                }
            }

            return false;
        }

        private Fixture GetOneToShow(IEnumerable<Fixture> games)
        {
            return games.FirstOrDefault(game => !IsInLive(game));
        }

        public bool HasNotBeenAdded(ItemsControl box, ViewType type)
        {
            foreach (var view in box.Items)
            {

                try
                {
                    var viewCurrent = (IView)view;
                    if (viewCurrent.GetViewType() == type) return false;
                }
                catch (Exception)
                {
                    _errors++;
                }

            }
            return true;
        }
        private async Task<int> ShowResults()
        {
            var containers = new[] {BPL, SerieA, LaLiga, Bundesliga, League1, Absa};
            var show = new[]
            {
                _settings.BPL, _settings.SerieA, _settings.LaLiga,
                _settings.Bundesliga, _settings.League1, _settings.Absa
            };

            var competion = new[]
            {
                Competition.BPL, Competition.SerieA, Competition.LaLiga,
                Competition.Bundesliga, Competition.League1, Competition.Absa
            };

            for (var i = 0; i < 6; i++)
            {
                try
                {
                    if (!show[i]) continue;
                    if (!HasNotBeenAdded(containers[i], ViewType.Result)) continue;
                    var english = await _superSport.GetResults(competion[i]);
                    if (english.Count <= 0) continue;
                    containers[i].Items.Add(new Grid {Width = 40, Height = 13});
                    containers[i].Items.Add(new ResultView(english, competion[i]));
                }
                catch (Exception)
                { _errors++; }
            }

            return 1;
        }

        private async Task<int> ShowLiveGame()
        {
            var containers = new[] { BPL, SerieA, LaLiga, Bundesliga, League1, Absa };
            var show = new[]
            {
                _settings.BPL, _settings.SerieA, _settings.LaLiga,
                _settings.Bundesliga, _settings.League1, _settings.Absa
            };

            var competion = new[]
            {
                Competition.BPL, Competition.SerieA, Competition.LaLiga,
                Competition.Bundesliga, Competition.League1, Competition.Absa
            };

            for (var i = 0; i < 6; i++)
            {
                try
                {
                    if (!show[i]) continue;
                    if (!HasNotBeenAdded(containers[i], ViewType.Live)) continue;
                    var english = await _superSport.GetLive(competion[i]);
                    if (english.Count <= 0) continue;
                    containers[i].Items.Add(new Grid { Width = 40, Height = 13 });
                    containers[i].Items.Add(new LiveView(english, competion[i]));
                }
                catch (Exception)
                { _errors++; }
            }
            return 1;
        }

        private async Task<int> ShowLogs()
        {
            var containers = new[] { BPL, SerieA, LaLiga, Bundesliga, League1, Absa };
            var show = new[]
            {
                _settings.BPL, _settings.SerieA, _settings.LaLiga,
                _settings.Bundesliga, _settings.League1, _settings.Absa
            };

            var competion = new[]
            {
                Competition.BPL, Competition.SerieA, Competition.LaLiga,
                Competition.Bundesliga, Competition.League1, Competition.Absa
            };

            for (var i = 0; i < 6; i++)
            {
                try
                {
                    if (!show[i]) continue;
                    if (!HasNotBeenAdded(containers[i], ViewType.Log)) continue;
                    var english = await _superSport.GetLog(competion[i]);
                    if (english.Count <= 0) continue;
                    containers[i].Items.Add(new Grid { Width = 40, Height = 13 });
                    containers[i].Items.Add(new LogView(english, competion[i]));
                }
                catch (Exception)
                { _errors++; }
            }
            return 1;
        }

        private async Task<int> ShowScorers()
        {
            var containers = new[] { BPL, SerieA, LaLiga, Bundesliga, League1, Absa };
            var show = new[]
            {
                _settings.BPL, _settings.SerieA, _settings.LaLiga,
                _settings.Bundesliga, _settings.League1, _settings.Absa
            };

            var competion = new[]
            {
                Competition.BPL, Competition.SerieA, Competition.LaLiga,
                Competition.Bundesliga, Competition.League1, Competition.Absa
            };

            for (var i = 0; i < 6; i++)
            {
                try
                {
                    if (!show[i]) continue;
                    if (!HasNotBeenAdded(containers[i], ViewType.Score)) continue;
                    var english = await _superSport.GetScorers(competion[i]);
                    if (english.Count <= 0) continue;
                    containers[i].Items.Add(new Grid { Width = 40, Height = 13 });
                    containers[i].Items.Add(new ScorerView(english, competion[i]));
                }
                catch (Exception)
                { _errors++; }
            }
            return 1;
        }

        private async Task<int> ShowFixture()
        {
            var containers = new[] { BPL, SerieA, LaLiga, Bundesliga, League1, Absa };
            var show = new[]
            {
                _settings.BPL, _settings.SerieA, _settings.LaLiga,
                _settings.Bundesliga, _settings.League1, _settings.Absa
            };

            var competion = new[]
            {
                Competition.BPL, Competition.SerieA, Competition.LaLiga,
                Competition.Bundesliga, Competition.League1, Competition.Absa
            };

            for (var i = 0; i < 6; i++)
            {
                try
                {
                    if (!show[i]) continue;
                    if (!HasNotBeenAdded(containers[i], ViewType.Fixture)) continue;
                    var english = await _superSport.GetFixture(competion[i]);
                    if (english.Count <= 0) continue;
                    containers[i].Items.Add(new Grid {Width = 40, Height = 13});
                    containers[i].Items.Add(new FixtureView(GetOneToShow(english), competion[i]));
                }
                catch (Exception)
                {
                    _errors++;
                }
            }
            return 1;
        }

        private  void Panorama_Loaded(object sender, RoutedEventArgs e)
        {
           
        }

        private bool _busy;

        private void HideShow()
        {
            if (!_settings.BPL) BplItem.Visibility = Visibility.Collapsed;
            if (!_settings.Absa) AbsaItem.Visibility = Visibility.Collapsed;
            if (!_settings.LaLiga) LaLigaItem.Visibility = Visibility.Collapsed;
            if (!_settings.SerieA) SerieAItem.Visibility = Visibility.Collapsed;
            if (!_settings.Bundesliga) BundesligaItem.Visibility = Visibility.Collapsed;
            if (!_settings.League1) Leage1Item.Visibility = Visibility.Collapsed;

            if (_settings.BPL) BplItem.Visibility = Visibility.Visible;
            if (_settings.Absa) AbsaItem.Visibility = Visibility.Visible;
            if (_settings.LaLiga) LaLigaItem.Visibility = Visibility.Visible;
            if (_settings.SerieA) SerieAItem.Visibility = Visibility.Visible;
            if (_settings.Bundesliga) BundesligaItem.Visibility = Visibility.Visible;
            if (_settings.League1) Leage1Item.Visibility = Visibility.Visible;
        }
        private async void PhoneApplicationPage_Loaded(object sender, RoutedEventArgs e)
        {
            try
            {
                _busy = true;
                Progress.Visibility = Visibility.Visible;
                _settings = new SettingsManager();
                HideShow();
                BPL.Items.Clear();
                Absa.Items.Clear();
                LaLiga.Items.Clear();
                SerieA.Items.Clear();
                Bundesliga.Items.Clear();
                League1.Items.Clear();

                int x = 0;

                 if(_settings.Log)
                  x=  await ShowLogs();
                if(_settings.Live)
                    x = await ShowLiveGame();
                if(_settings.Fixture)
                    x = await ShowFixture();
                if(_settings.Score)
                    x = await ShowScorers();
                if(_settings.Result)
                    x = await ShowResults();
                

                Progress.Visibility = Visibility.Collapsed;
                if (x == 1)
                {
                    _busy = false;
                }
                    
            }
            catch (Exception)
            {

                _errors++;
            }
        }

        private void BPL_SelectionChanged(object sender, RoutedEventArgs e)
        {
            try
            {
                if (_busy) return;
                var view = (IView) sender;
                App.SelectedList = view;

                App.SelectedCompetition = view.GetCompetition();

                if (view.GetViewType() == ViewType.Fixture)
                {

                }
                else if (view.GetViewType() == ViewType.Live)
                {
                    NavigationService.Navigate(new Uri("/OtherPages/Live.xaml", UriKind.Relative));
                }
                else if (view.GetViewType() == ViewType.Log)
                {
                    NavigationService.Navigate(new Uri("/OtherPages/FullLog.xaml", UriKind.Relative));
                }
                else if (view.GetViewType() == ViewType.Result)
                {
                    NavigationService.Navigate(new Uri("/OtherPages/Results.xaml", UriKind.Relative));
                }
                else if (view.GetViewType() == ViewType.Score)
                {
                    NavigationService.Navigate(new Uri("/OtherPages/Scorers.xaml", UriKind.Relative));
                }
            }
            catch (Exception)
            {

                _errors++;
            }
        }

        private void btnSettings_Click(object sender, RoutedEventArgs e)
        {
            if (_busy) return;
            BPL.Items.Clear();
            Absa.Items.Clear();
            LaLiga.Items.Clear();
            SerieA.Items.Clear();
            Bundesliga.Items.Clear();
            League1.Items.Clear();
            NavigationService.Navigate(new Uri("/OtherPages/Settings.xaml", UriKind.Relative));
        }
    }
}