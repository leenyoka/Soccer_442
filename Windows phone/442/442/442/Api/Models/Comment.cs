namespace _442.Api.Models
{
    public class Comment
    {
        public string Time { get; set; }
        public string Text { get; set; }


        public Comment(string time, string text)
        {
            Time = time;
            Text = text;
        }
    }
}
