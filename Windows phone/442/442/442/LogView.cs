using System;
using System.Globalization;
using System.Windows.Controls;
using System.Windows.Media;
using _442.Api;
using _442.Api.Models;
using System.Collections.Generic;
using System.Windows.Media.Imaging;
using Microsoft.Phone.Controls;

namespace _442
{
    public class LogView : Grid, IView
    {

        #region Properties
        private Competition myCompetition;
        public List<LogItem> MyContent { get; set; }

        #endregion Properties

        #region Constructor

        public LogView(List<LogItem> log, Competition cmp)
        {
            MyContent = log;
            Tap += App.Handler;
            myCompetition = cmp;
            Log(log,true);
        }
        public LogView(List<LogItem> log, Competition cmp, bool Noheader)
        {
            Tap += App.Handler;
            myCompetition = cmp;
            Log(log, false);
        }
        #endregion Constructor

        #region Methods

        private void Log(List<LogItem> logL, bool header)
        {
            var log = new Grid {Height = 232, Width = 420, VerticalAlignment = System.Windows.VerticalAlignment.Top};

            log.Children.Add(LogBackground());
            log.Children.Add(Rows());
            log.Children.Add(Columns());
            log.Children.Add(Info(logL, header));
            Children.Add(log);
        }
        private Grid LogBackground()
        {
            var grid = new Grid {Background = GetBackGround("RawBackground")};
            return grid;
        }
        private ImageBrush GetBackGround(string name)
        {
            var bi = new ImageBrush();
            var uri = new Uri(@"../Images/Styling/" + name + ".png", UriKind.Relative);
            bi.ImageSource = new BitmapImage(uri);
            return bi;
        }
        private WrapPanel Rows()
        {
            var panel = new WrapPanel();

            var one = GetNakedRowGrid();
            one.Background = GetBackGround("RawBackground");
            panel.Children.Add(one);

            panel.Children.Add(GetNakedRowGrid());

            var two = GetNakedRowGrid();
            two.Background = GetBackGround("RawBackground");
            panel.Children.Add(two);

            panel.Children.Add(GetNakedRowGrid());

            var three = GetNakedRowGrid();
            three.Background = GetBackGround("RawBackground");
            panel.Children.Add(three);


            return panel;
        }
        private Grid GetNakedRowGrid()
        {
            var grid = new Grid {Width = 420, Height = 46};
            return grid;
        }
        private WrapPanel Columns()
        {
            var panel = new WrapPanel();

            var teamNames = new Grid {Height = 232, Width = 200};
            panel.Children.Add(teamNames);

            var one = new Grid
            {
                Height = 232,
                Width = 55,
                VerticalAlignment = System.Windows.VerticalAlignment.Top,
                Background = GetBackGround("RawBackground")
            };
            panel.Children.Add(one);
            panel.Children.Add(new Grid { Height = 232, Width = 55, VerticalAlignment = System.Windows.VerticalAlignment.Top });
            var two = new Grid
            {
                Height = 232,
                Width = 55,
                VerticalAlignment = System.Windows.VerticalAlignment.Top,
                Background = GetBackGround("RawBackground")
            };
            panel.Children.Add(two);
            
            return panel;
        }
        WrapPanel Info(List<LogItem> items, bool header)
        {
            var panel = new WrapPanel();
            if(header)
            panel.Children.Add(Header());

            for (var i = 0; i < 4; i++)
                panel.Children.Add(GetInfo(items[i]));

            return panel;
        }
        private WrapPanel Header()
        {
            var panel = new WrapPanel { Width =420, Height = 46};

            var log = new Grid { Width = 200, Height = 46 };
            var block = new TextBlock { Foreground = new SolidColorBrush(Colors.White), 
                VerticalAlignment = System.Windows.VerticalAlignment.Center, Text = "LOG" };
            log.Children.Add(block);
            panel.Children.Add(log);
            panel.Children.Add(Content("P"));
            panel.Children.Add(Content("W"));
            panel.Children.Add(Content("GD"));
            panel.Children.Add(Content("Pts"));
            return panel;
        }
        private Grid Content(string value)
        {
            var log = new Grid { Width = 55, Height = 46 };
            var block = new TextBlock { Foreground = new SolidColorBrush(Colors.White),
               HorizontalAlignment = System.Windows.HorizontalAlignment.Center, VerticalAlignment 
               = System.Windows.VerticalAlignment.Center, Text = value };
            log.Children.Add(block);
            return log;
        }
        private WrapPanel GetInfo(LogItem item)
        {
            var panel = new WrapPanel { Width = 420, Height = 46 };

            if (item != null)
            {
                var log = new Grid {Width = 200, Height = 46};
                var block = new TextBlock
                {
                    Foreground = new SolidColorBrush(Colors.White),
                    VerticalAlignment = System.Windows.VerticalAlignment.Center,
                    Text = item.TeamName
                };
                log.Children.Add(block);
                panel.Children.Add(log);
                panel.Children.Add(Content(item.GamesPlayed.ToString(CultureInfo.InvariantCulture)));
                panel.Children.Add(Content(item.GamesWon.ToString(CultureInfo.InvariantCulture)));
                panel.Children.Add(Content(item.GoalDifference.ToString(CultureInfo.InvariantCulture)));
                panel.Children.Add(Content(item.Points.ToString(CultureInfo.InvariantCulture)));

            }
            else
            {
                panel.Height = 0;
            }
            return panel;
        }
        #endregion Methods

        public Competition GetCompetition()
        {
            return myCompetition;
        }

        public ViewType GetViewType()
        {
            return ViewType.Log;
        }
    }
    public class ScorerView : Grid, IView
    {

        #region Properties
        private Competition myCompetition;
        public List<TopGoalScorer> MyContent { get; set; }
        #endregion Properties

        #region Constructor

        public ScorerView(List<TopGoalScorer> log, Competition cmp)
        {
            MyContent = log;
            Tap += App.Handler;
            myCompetition = cmp;
            Log(log,true);
        }
        public ScorerView(List<TopGoalScorer> log, Competition cmp, bool header)
        {
            MyContent = log;
            Tap += App.Handler;
            myCompetition = cmp;
            Log(log,false);
        }
        #endregion Constructor

        #region Methods

        private void Log(List<TopGoalScorer> logL, bool header)
        {
            var log = new Grid { Height = 232, Width = 420, VerticalAlignment = System.Windows.VerticalAlignment.Top };

            log.Children.Add(LogBackground());
            log.Children.Add(Rows());
            //log.Children.Add(Columns());
            log.Children.Add(Info(logL, header));
            Children.Add(log);
        }
        private Grid LogBackground()
        {
            var grid = new Grid { Background = GetBackGround("RawBackground") };
            return grid;
        }
        private ImageBrush GetBackGround(string name)
        {
            var bi = new ImageBrush();
            var uri = new Uri(@"../Images/Styling/" + name + ".png", UriKind.Relative);
            bi.ImageSource = new BitmapImage(uri);
            return bi;
        }
        private WrapPanel Rows()
        {
            var panel = new WrapPanel();

            var one = GetNakedRowGrid();
            one.Background = GetBackGround("RawBackground");
            panel.Children.Add(one);

            panel.Children.Add(GetNakedRowGrid());

            var two = GetNakedRowGrid();
            two.Background = GetBackGround("RawBackground");
            panel.Children.Add(two);

            panel.Children.Add(GetNakedRowGrid());

            var three = GetNakedRowGrid();
            three.Background = GetBackGround("RawBackground");
            panel.Children.Add(three);


            return panel;
        }
        private Grid GetNakedRowGrid()
        {
            var grid = new Grid { Width = 420, Height = 46 };
            return grid;
        }
     
        WrapPanel Info(IReadOnlyList<TopGoalScorer> items, bool header)
        {
            var panel = new WrapPanel();

            if(header)
            panel.Children.Add(Header());

            for (var i = 0; i < 4; i++)
                panel.Children.Add(GetInfo(items[i]));

            return panel;
        }
        private Grid Header()
        {
            var panel = new Grid { Width = 420, Height = 46 };

            var log = new Grid { Width = 200, Height = 46 };
            var block = new TextBlock
            {
                Foreground = new SolidColorBrush(Colors.White),
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                Text = "Goal Scorers"
            };
            log.Children.Add(block);
            log.HorizontalAlignment = System.Windows.HorizontalAlignment.Left;
            panel.Children.Add(log);
            var value = Content("Goals");
            value.HorizontalAlignment = System.Windows.HorizontalAlignment.Right;
            panel.Children.Add(value);
            return panel;
        }
        private Grid Content(string value)
        {
            var log = new Grid { Width = 55, Height = 46 };
            var block = new TextBlock
            {
                Foreground = new SolidColorBrush(Colors.White),
                HorizontalAlignment = System.Windows.HorizontalAlignment.Center,
                VerticalAlignment
                    = System.Windows.VerticalAlignment.Center,
                Text = value
            };
            log.Children.Add(block);
            return log;
        }
        private Grid GetInfo(TopGoalScorer item)
        {
            var panel = new Grid { Width = 420, Height = 46 };

            var log = new Grid { Width = 200, Height = 46 };
            var block = new TextBlock
            {
                Foreground = new SolidColorBrush(Colors.White),
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                Text = item.Player
            };
            log.Children.Add(block);
            log.HorizontalAlignment = System.Windows.HorizontalAlignment.Left;
            panel.Children.Add(log);

            var piece = Content(item.Goals.ToString(CultureInfo.InvariantCulture));
            piece.HorizontalAlignment = System.Windows.HorizontalAlignment.Right;
            panel.Children.Add(piece);
            return panel;
        }
        #endregion Methods

        public Competition GetCompetition()
        {
            return myCompetition;
        }

        public ViewType GetViewType()
        {
            return ViewType.Score;
        }
    }
    public class LiveView : Grid, IView
    {

        #region Properties
        private Competition myCompetition;
        public List<Live> MyContent; 
        #endregion Properties

        #region Constructor

        public LiveView( List<Live> game, Competition cmp)
        {
            MyContent = game;
            Tap += App.Handler;
            myCompetition = cmp;
            Log(game[0],true);
        }
        public LiveView(Live game, Competition cmp)
        {
            MyContent = new List<Live>(new[] { game });
            Tap += App.Handler;
            myCompetition = cmp;
            Log(game, false);
        }
        public LiveView(Live game, Competition cmp, string matchStatus)
        {
            MyContent = new List<Live>(new[] { game });
            Tap += App.Handler;
            myCompetition = cmp;
            Log(game, matchStatus);
        }
        #endregion Constructor

        #region Methods

        private void Log( Live logL, bool header)
        {
            var log = new Grid { Height = 160, Width = 420, VerticalAlignment = System.Windows.VerticalAlignment.Top };

            log.Children.Add(LogBackground());
            log.Children.Add(Rows());
            log.Children.Add(Header(header));
            log.Children.Add(GetInfo( logL));
            Children.Add(log);
        }
        private void Log(Live logL, string matchStatus)
        {
            var log = new Grid { Height = 160, Width = 420, VerticalAlignment = System.Windows.VerticalAlignment.Top };

            log.Children.Add(LogBackground());
            log.Children.Add(Rows());
            log.Children.Add(Header(matchStatus));
            log.Children.Add(GetInfo(logL));
            Children.Add(log);
        }
        private Grid LogBackground()
        {
            var grid = new Grid { Background = GetBackGround("RawBackground") };
            return grid;
        }
        private ImageBrush GetBackGround(string name)
        {
            var bi = new ImageBrush();
            var uri = new Uri(@"../Images/Styling/" + name + ".png", UriKind.Relative);
            bi.ImageSource = new BitmapImage(uri);
            return bi;
        }
        private WrapPanel Rows()
        {
            var panel = new WrapPanel();

            var one = GetNakedRowGrid();
            one.Background = GetBackGround("RawBackground");
            panel.Children.Add(one);


            return panel;
        }
        private Grid GetNakedRowGrid()
        {
            var grid = new Grid { Width = 420, Height = 46 };
            return grid;
        }

        private Grid Header(bool header)
        {
            var panel = new Grid { Width = 420, Height = 46 };

            var log = new Grid { Width = 200, Height = 46 };
            var block = new TextBlock
            {
                Foreground = new SolidColorBrush(Colors.White),
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                Text = "LIVE"
            };
            if (header)
            {
                log.Children.Add(block);
                log.HorizontalAlignment = System.Windows.HorizontalAlignment.Left;
                panel.Children.Add(log);
                var value = Content("More Games");
                value.HorizontalAlignment = System.Windows.HorizontalAlignment.Right;
                panel.Children.Add(value);
                panel.VerticalAlignment = System.Windows.VerticalAlignment.Top;
            }
            return panel;
        }
        private Grid Header(string matchStatus)
        {
            var panel = new Grid { Width = 420, Height = 46 };

            var log = new Grid { Width = 200, Height = 46 };
            var block = new TextBlock
            {
                Foreground = new SolidColorBrush(Colors.White),
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                Text = "LIVE"
            };
           
                log.Children.Add(block);
                log.HorizontalAlignment = System.Windows.HorizontalAlignment.Left;
                panel.Children.Add(log);
                var value = Content(matchStatus);
                value.HorizontalAlignment = System.Windows.HorizontalAlignment.Right;
                panel.Children.Add(value);
                panel.VerticalAlignment = System.Windows.VerticalAlignment.Top;
            
            return panel;
        }
        private Grid Content(string value)
        {
            var log = new Grid {Height = 46 };
            var block = new TextBlock
            {
                Foreground = new SolidColorBrush(Colors.White),
                HorizontalAlignment = System.Windows.HorizontalAlignment.Center,
                VerticalAlignment
                    = System.Windows.VerticalAlignment.Center,
                Text = value
            };
            log.Children.Add(block);
            return log;
        }
        private Grid GetInfo( Live item)
        {
            var panel = new Grid
            {
                Width = 420,
                Height = 120,
                VerticalAlignment = System.Windows.VerticalAlignment.Bottom
            };
            panel.Children.Add(TeamOne( item.HomeTeamName));
            panel.Children.Add((GetScore(item.HomeTeamScore,item.AwayTeamScore)));
            panel.Children.Add(TeamTwo( item.AwayTeamName));

            return panel;
        }
        private ImageBrush GetTeamLogo(string name)
        {
            var bi = new ImageBrush();
            var uri = new Uri(@"../Images/TeamLogo/" + name + ".png", UriKind.Relative);
            bi.ImageSource = new BitmapImage(uri);
            return bi;
        }

        private Grid TeamOne(string name)
        {
            var grid = new Grid {Width = 140, HorizontalAlignment = System.Windows.HorizontalAlignment.Left};
            var inner = new Grid
            {
                Background = GetTeamLogo( name),
                Width = 100,
                Height = 100,
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                HorizontalAlignment = System.Windows.HorizontalAlignment.Center
            };
            grid.Children.Add(inner);
            return grid;
        }
        private Grid TeamTwo( string name)
        {
            var grid = new Grid { Width = 140, HorizontalAlignment = System.Windows.HorizontalAlignment.Right };
            var inner = new Grid
            {
                Background = GetTeamLogo( name),
                Width = 100,
                Height = 100,
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                HorizontalAlignment = System.Windows.HorizontalAlignment.Center
            };
            grid.Children.Add(inner);
            return grid;
        }

        private Grid GetScore(int one, int two)
        {
            var grid = new Grid { Width = 140, HorizontalAlignment = System.Windows.HorizontalAlignment.Center };
            var block = new TextBlock
            {
                HorizontalAlignment = System.Windows.HorizontalAlignment.Center,
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                FontSize = 60,
                Text = one + " : " + two
            };
            grid.Children.Add(block);

            return grid;
        }
        #endregion Methods

        public Competition GetCompetition()
        {
            return myCompetition;
        }

        public ViewType GetViewType()
        {
            return ViewType.Live;
        }
    }
    public class FixtureView : Grid, IView
    {

        #region Properties
        private Competition myCompetition;
        #endregion Properties

        #region Constructor

        public FixtureView( Fixture game, Competition cmp)
        {
            Tap += App.Handler;
            myCompetition = cmp;
            if(game != null)
            Log( game);
        }

        #endregion Constructor

        #region Methods

        private void Log( Fixture logL)
        {
            var log = new Grid { Height = 160, Width = 420, VerticalAlignment = System.Windows.VerticalAlignment.Top };

            log.Children.Add(LogBackground());
            log.Children.Add(Rows());
            log.Children.Add(Header(logL));
            log.Children.Add(GetInfo(logL));
            Children.Add(log);
        }
        private Grid LogBackground()
        {
            var grid = new Grid { Background = GetBackGround("RawBackground") };
            return grid;
        }
        private ImageBrush GetBackGround(string name)
        {
            var bi = new ImageBrush();
            var uri = new Uri(@"../Images/Styling/" + name + ".png", UriKind.Relative);
            bi.ImageSource = new BitmapImage(uri);
            return bi;
        }
        private WrapPanel Rows()
        {
            var panel = new WrapPanel();

            var one = GetNakedRowGrid();
            one.Background = GetBackGround("RawBackground");
            panel.Children.Add(one);


            return panel;
        }
        private Grid GetNakedRowGrid()
        {
            var grid = new Grid { Width = 420, Height = 46 };
            return grid;
        }

        private Grid Header(Fixture item)
        {
            var panel = new Grid { Width = 420, Height = 46 };

            var log = new Grid { Width = 200, Height = 46 };
            var block = new TextBlock
            {
                Foreground = new SolidColorBrush(Colors.White),
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                Text = "FIXTURES"
            };
            log.Children.Add(block);
            log.HorizontalAlignment = System.Windows.HorizontalAlignment.Left;
            panel.Children.Add(log);
            var value = Content(item.FixtureDate.ToShortDateString());
            value.HorizontalAlignment = System.Windows.HorizontalAlignment.Right;
            panel.Children.Add(value);
            panel.VerticalAlignment = System.Windows.VerticalAlignment.Top;
            return panel;
        }
        private Grid Content(string value)
        {
            var log = new Grid { Height = 46 };
            var block = new TextBlock
            {
                Foreground = new SolidColorBrush(Colors.White),
                HorizontalAlignment = System.Windows.HorizontalAlignment.Center,
                VerticalAlignment
                    = System.Windows.VerticalAlignment.Center,
                Text = value
            };
            log.Children.Add(block);
            return log;
        }
        private Grid GetInfo( Fixture item)
        {
            var panel = new Grid
            {
                Width = 420,
                Height = 120,
                VerticalAlignment = System.Windows.VerticalAlignment.Bottom
            };
            panel.Children.Add(TeamOne(item.HomeTeamName));
            panel.Children.Add((GetScore()));
            panel.Children.Add(TeamTwo(item.AwayTeamName));

            return panel;
        }
        private ImageBrush GetTeamLogo(string name)
        {
            var bi = new ImageBrush();
            var uri = new Uri(@"../Images/TeamLogo/" + name + ".png", UriKind.Relative);
            bi.ImageSource = new BitmapImage(uri);
            return bi;
        }

        private Grid TeamOne( string name)
        {
            var grid = new Grid { Width = 140, HorizontalAlignment = System.Windows.HorizontalAlignment.Left };
            var inner = new Grid
            {
                Background = GetTeamLogo( name),
                Width = 100,
                Height = 100,
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                HorizontalAlignment = System.Windows.HorizontalAlignment.Center
            };
            grid.Children.Add(inner);
            return grid;
        }
        private Grid TeamTwo( string name)
        {
            var grid = new Grid { Width = 140, HorizontalAlignment = System.Windows.HorizontalAlignment.Right };
            var inner = new Grid
            {
                Background = GetTeamLogo(name),
                Width = 100,
                Height = 100,
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                HorizontalAlignment = System.Windows.HorizontalAlignment.Center
            };
            grid.Children.Add(inner);
            return grid;
        }

        private Grid GetScore()
        {
            var grid = new Grid { Width = 140, HorizontalAlignment = System.Windows.HorizontalAlignment.Center };
            var block = new TextBlock
            {
                HorizontalAlignment = System.Windows.HorizontalAlignment.Center,
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                FontSize = 60,
                Text = "VS"
            };
            grid.Children.Add(block);

            return grid;
        }
        #endregion Methods

        public Competition GetCompetition()
        {
            return myCompetition;
        }

        public ViewType GetViewType()
        {
           return ViewType.Fixture;
        }
    }

    public class ResultView : Grid, IView
    {

        #region Properties
        private Competition myCompetition;
        public List<Result> MyContent { get; set; } 
        #endregion Properties

        #region Constructor

        public ResultView(List<Result> game, Competition cmp)
        {
            MyContent = game;
            Tap += App.Handler;
            myCompetition = cmp;
            Log(game[0]);
        }
        public ResultView(Result game, Competition cmp, bool noHeader)
        {

            myCompetition = cmp;
            Log(game, noHeader);
        }
        #endregion Constructor

        #region Methods

        private void Log(Result logL)
        {
            var log = new Grid { Height = 160, Width = 420, VerticalAlignment = System.Windows.VerticalAlignment.Top };

            log.Children.Add(LogBackground());
            log.Children.Add(Rows());
            log.Children.Add(Header(logL, false));
            log.Children.Add(GetInfo(logL));
            Children.Add(log);
        }
        private void Log(Result logL, bool noHeader)
        {
            var log = new Grid { Height = 160, Width = 420, VerticalAlignment = System.Windows.VerticalAlignment.Top };

            log.Children.Add(LogBackground());
            log.Children.Add(Rows());
            log.Children.Add(Header(logL, noHeader));
            log.Children.Add(GetInfo(logL));
            Children.Add(log);
        }
        private Grid LogBackground()
        {
            var grid = new Grid { Background = GetBackGround("RawBackground") };
            return grid;
        }
        private ImageBrush GetBackGround(string name)
        {
            var bi = new ImageBrush();
            var uri = new Uri(@"../Images/Styling/" + name + ".png", UriKind.Relative);
            bi.ImageSource = new BitmapImage(uri);
            return bi;
        }
        private WrapPanel Rows()
        {
            var panel = new WrapPanel();

            var one = GetNakedRowGrid();
            one.Background = GetBackGround("RawBackground");
            panel.Children.Add(one);


            return panel;
        }
        private Grid GetNakedRowGrid()
        {
            var grid = new Grid { Width = 420, Height = 46 };
            return grid;
        }

        WrapPanel Info(Fixture items)
        {
            var panel = new WrapPanel();
            //panel.Children.Add(Header(items));

            //for (var i = 0; i < 4; i++)
            //panel.Children.Add(GetInfo(items[i]));

            return panel;
        }
        private Grid Header(Result item, bool noHeader)
        {
            var panel = new Grid { Width = 420, Height = 46 };

            var log = new Grid { Width = 200, Height = 46 };
            var block = new TextBlock
            {
                Foreground = new SolidColorBrush(Colors.White),
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                Text = "RESULTS"
            };
            log.Children.Add(block);
            log.HorizontalAlignment = System.Windows.HorizontalAlignment.Left;
            if(!noHeader)
            panel.Children.Add(log);
            var value = Content(item.Date);
            value.HorizontalAlignment = System.Windows.HorizontalAlignment.Right;
            panel.Children.Add(value);
            panel.VerticalAlignment = System.Windows.VerticalAlignment.Top;
            return panel;
        }
        private Grid Content(string value)
        {
            var log = new Grid { Height = 46 };
            var block = new TextBlock
            {
                Foreground = new SolidColorBrush(Colors.White),
                HorizontalAlignment = System.Windows.HorizontalAlignment.Center,
                VerticalAlignment
                    = System.Windows.VerticalAlignment.Center,
                Text = value
            };
            log.Children.Add(block);
            return log;
        }
        private Grid GetInfo(Result item)
        {
            var panel = new Grid
            {
                Width = 420,
                Height = 120,
                VerticalAlignment = System.Windows.VerticalAlignment.Bottom
            };
            panel.Children.Add(TeamOne(item.HomeTeamName));
            panel.Children.Add((GetScore(item.HomeTeamScore, item.AwayTeamScore)));
            panel.Children.Add(TeamTwo(item.AwayTeamName));

            return panel;
        }
        private ImageBrush GetTeamLogo(string name)
        {
            var bi = new ImageBrush();
            var uri = new Uri(@"../Images/TeamLogo/" + name + ".png", UriKind.Relative);
            bi.ImageSource = new BitmapImage(uri);
            return bi;
        }

        private Grid TeamOne(string name)
        {
            var grid = new Grid { Width = 140, HorizontalAlignment = System.Windows.HorizontalAlignment.Left };
            var inner = new Grid
            {
                Background = GetTeamLogo(name),
                Width = 100,
                Height = 100,
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                HorizontalAlignment = System.Windows.HorizontalAlignment.Center
            };
            grid.Children.Add(inner);
            return grid;
        }
        private Grid TeamTwo(string name)
        {
            var grid = new Grid { Width = 140, HorizontalAlignment = System.Windows.HorizontalAlignment.Right };
            var inner = new Grid
            {
                Background = GetTeamLogo(name),
                Width = 100,
                Height = 100,
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                HorizontalAlignment = System.Windows.HorizontalAlignment.Center
            };
            grid.Children.Add(inner);
            return grid;
        }

        private Grid GetScore(int one, int two)
        {
            var grid = new Grid { Width = 140, HorizontalAlignment = System.Windows.HorizontalAlignment.Center };
            var block = new TextBlock
            {
                HorizontalAlignment = System.Windows.HorizontalAlignment.Center,
                VerticalAlignment = System.Windows.VerticalAlignment.Center,
                FontSize = 60,
                Text = one + " : " + two
            };
            grid.Children.Add(block);

            return grid;
        }
        #endregion Methods

        public Competition GetCompetition()
        {
            return myCompetition;
        }

        public ViewType GetViewType()
        {
            return ViewType.Result;
        }
    }
}
