using _442.Api;

namespace _442
{
    public enum ViewType
    {
        Log, Live, Fixture, Score, Result
    }

    public interface IView
    {
        Competition GetCompetition();
        ViewType GetViewType();
    }
}
