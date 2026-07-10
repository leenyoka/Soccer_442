using System.Web;
using System.Xml.Linq;
using Soccer442.Api.Models;

namespace Soccer442.Api.Sources;

/// <summary>
/// C# port of NewsClient.java - pulls football news from Google News' public RSS search
/// feed - free, no key, no account, and an actual documented feed format Google serves for
/// exactly this kind of programmatic consumption.
/// </summary>
public class NewsClient
{
    private readonly HttpFetcher _dog;
    private readonly ILogger<NewsClient> _logger;

    public NewsClient(HttpFetcher dog, ILogger<NewsClient> logger)
    {
        _dog = dog;
        _logger = logger;
    }

    public async Task<List<NewsItem>> GetNewsAsync(string competition)
    {
        var items = new List<NewsItem>();
        try
        {
            var query = HttpUtility.UrlEncode($"{competition} football");
            var url = $"https://news.google.com/rss/search?q={query}&hl=en-US&gl=US&ceid=US:en";
            var raw = await _dog.FetchAsync(url);
            if (raw == null) return items;

            var doc = XDocument.Parse(raw);
            foreach (var item in doc.Descendants("item"))
            {
                var title = item.Element("title")?.Value;
                var link = item.Element("link")?.Value;
                if (title != null && link != null) items.Add(new NewsItem(CleanTitle(title), link));
            }
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "GetNews failed for {Competition}", competition);
        }
        return items;
    }

    // Google News titles are "Headline - Source Name"; the <source> element already carries
    // the source name separately, so trim the redundant suffix for display.
    private static string CleanTitle(string title)
    {
        var idx = title.LastIndexOf(" - ", StringComparison.Ordinal);
        return idx > 0 ? title[..idx] : title;
    }
}
