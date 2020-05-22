package com.oms.lindanyoka.soccer_442;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-22.
 */
public class SuperSport {
    AppConfig _uriProvider = new AppConfig();


    public Live GetMatchDetails(Live game, Competition competition, long matchId) {
        try {
            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetMatchDetails(competition, matchId));
            return Commentry(game, value);
        } catch (Exception ex) {

            return null;
        }
    }
    public Result GetMatchDetails(Result game, Competition competition, String matchId) {
        try {
            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetMatchDetails(competition, Long.parseLong(matchId)));
            return Commentry(game, value);
        } catch (Exception ex) {

            return null;
        }
    }

    public ArrayList<Fixture> GetFixture(Competition competition) {
        try {
            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetFixtureUri(competition));
            return Fixtures(value);
        } catch (Exception ex) {

            return null;
        }
    }
    public GameStats GetStats(String matchId)
    {
        try {
            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetStatsUri(matchId));
            return GetStatsFromFile(value);
        } catch (Exception ex) {

            return null;
        }
    }
    public LineUp GetLineUp(String matchId)
    {
        try {
            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetLineUpUri(matchId));
              return LineUp(value);
        } catch (Exception ex) {

            return null;
        }
    }
    public ArrayList<Result> GetResults(Competition competition) {
        try {


            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetResultsUri(competition));
            String mobiValue = dog.Fetch(_uriProvider.GetResultsUri2(competition));
            return Results(value, mobiValue);
        } catch (Exception ex) {

            return null;
        }

    }

    public ArrayList<TopGoalScorer> GetScorers(Competition competition) {
        try {
            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetScorersUr(competition));
            return Scorers(value);
        } catch (Exception ex) {

            return null;
        }
    }

    public ArrayList<LogItem> GetLog(Competition competition) {
        try {
            WebDog dog = new WebDog();
            String value = dog.Fetch(_uriProvider.GetLogUri(competition));
            return Logs(value, competition);
        } catch (Exception ex) {

            return null;
        }
    }

    public ArrayList<Live> GetLive(Competition competition) {
        try {
            WebDog dog = new WebDog();
            String values = dog.Fetch(_uriProvider.GetLiveUri(competition));
            return Live(values);
        } catch (Exception ex) {

            return null;
        }
    }

    public ArrayList<NewsItem> GetNews(Competition competition) {
        try {
            WebDog dog = new WebDog();
            String values = dog.Fetch(_uriProvider.News(competition));
            return NewsList(values);
        } catch (Exception ex) {

            return null;
        }
    }
    public String GetNewsArticle(String url) {
        try {
            WebDog dog = new WebDog();
            String values = dog.Fetch(_uriProvider.NewsArticle(url));
            return NewsArticle(values);
        } catch (Exception ex) {

            return null;
        }
    }

    private ArrayList<LogItem> Logs(String htmlTable, Competition competition) {
        int startingIndex = htmlTable.indexOf("<table") - 1;
        htmlTable = htmlTable.substring(startingIndex);
        int endIndex = htmlTable.indexOf("</table>");
        htmlTable = htmlTable.substring(0, endIndex + 8);

        ArrayList<String> answer = GetRows(htmlTable);
        ArrayList<ArrayList<String>> table = GetRowsWithCols(answer);

        return Logs(table, competition);
    }
    private GameStats GetStatsFromFile(String html)
    {
        if(html.indexOf("class=\"statsL") != -1 ) {
            html = html.substring(html.indexOf("class=\"statsL"));
            html = html.substring(0, html.lastIndexOf("Match Summary"));
            html = "<div " + html;

            try {
                ArrayList<String> answer = GetTableContent("<div", "</div>", html);

                TeamStats home =new TeamStats(FrmDiv(answer.get(0)),FrmDiv(answer.get(3)),FrmDiv(answer.get(6)),
                        FrmDiv(answer.get(9)),FrmDiv(answer.get(12)),FrmDiv(answer.get(15)));
                TeamStats away  =new TeamStats(FrmDiv(answer.get(2)),FrmDiv(answer.get(5)),FrmDiv(answer.get(8)),
                        FrmDiv(answer.get(11)),FrmDiv(answer.get(14)),FrmDiv(answer.get(17)));
                //ArrayList<ArrayList<String>> table = GetRowsWithCols(answer);

                return new GameStats(home,away);

            }
            catch (Exception ex)
            {
                int x = 0;
            }

            int x = 0;
        }

        return null;
    }
    private String FrmDiv(String divElem)
    {
        String answer = divElem.substring(0, divElem.lastIndexOf("<"));
         answer = answer.substring(answer.lastIndexOf(">") + 1);


        answer = answer.trim();

        return answer;
    }
    private ArrayList<LogItem> Logs(ArrayList<ArrayList<String>> pieces, Competition competition) {
        ArrayList<LogItem> log = new ArrayList<LogItem>();
        int pos = 1;

        for (List<String> rawLogItem : pieces) {
            int value;

            if (IsInt(rawLogItem.get(1))) {
                log.add(new LogItem(pos,rawLogItem.get(0), Integer.parseInt(rawLogItem.get(1)),
                        Integer.parseInt(rawLogItem.get(2)), Integer.parseInt(rawLogItem.get(3)),
                        Integer.parseInt(rawLogItem.get(4)),competition.name()));

                pos++;
            }

        }

        return log;
    }

    private boolean IsInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private ArrayList<Result> Results(String htmlTable, String mobi) {

        htmlTable = htmlTable.substring(htmlTable.indexOf("<table"));
        htmlTable = htmlTable.substring(0, htmlTable.lastIndexOf("</table") + 8);
        htmlTable = htmlTable.replace("<br />", "");
        htmlTable = htmlTable.substring(10);
        htmlTable = htmlTable.substring(0, htmlTable.length() - 10);

        ArrayList<String> tables = GetTableContent("<table", "</table>", htmlTable);

        ArrayList<ArrayList<ArrayList<String>>> results = new ArrayList<ArrayList<ArrayList<String>>>();
        // tables.Select(table => GetRowsWithCols(GetRows(table))).ToList();
        for (String table : tables)
            results.add(GetRowsWithCols(GetRows(table)));


        return WithIds(REsultsFromList(results), mobi);

    }
    private LineUp LineUp(String htmlTable)
    {
        htmlTable = htmlTable.substring(htmlTable.indexOf("<table"));
        htmlTable = htmlTable.substring(0, htmlTable.lastIndexOf("</table") + 8);
        htmlTable = htmlTable.replace("<br />", "");
        htmlTable = htmlTable.substring(10);
        htmlTable = htmlTable.substring(0, htmlTable.length() - 10);


        ArrayList<String> tables = GetRows( htmlTable);
        ArrayList<ArrayList<String>> tables1 = GetRowsWithCols(tables);

        return FromTable(tables1);


    }
    private LineUp FromTable(ArrayList<ArrayList<String>> source)
    {
        boolean team1 = true;
         ArrayList<LineUpPlayer> home = new ArrayList<>();
         ArrayList<LineUpPlayer> away = new ArrayList<>();

        for (int i = 1; i < source.size(); i++)
        {
            if(source.get(i).size() == 1)
                team1 = false;
            else if(source.get(i).size() != 0)
            {
                if(utility.IsInt(source.get(i).get(1)))
                {
                    if(team1)
                    home.add(new LineUpPlayer(source.get(i).get(0),
                            Integer.parseInt(source.get(i).get(1)),source.get(i).get(2)));
                    else
                        away.add(new LineUpPlayer(source.get(i).get(0),
                                Integer.parseInt(source.get(i).get(1)),source.get(i).get(2)));
                }

            }
        }

        return new LineUp(home,away);

    }
    Utility utility = new Utility();
    private ArrayList<Result> WithIds(ArrayList<Result> withoutIds, String mobi)
    {
        ArrayList<MatchIdFile> ids = MatchIds(mobi);
        ArrayList<Result> withIds = new ArrayList<Result>();

        for (int i = 0; i < withoutIds.size();i ++)
        {
                Result current = withoutIds.get(i);
                MatchIdFile x = FindMyMatch(current,ids);

                if(x != null) {
                    current.matchId = x.matchId;
                    withIds.add(current);
                }

        }
        return withIds;
    }
    private MatchIdFile FindMyMatch(Result result, ArrayList<MatchIdFile> ids)
    {
        for(MatchIdFile current :ids)
        {
            if((result.AwayTeamName.toLowerCase().trim().equals(current.away.toLowerCase())
                && result.HomeTeamName.toLowerCase().trim().equals(current.home.toLowerCase())) ||
                    (result.HomeTeamName.toLowerCase().trim().equals(current.away.toLowerCase())
                            && result.AwayTeamName.toLowerCase().trim().equals(current.home.toLowerCase())))
            {
                return current;
            }

        }
        return null;
    }
    private ArrayList<MatchIdFile> MatchIds(String mobi)
    {
        ArrayList<MatchIdFile> ids = new ArrayList<MatchIdFile>();

        while (mobi.indexOf("/football/match/") != -1)
        {
            String nameSource = mobi;
            nameSource = nameSource.substring(nameSource.indexOf("\"teamname\">") + "\"teamname\">".length());
            String home = nameSource.substring(0,nameSource.indexOf("<"));

            nameSource = nameSource.substring(nameSource.indexOf("\"teamname\">") + "\"teamname\">".length());
            String away = nameSource.substring(0,nameSource.indexOf("<"));

            //

            mobi=  mobi.substring(mobi.indexOf("/football/match/") + "/football/match/".length());
            String id =  mobi.substring(0, mobi.indexOf("\""));
            ids.add(new MatchIdFile(home.trim(), away.trim(),id));
        }

        return ids;
    }
    private ArrayList<Result> REsultsFromList(ArrayList<ArrayList<ArrayList<String>>> values) {
        ArrayList<Result> list = new ArrayList<Result>();
        for (ArrayList<ArrayList<String>> rawResult : values) {
            if (rawResult.size() > 2) {
                String homeScorers = (rawResult.get(2).size() > 0) ? rawResult.get(2).get(0) : "";
                String awayScorers = (rawResult.get(2).size() > 2) ? rawResult.get(2).get(2) : "";
                String v = rawResult.get(1).get(1);
                try {

                    int score1 = Integer.parseInt(v.split("-")[0].trim());
                    int score2 = Integer.parseInt(v.split("-")[1].trim());

                    list.add(new Result(rawResult.get(1).get(0), rawResult.get(1).get(2), score1,
                            score2, rawResult.get(0).get(0), homeScorers, awayScorers));
                } catch (Exception ex) {
                    int x = 0;
                }
            }
        }
        return list;
    }

    private ArrayList<Fixture> Fixtures(String htmlTable) {
        String raw = htmlTable;
        htmlTable = htmlTable.substring(htmlTable.indexOf("<table"));
        htmlTable = htmlTable.substring(0, htmlTable.lastIndexOf("</table") + 8);
        htmlTable = htmlTable.replace("<br />", "");
        htmlTable = htmlTable.substring(10);
        htmlTable = htmlTable.substring(0, htmlTable.length() - 10);

        ArrayList<String> tables = GetTableContent("<table", "</table>", htmlTable);

        ArrayList<ArrayList<ArrayList<String>>> results = new ArrayList<ArrayList<ArrayList<String>>>();

        for (String value : tables)
            results.add(GetRowsWithCols(GetRows(value)));
        // tables.Select(table => GetRowsWithCols(GetRows(table))).ToList();

        return FixtureFromList(results, raw);
    }

    private ArrayList<Fixture> FixtureFromList(ArrayList<ArrayList<ArrayList<String>>> list, String raw) {
        ArrayList<Fixture> fixtures = new ArrayList<Fixture>();
        String nextFixtureDate = "";
        raw = raw.substring(raw.indexOf("<table"));
        raw = raw.substring(0, raw.indexOf("</table>") + 8);
        raw = GetRowsWithCols(GetRows(raw)).get(0).get(0);//[0][0];

        //if (IsDate(raw))
        nextFixtureDate = raw;//  ParseDate(raw);

        for (ArrayList<ArrayList<String>> subList : list) {
            if (subList.size() == 1 && subList.get(0).size() == 1) {
                nextFixtureDate = subList.get(0).get(0);// ParseDate(subList.get(0).get(0));
            } else if (subList.size() > 0 && subList.get(0).size() > 2) {
                //fixtures.AddRange(subList.Select(rawFixture => new Fixture(rawFixture[0],
                //      rawFixture[2], nextFixtureDate, rawFixture[3], rawFixture[4])));
                for (List<String> value : subList)
                    fixtures.add(new Fixture(value.get(0), value.get(2), nextFixtureDate, value.get(3), value.get(4)));
            }
        }


        return fixtures;

    }

    private Date ParseDate(String value) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date start = simpleDateFormat.parse(value);
            //calendar.setTime(start); // comment out to test current time
            return start;
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean IsDate(String value) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date start = simpleDateFormat.parse(value);
            return true;
        } catch (Exception ex) {
            return false;
        }


    }

    private ArrayList<Live> Live(String content) {

        ArrayList<String> rawLive = new ArrayList<String>();

        while (content.indexOf("<div id=\"footballlivescoringone\">") != -1) {
            try {


                int startingIndex = content.indexOf("<div id=\"footballlivescoringone\">");

                int endIndex = FindMatchingTagIndex(startingIndex, "<div", "</div>", content);

                if (endIndex == -1) continue;
                rawLive.add(content.substring(startingIndex, startingIndex + (endIndex - startingIndex)));

                content = content.substring(endIndex + 6);
            } catch (Exception ex) {
                int x = 0;
            }
        }

        return Live(rawLive);
    }
    private ArrayList<NewsItem> NewsList(String content) {
        int count = 9;
        ArrayList<NewsItem> rawLive = new ArrayList<NewsItem>();

        while (content.indexOf("<div class=\"newsImgItem\">") != -1 && count > 0) {
            try {


                int startingIndex = content.indexOf("<div class=\"newsImgItem\">");

                int endIndex = FindMatchingTagIndex(startingIndex, "<div", "</div>", content);

                if (endIndex == -1) continue;
                String value =content.substring(startingIndex, startingIndex + (endIndex - startingIndex));

                String rawLink = value.substring(value.indexOf("<a "));
                rawLink = rawLink.substring(0, rawLink.indexOf(">"));
                rawLink = rawLink.substring(rawLink.indexOf("\""));
                rawLink = utility.Trim("\"",rawLink);

                String title = rawLink.substring(rawLink.lastIndexOf("/") +1);
                title = title.replace("_", " ");

                if(count != 9)
                    rawLive.add(new NewsItem(title,rawLink));
                else
                {
                    String d = value.substring(value.indexOf("src=") + 5);
                    d = d.substring(0,d.indexOf("/>")-2);
                    rawLive.add(new NewsItem(title,rawLink,d));
                }

                content = content.substring(endIndex + 6);
                count--;
            } catch (Exception ex) {
                int x = 0;
            }
        }

        return rawLive;
    }

    private String NewsArticle(String content)
    {
        if(content.indexOf("\"articlebody\"") == -1)
            return null;

        content = content.substring(content.indexOf("\"articlebody\"") + "\"articlebody\"".length() + 1);
        content = content.substring(0,content.indexOf("</div>"));
        content = content.replace("<p>", "");
        content = content.replace("</p>", "\n");

        return content;
    }
    private Live Commentry(Live game, String content) {

        ArrayList<String> rawLive = new ArrayList<String>();

        while (content.indexOf("<div id=\"match\">") != -1) {
            int startingIndex = content.indexOf("<div id=\"match\">");

            int endIndex = FindMatchingTagIndex(startingIndex, "<div", "</div>", content);

            if (endIndex != -1) {
                rawLive.add(content.substring(startingIndex, startingIndex + (endIndex - startingIndex)));

                content = content.substring(endIndex + 6);
            }
        }

        if (rawLive.size() <= 0) return null;
        ArrayList<String> matchCom = GetTableContent("<div", "</div>", rawLive.get(0));

        String home = matchCom.get(matchCom.size() - 1).substring(0, matchCom.get(matchCom.size() - 1).indexOf("</div"));
        home = home.substring(home.lastIndexOf(">") + 1);

        if (game != null)
            game.MatchStatus = home;

        game = GetScorers(game, matchCom);

        game.Commentry = Comments(content);

        return game;


        //return Live(rawLive);
    }
    private Result Commentry(Result game, String content) {

        ArrayList<String> rawLive = new ArrayList<String>();

        while (content.indexOf("<div id=\"match\">") != -1) {
            int startingIndex = content.indexOf("<div id=\"match\">");

            int endIndex = FindMatchingTagIndex(startingIndex, "<div", "</div>", content);

            if (endIndex != -1) {
                rawLive.add(content.substring(startingIndex, startingIndex + (endIndex - startingIndex)));

                content = content.substring(endIndex + 6);
            }
        }

        if (rawLive.size() <= 0) return null;
        ArrayList<String> matchCom = GetTableContent("<div", "</div>", rawLive.get(0));

        //String home = matchCom.get(matchCom.size() - 1).substring(0, matchCom.get(matchCom.size() - 1).indexOf("</div"));
       // home = home.substring(home.lastIndexOf(">") + 1);

        if (game != null)
            //game.MatchStatus = home;

        game = GetScorers(game, matchCom);

        game.Commentry = Comments(content);

        return game;


        //return Live(rawLive);
    }

    private Live GetScorers(Live game, ArrayList<String> rawData) {
        for (String value : rawData) {
            if (value.replace("team1infoholder", "").indexOf("team1info") != -1) {
                String home = value.substring(0, value.indexOf("</div"));
                home = home.substring(home.replace("<br />", "").lastIndexOf(">") + 1);

                if (game != null)
                    game.HomeTeamGoalScorers = GetScorers(home);
            }

            if (value.replace("team2infoholder", "").indexOf("team2info") != -1) {
                String home = value.substring(0, value.indexOf("</div"));
                home = home.substring(home.replace("<br />", "").lastIndexOf(">") + 1);

                if (game != null)
                    game.AwayTeamGoalScorers = GetScorers(home);
            }
        }


        return game;
    }
    private Result GetScorers(Result game, ArrayList<String> rawData) {
        for (String value : rawData) {
            if (value.replace("team1infoholder", "").indexOf("team1info") != -1) {
                String home = value.substring(0, value.indexOf("</div"));
                home = home.substring(home.replace("<br />", "").lastIndexOf(">") + 1);

                if (game != null)
                    game.HomeTeamGoalScorers = GetScorers(home);
            }

            if (value.replace("team2infoholder", "").indexOf("team2info") != -1) {
                String home = value.substring(0, value.indexOf("</div"));
                home = home.substring(home.replace("<br />", "").lastIndexOf(">") + 1);

                if (game != null)
                    game.AwayTeamGoalScorers = GetScorers(home);
            }
        }


        return game;
    }
    private ArrayList<String> GetScorers(String value) {
        value = value.replace("<br />", "");
        String[] values = value.split("\\)");

        ArrayList<String> list = new ArrayList<String>();
        for (String myValue : values) {
            if (myValue.trim().length() > 3) //list.add(myValue.trim().trim(',') + ")");
                list.add(Trim(",", myValue) + ")");
        }
        return list;
    }

    private String Trim(String value, String host) {
        while (host.endsWith(value))
            host = host.substring(0, host.length() - 2);

        while (host.startsWith(value) || host.startsWith(" "))
            host = host.substring(1);

        return host;
    }

    private ArrayList<Comment> Comments(String rawData) {
        //<div id="comm">
        ArrayList<Comment> comments = new ArrayList<Comment>();

        int startingIndex = rawData.indexOf("<div id=\"comm\">");

        int endIndex = FindMatchingTagIndex(startingIndex, "<div", "</div>", rawData);

        if (endIndex != -1) {
            rawData = rawData.substring(startingIndex, endIndex - startingIndex);

            ArrayList<String> pieces = GetTableContent("<div", "</div", rawData);

            for (int i = 0; i < pieces.size() - 1; i++) {
                String time = pieces.get(i).substring(0, pieces.get(i).indexOf("</div"));
                time = time.substring(time.lastIndexOf(">") + 1);

                String txt = pieces.get(i + 1).substring(0, pieces.get(i + 1).indexOf("</div"));
                txt = txt.substring(txt.lastIndexOf(">") + 1);

                comments.add(new Comment(time, txt));
                i++;
            }

        }
        return comments;

    }

    private ArrayList<Live> Live(ArrayList<String> rawLive) {
        try {
            ArrayList<Live> liveGames = new ArrayList<Live>();

            for (String rawGame : rawLive) {
                ArrayList<String> value = GetTableContent("<div", "</div>", rawGame);

                String home = value.get(2).substring(0, value.get(2).indexOf("</div"));
                home = home.substring(home.lastIndexOf(">") + 1);

                String homeScoreLine = value.get(3).substring(0, value.get(3).lastIndexOf("</div"));
                homeScoreLine = homeScoreLine.substring(homeScoreLine.lastIndexOf(">") + 1);

                String away = value.get(5).substring(0, value.get(5).lastIndexOf("</div"));
                away = away.substring(away.lastIndexOf(">") + 1);

                String awayScoreLine = value.get(6).substring(0, value.get(6).lastIndexOf("</div"));
                awayScoreLine = awayScoreLine.substring(awayScoreLine.lastIndexOf(">") + 1);


                int startingIndex = value.get(7).indexOf("match/") + 6;
                int endIndex = value.get(7).indexOf("Live Scoring") - 2;
                long matchId = Long.parseLong(value.get(7).substring(startingIndex, startingIndex + (endIndex - startingIndex)));

                liveGames.add(new Live(home, away, Integer.parseInt(homeScoreLine), Integer.parseInt(awayScoreLine), matchId));
            }

            return liveGames;
        } catch (Exception ex) {
            return null;
        }
    }

    private ArrayList<TopGoalScorer> Scorers(String htmlTable) {
        try {
            htmlTable = htmlTable.substring(htmlTable.indexOf("<table"));
            htmlTable = htmlTable.substring(0, htmlTable.lastIndexOf("</table") + 8);
            ArrayList<String> answer = GetRows(htmlTable);
            ArrayList<ArrayList<String>> table = GetRowsWithCols(answer);

            ArrayList<TopGoalScorer> scorers = new ArrayList<TopGoalScorer>();

            for (List<String> rawScorer : table) {
                try {
                    scorers.add(new TopGoalScorer(rawScorer.get(0), rawScorer.get(1), Integer.parseInt(rawScorer.get(2))));
                }
                catch (Exception ex)
                {
                    int x = 0;
                }

            }

            return scorers;
        } catch (Exception ex) {
            return null;
        }
    }

    private ArrayList<String> GetRows(String table) {
        return GetTableContent("<tr", "</tr>", table);
    }

    private ArrayList<ArrayList<String>> GetRowsWithCols(ArrayList<String> rows) {
        ArrayList<ArrayList<String>> answer = new ArrayList<ArrayList<String>>();

        for (String row : rows)
            answer.add(GetColums(row));

        return answer;
    }

    private ArrayList<String> GetColums(String row) {
        ArrayList<String> value = GetTableContent("<td", "</td>", row);

        for (int i = 0; i < value.size(); i++) {
            value.set(i, value.get(i).substring(0, value.get(i).indexOf("</")));
            value.set(i, value.get(i).substring(value.get(i).lastIndexOf(">") + 1));
        }

        return value;
    }

    private ArrayList<String> GetTableContent(String openingTag, String closingTag, String data) {
        ArrayList<String> rows = new ArrayList<String>();

        while (data.indexOf(openingTag) != -1) {
            try {
                int rowStart = data.indexOf(openingTag);
                data = data.substring(rowStart);
                int rowEnd = data.indexOf(closingTag);
                rows.add(data.substring(0, rowEnd + closingTag.length()));
                data = data.substring(rowEnd + closingTag.length());
            } catch (Exception ex) {
                int x = 0;
            }
        }

        return rows;
    }

    public int FindMatchingTagIndex(int startIndex, String tag, String closingTag, String data) {

        int match = 0, dataLen = data.length(), tagLen = tag.length(), cTagLen = closingTag.length();

        for (int start = startIndex + 1; start < dataLen - cTagLen; start++) {
            String newTag = data.substring(start, start + tagLen);

            if (newTag.equals(tag))
                match++;

            String newClosingTag = data.substring(start, start + cTagLen);

            if (newClosingTag.equals(closingTag)) {
                if (match == 0)
                    return start + cTagLen;
                else match--;
            }
        }


        return -1;
    }
}
