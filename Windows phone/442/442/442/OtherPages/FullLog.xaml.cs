using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using Microsoft.Phone.Net.NetworkInformation;
using _442.Api;
using _442.Api.Models;

namespace _442.OtherPages
{
    public partial class FullLog
    {
        public FullLog()
        {
            InitializeComponent();
            CompetitionName.Text = App.SelectedCompetition.ToString();
        }
        private async Task<int> LoadResults()
        {
            var resultView = (LogView)App.SelectedList;
            var first = true;

            var lists = SpitIntoFours(resultView.MyContent);

            foreach (var result in lists)
            {
                if (!first)
                {
                    var piece = new LogView(result, Competition.BPL, true) {Height = 174};
                    LiveList.Items.Add(piece);
                }
                else
                {
                    first = false;
                    LiveList.Items.Add(new LogView(result, Competition.BPL));
                }
            }

            return 1;
        }

        private IEnumerable<List<LogItem>> SpitIntoFours(IReadOnlyList<LogItem> originalLog)
        {
            var lists = new List<List<LogItem>>();

            var upper = (originalLog.Count/4) ;
            var counter = 0;

            while (counter < upper)
            {
                var start = counter*4;
                lists.Add((new[]
                {
                    originalLog[start], originalLog[start + 1],
                    originalLog[start+ 2], originalLog[start + 3]
                }).ToList());

                counter++;
            }

            return lists;
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