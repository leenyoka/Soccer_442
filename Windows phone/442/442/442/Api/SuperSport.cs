using System;
using System.Linq;
using System.Threading.Tasks;
using System.Collections.Generic;
using _442.Api.Models;

namespace _442.Api
{
    public class SuperSport
    {
        readonly AppConfig _uriProvider = new AppConfig();
        public async Task<Live> GetMatchDetails(Live game, Competition competition, long matchId)
        {
            var dog = new WebDog();
            var value = await dog.Fetch(_uriProvider.GetMatchDetails(competition, matchId));
            return Commentry(game, value);
        }

        public async Task<List<Fixture>> GetFixture(Competition competition)
        {
            var dog = new WebDog();
            var value = await dog.Fetch(_uriProvider.GetFixtureUri(competition));
            return Fixtures(value);
        }

        public async Task<List<Result>> GetResults(Competition competition)
        {
            var dog = new WebDog();
            var value = await dog.Fetch(_uriProvider.GetResultsUri(competition));
            return Results(value);

        }

        public async Task<List<TopGoalScorer>> GetScorers(Competition competition)
        {
            var dog = new WebDog();
            var value = await dog.Fetch(_uriProvider.GetScorersUr(competition));
            return Scorers(value);
        }

        public async Task<List<LogItem>> GetLog(Competition competition)
        {
            var dog = new WebDog();
            var value = await dog.Fetch(_uriProvider.GetLogUri(competition));
                return Logs(value);
        }

        public async Task<List<Live>> GetLive(Competition competition)
        {
            var dog = new WebDog();
            var values = await dog.Fetch(_uriProvider.GetLiveUri(competition));
            return Live(values);
        }

        private List<LogItem> Logs(string htmlTable)
        {
            var startingIndex = htmlTable.IndexOf("<table", StringComparison.Ordinal) - 1;
            htmlTable = htmlTable.Substring(startingIndex);
            var endIndex = htmlTable.IndexOf("</table>", StringComparison.Ordinal);
            htmlTable = htmlTable.Substring(0, endIndex + 8);

            var answer = GetRows(htmlTable);
            var table = GetRowsWithCols(answer);

            return Logs(table);
        }
        private List<LogItem> Logs(IEnumerable<List<string>> pieces)
        {
            var log = new List<LogItem>();

            foreach (var rawLogItem in pieces)
            {
                int value;

                if (int.TryParse(rawLogItem[1], out value))
                {
                    log.Add(new LogItem(rawLogItem[0], int.Parse(rawLogItem[1]),
                        int.Parse(rawLogItem[2]), int.Parse(rawLogItem[3]), int.Parse(rawLogItem[4])));
                }
            }

            return log;
        }
        private List<Result> Results(string htmlTable)
        {
            htmlTable = htmlTable.Substring(htmlTable.IndexOf("<table", StringComparison.Ordinal));
            htmlTable = htmlTable.Substring(0, htmlTable.LastIndexOf("</table", StringComparison.Ordinal) + 8);
            htmlTable = htmlTable.Replace("<br />", "");
            htmlTable = htmlTable.Substring(10);
            htmlTable = htmlTable.Substring(0, htmlTable.Length - 10);

            var tables = GetTableContent("<table", "</table>", htmlTable);

            var results = tables.Select(table => GetRowsWithCols(GetRows(table))).ToList();


            return REsultsFromList(results);

        }
        private List<Result> REsultsFromList(IEnumerable<List<List<string>>> values)
        {
            return (from rawResult in values where rawResult.Count > 2 
                    let homeScorers = (rawResult[2].Count > 0) ? rawResult[2][0] : "" 
                    let awayScorers = (rawResult[2].Count > 2) ? rawResult[2][2] : "" 
                    let v = rawResult[1][1] select new Result(rawResult[1][0], rawResult[1][2], 
                        int.Parse(v.Split('-')[0]), int.Parse(v.Split('-')[1]), rawResult[0][0], 
                        homeScorers, awayScorers)).ToList();
        }

        private List<Fixture> Fixtures(string htmlTable)
        {
            var raw = htmlTable;
            htmlTable = htmlTable.Substring(htmlTable.IndexOf("<table", StringComparison.Ordinal));
            htmlTable = htmlTable.Substring(0, htmlTable.LastIndexOf("</table", StringComparison.Ordinal) + 8);
            htmlTable = htmlTable.Replace("<br />", "");
            htmlTable = htmlTable.Substring(10);
            htmlTable = htmlTable.Substring(0, htmlTable.Length - 10);

            var tables = GetTableContent("<table", "</table>", htmlTable);

            var results = tables.Select(table => GetRowsWithCols(GetRows(table))).ToList();

            return FixtureFromList(results, raw);
        }
        private List<Fixture> FixtureFromList(IEnumerable<List<List<string>>> list, string raw)
        {
            var fixtures = new List<Fixture>();
            var nextFixtureDate = DateTime.MinValue;
            raw = raw.Substring(raw.IndexOf("<table", StringComparison.Ordinal));
            raw = raw.Substring(0, raw.IndexOf("</table>", StringComparison.Ordinal) + 8);
            raw = GetRowsWithCols(GetRows(raw))[0][0];

            if (IsDate(raw))
                nextFixtureDate = Convert.ToDateTime(raw);

            foreach (var subList in list)
            {
                if (subList.Count == 1 && subList[0].Count == 1 && IsDate(subList[0][0]))
                {
                    nextFixtureDate = Convert.ToDateTime(subList[0][0]);
                }
                else if (subList.Count > 0 && subList[0].Count > 2)
                {
                    fixtures.AddRange(subList.Select(rawFixture => new Fixture(rawFixture[0], 
                        rawFixture[2], nextFixtureDate, rawFixture[3], rawFixture[4])));
                }
            }


            return fixtures;

        }
        private bool IsDate(string value)
        {
            try
            {
                Convert.ToDateTime(value);
                return true;
            }
            catch
            {
                return false;
            }
        }
        private List<Live> Live(string content)
        {

            var rawLive = new List<string>();

            while (content.IndexOf("<div id=\"footballlivescoringone\">", StringComparison.Ordinal) != -1)
            {
                var startingIndex = content.IndexOf("<div id=\"footballlivescoringone\">", StringComparison.Ordinal);

                var endIndex = FindMatchingTagIndex(startingIndex, "<div", "</div>", content);

                if (endIndex == -1) continue;
                rawLive.Add(content.Substring(startingIndex, endIndex - startingIndex));

                content = content.Substring(endIndex + 6);
            }

            return Live(rawLive);
        }
        private Live Commentry(Live game, string content)
        {

            var rawLive = new List<string>();

            while (content.IndexOf("<div id=\"match\">", StringComparison.Ordinal) != -1)
            {
                int startingIndex = content.IndexOf("<div id=\"match\">", StringComparison.Ordinal);

                int endIndex = FindMatchingTagIndex(startingIndex, "<div", "</div>", content);

                if (endIndex != -1)
                {
                    rawLive.Add(content.Substring(startingIndex, endIndex - startingIndex));

                    content = content.Substring(endIndex + 6);
                }
            }

            if (rawLive.Count <= 0) return null;
            var matchCom = GetTableContent("<div", "</div>", rawLive[0]);

            var home = matchCom[matchCom.Count - 1].Substring(0, matchCom[matchCom.Count - 1].IndexOf("</div", StringComparison.Ordinal));
            home = home.Substring(home.LastIndexOf(">", StringComparison.Ordinal) + 1);

            if (game != null)
                game.MatchStatus = home;

            game = GetScorers(game, matchCom);

            game.Commentry = Comments(content);

            return game;


            //return Live(rawLive);
        }
        private Live GetScorers(Live game, IEnumerable<string> rawData)
        {
            foreach (var value in rawData)
            {
                if (value.Replace("team1infoholder", "").IndexOf("team1info", StringComparison.Ordinal) != -1)
                {
                    var home = value.Substring(0, value.IndexOf("</div", StringComparison.Ordinal));
                    home = home.Substring(home.Replace("<br />", "").LastIndexOf(">", StringComparison.Ordinal) + 1);

                    if (game != null)
                        game.HomeTeamGoalScorers = GetScorers(home);
                }

                if (value.Replace("team2infoholder", "").IndexOf("team2info", StringComparison.Ordinal) != -1)
                {
                    var home = value.Substring(0, value.IndexOf("</div", StringComparison.Ordinal));
                    home = home.Substring(home.Replace("<br />", "").LastIndexOf(">", StringComparison.Ordinal) + 1);

                    if (game != null)
                        game.AwayTeamGoalScorers = GetScorers(home);
                }
            }



            return game;
        }
        private List<string> GetScorers(string value)
        {
            value = value.Replace("<br />", "");
            var values = value.Split(')');

            return (from myValue in values where myValue.Trim().Length > 3 
                    select myValue.Trim().Trim(',') + ")").ToList();
        }
        private List<Comment> Comments(string rawData)
        {
            //<div id="comm">
            var comments = new List<Comment>();

            int startingIndex = rawData.IndexOf("<div id=\"comm\">", StringComparison.Ordinal);

            int endIndex = FindMatchingTagIndex(startingIndex, "<div", "</div>", rawData);

            if (endIndex != -1)
            {
                rawData = rawData.Substring(startingIndex, endIndex - startingIndex);

                var pieces = GetTableContent("<div", "</div", rawData);

                for (int i = 0; i < pieces.Count - 1; i++)
                {
                    var time = pieces[i].Substring(0, pieces[i].IndexOf("</div", StringComparison.Ordinal));
                    time = time.Substring(time.LastIndexOf(">", StringComparison.Ordinal) + 1);

                    var txt = pieces[i + 1].Substring(0, pieces[i + 1].IndexOf("</div", StringComparison.Ordinal));
                    txt = txt.Substring(txt.LastIndexOf(">", StringComparison.Ordinal) + 1);

                    comments.Add(new Comment(time, txt));
                    i++;
                }

            }
            return comments;

        }
        private List<Live> Live(IEnumerable<string> rawLive)
        {
            var liveGames = new List<Live>();

            foreach (string rawGame in rawLive)
            {
                var value = GetTableContent("<div", "</div>", rawGame);

                var home = value[2].Substring(0, value[2].IndexOf("</div", StringComparison.Ordinal));
                home = home.Substring(home.LastIndexOf(">", StringComparison.Ordinal) + 1);

                var homeScoreLine = value[3].Substring(0, value[3].LastIndexOf("</div", StringComparison.Ordinal));
                homeScoreLine = homeScoreLine.Substring(homeScoreLine.LastIndexOf(">", StringComparison.Ordinal) + 1);

                var away = value[5].Substring(0, value[5].LastIndexOf("</div", StringComparison.Ordinal));
                away = away.Substring(away.LastIndexOf(">", StringComparison.Ordinal) + 1);

                var awayScoreLine = value[6].Substring(0, value[6].LastIndexOf("</div", StringComparison.Ordinal));
                awayScoreLine = awayScoreLine.Substring(awayScoreLine.LastIndexOf(">", StringComparison.Ordinal) + 1);


                var startingIndex = value[7].IndexOf("match/", StringComparison.Ordinal) + 6;
                var endIndex = value[7].IndexOf("Live Scoring", StringComparison.Ordinal) - 2;
                long matchId = long.Parse(value[7].Substring(startingIndex, endIndex - startingIndex));

                liveGames.Add(new Live(home, away, int.Parse(homeScoreLine), int.Parse(awayScoreLine), matchId));
            }

            return liveGames;
        }
        private List<TopGoalScorer> Scorers(string htmlTable)
        {

            htmlTable = htmlTable.Substring(htmlTable.IndexOf("<table", StringComparison.Ordinal));
            htmlTable = htmlTable.Substring(0, htmlTable.LastIndexOf("</table", StringComparison.Ordinal) + 8);
            var answer = GetRows(htmlTable);
            var table = GetRowsWithCols(answer);

            var scorers = new List<TopGoalScorer>();

            foreach (List<string> rawScorer in table)
            {
                try
                {
                    scorers.Add(new TopGoalScorer(rawScorer[0], rawScorer[1], int.Parse(rawScorer[2])));
                }
                catch (Exception)
                { }
            }

            return scorers;
        }
        private List<string> GetRows(string table)
        {
            return GetTableContent("<tr", "</tr>", table);
        }
        private List<List<string>> GetRowsWithCols(List<string> rows)
        {
            List<List<string>> answer = new List<List<string>>();

            foreach (string row in rows)
                answer.Add(GetColums(row));

            return answer;
        }
        private List<string> GetColums(string row)
        {
            var value = GetTableContent("<td", "</td>", row);

            for (var i = 0; i < value.Count; i++)
            {
                value[i] = value[i].Substring(0, value[i].IndexOf("</", StringComparison.Ordinal));
                value[i] = value[i].Substring(value[i].LastIndexOf(">", StringComparison.Ordinal) + 1);
            }

            return value;
        }
        private List<string> GetTableContent(string openingTag, string closingTag, string data)
        {
            var rows = new List<string>();

            while (data.IndexOf(openingTag, StringComparison.Ordinal) != -1)
            {
                var rowStart = data.IndexOf(openingTag, StringComparison.Ordinal);
                data = data.Substring(rowStart);
                var rowEnd = data.IndexOf(closingTag, StringComparison.Ordinal);
                rows.Add(data.Substring(0, rowEnd + closingTag.Length));
                data = data.Substring(rowEnd + closingTag.Length);
            }

            return rows;
        }
        public int FindMatchingTagIndex(int startIndex, string tag, string closingTag, string data)
        {
            var match = 0;

            for (var start = startIndex + 1; start < data.Length - closingTag.Length; start++)
            {
                var newTag = data.Substring(start, tag.Length);

                if (newTag == tag)
                    match++;

                var newClosingTag = data.Substring(start, closingTag.Length);

                if (newClosingTag == closingTag)
                {
                    if (match == 0)
                        return start + closingTag.Length;
                    else match--;
                }
            }
            return -1;
        }
    }
}
