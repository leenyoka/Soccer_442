using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Net.NetworkInformation;
using _442.Api;
using GestureEventArgs = System.Windows.Input.GestureEventArgs;

namespace _442.OtherPages
{
    public partial class Live
    {
        public Live()
        {
            InitializeComponent();
            CompetitionName.Text = App.SelectedCompetition.ToString();
        }
        private async Task<int> LoadResults()
        {


            List<_442.Api.Models.Live> english = null;

            var resultView = (LiveView)App.SelectedList;

            if (resultView.MyContent.Count > 1)
            {

                foreach (var result in resultView.MyContent)
                {

                    LiveList.Items.Add(new Grid {Width = 40, Height = 13});
                    var game = new LiveView(result, App.SelectedCompetition);
                    game.Tap += new EventHandler<GestureEventArgs>(GameSelected);
                    LiveList.Items.Add(game);
                }
            }
            else
            {
                GameSelected(new LiveView(resultView.MyContent, App.SelectedCompetition), null);
            }

            return 1;
        }

        private async void PhoneApplicationPage_Loaded(object sender, RoutedEventArgs e)
        {
            if (DeviceNetworkInformation.IsNetworkAvailable)
            await LoadResults();
            else MessageBox.Show("You are not connected.", "Connection Needed", MessageBoxButton.OK);
        }
        readonly SuperSport _superSport = new SuperSport();
        private async void GameSelected(object sender, RoutedEventArgs e)
        {
            Progress.Visibility = Visibility.Visible;
            var gameInfoView = (LiveView) sender;

            var matchDetails = await _superSport.GetMatchDetails(gameInfoView.MyContent[0], App.SelectedCompetition,
                gameInfoView.MyContent[0].MatchId);

            ContentPanel2.Visibility = Visibility.Visible;
            ContentPanel.Visibility = Visibility.Collapsed;

            LiveList2.Items.Clear();
            var game = new LiveView(gameInfoView.MyContent[0], App.SelectedCompetition, matchDetails.MatchStatus);
            MatchInfo.Children.Add(game);

            foreach (var comment in matchDetails.Commentry)
            {
                LiveList2.Items.Add(new Grid{Height = 10});

                var block = new TextBlock
                {
                    Foreground = new SolidColorBrush(Colors.White),
                    Text = comment.Time + " : " + comment.Text,
                    TextWrapping = TextWrapping.Wrap
                };

                var panel = new WrapPanel {Width = 420,Background = GetBackGround("RawBackground")};

                if(comment.Text.Contains("Goal!"))
                    panel.Background = new SolidColorBrush(Colors.Green);
                panel.Children.Add(block);
                LiveList2.Items.Add((panel));
            }
            Progress.Visibility = Visibility.Collapsed;
            // 
        }
        private ImageBrush GetBackGround(string name)
        {
            var bi = new ImageBrush();
            var uri = new Uri(@"../Images/Styling/" + name + ".png", UriKind.Relative);
            bi.ImageSource = new BitmapImage(uri);
            return bi;
        }
        private void PhoneApplicationPage_BackKeyPress(object sender, System.ComponentModel.CancelEventArgs e)
        {
            if (ContentPanel2.Visibility == Visibility.Visible && LiveList.Items.Count > 0)
            {
                ContentPanel2.Visibility = Visibility.Collapsed;
                ContentPanel.Visibility = Visibility.Visible;
                e.Cancel = true;
            }
        }


    }
}