using System.Collections.Generic;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using Microsoft.Phone.Net.NetworkInformation;


namespace _442.OtherPages
{
    public partial class Results
    {
        public Results()
        {
            InitializeComponent();

            CompetitionName.Text = App.SelectedCompetition.ToString();
        }

        private async Task<int> LoadResults()
        {


            List<_442.Api.Models.Result> english = null;

            var resultView = (ResultView) App.SelectedList;

            foreach (var result in resultView.MyContent)
                {

                    ResultsList.Items.Add(new Grid {Width = 40, Height = 13});
                    ResultsList.Items.Add(new ResultView(result, App.SelectedCompetition, true));
                }
            
            return 1;
        }

        private async void PhoneApplicationPage_Loaded(object sender, RoutedEventArgs e)
        {
            if (DeviceNetworkInformation.IsNetworkAvailable)
            {
            await LoadResults();
            Progress.Visibility = Visibility.Collapsed;
            }
            else MessageBox.Show("You are not connected.", "Connection Needed", MessageBoxButton.OK);
        }
    }
}