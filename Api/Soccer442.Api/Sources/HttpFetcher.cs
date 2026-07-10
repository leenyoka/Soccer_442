namespace Soccer442.Api.Sources;

/// <summary>
/// C# port of WebDog.java - single point every source client funnels an upstream GET
/// through. No auth token needed: every upstream (ESPN, BBC, OpenLigaDB, TheSportsDB,
/// Google News RSS, Wikipedia) is free and key-less. Wikipedia's API policy requires a
/// descriptive User-Agent (blocks/throttles requests using a generic client default) -
/// sent on every request, harmless for the other sources.
/// </summary>
public class HttpFetcher
{
    private readonly HttpClient _http;
    private readonly ILogger<HttpFetcher> _logger;

    public HttpFetcher(HttpClient http, ILogger<HttpFetcher> logger)
    {
        _http = http;
        _logger = logger;
    }

    public async Task<string?> FetchAsync(string url)
    {
        try
        {
            using var request = new HttpRequestMessage(HttpMethod.Get, url);
            request.Headers.TryAddWithoutValidation("User-Agent", "Soccer442Api/1.0 (hobby project; https://github.com/leenyoka/Soccer_442)");
            request.Headers.TryAddWithoutValidation("Accept", "application/json, text/html;q=0.9, */*;q=0.8");
            using var response = await _http.SendAsync(request);
            return await response.Content.ReadAsStringAsync();
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Fetch failed for {Url}", url);
            return null;
        }
    }
}
