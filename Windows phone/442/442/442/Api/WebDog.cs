using System;
using System.Threading.Tasks;
using System.Net.Http;

namespace _442.Api
{
    public class WebDog
    {
        #region Properties

        private readonly HttpClient _client;

        #endregion Properties 

        #region Constructor

        public WebDog()
        {
            _client = new HttpClient();
        }

        #endregion Constructor

        public async Task<string> Fetch(string url)
        {
            var value = await ExecuteHttpGet(url);
            return value;
        }

        public async Task<string> ExecuteHttpGet(string uri)
        {
            try
            {
                var result = await _client.GetStringAsync( "http://" + uri);
                return result;

            }
            catch (Exception ex)
            {
                return "error";
            }
        }

    }
}
