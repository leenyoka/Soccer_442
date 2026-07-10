using System.Text.Json;

namespace Soccer442.Api.Sources;

/// <summary>
/// C# port of EspnJsonUtil.java - recursively scans a JSON tree for the first array whose
/// every element satisfies a shape predicate (e.g. "has a 'roster' array and a 'homeAway'
/// field"). Used instead of a hardcoded field path so a shallow restructuring of ESPN's
/// summary response doesn't break lineup/statistics extraction - only an actual removal or
/// rename of the fields themselves would.
/// </summary>
public static class EspnJsonUtil
{
    public static readonly JsonSerializerOptions CaseInsensitive = new() { PropertyNameCaseInsensitive = true };

    public static JsonElement? FindArrayByShape(JsonElement node, Func<JsonElement, bool> predicate, int maxDepth)
        => Walk(node, predicate, 0, maxDepth);

    private static JsonElement? Walk(JsonElement node, Func<JsonElement, bool> predicate, int depth, int maxDepth)
    {
        if (depth > maxDepth) return null;

        if (node.ValueKind == JsonValueKind.Array)
        {
            if (node.GetArrayLength() > 0 && AllMatch(node, predicate)) return node;
            foreach (var item in node.EnumerateArray())
            {
                var found = Walk(item, predicate, depth + 1, maxDepth);
                if (found != null) return found;
            }
            return null;
        }

        if (node.ValueKind == JsonValueKind.Object)
        {
            foreach (var prop in node.EnumerateObject())
            {
                var found = Walk(prop.Value, predicate, depth + 1, maxDepth);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static bool AllMatch(JsonElement arr, Func<JsonElement, bool> predicate)
    {
        foreach (var el in arr.EnumerateArray())
        {
            if (el.ValueKind != JsonValueKind.Object || !predicate(el)) return false;
        }
        return true;
    }

    public static double NumberField(JsonElement? obj, string field, double fallback)
    {
        if (obj == null || obj.Value.ValueKind != JsonValueKind.Object || !obj.Value.TryGetProperty(field, out var val) || val.ValueKind == JsonValueKind.Null)
            return fallback;
        if (val.ValueKind == JsonValueKind.String)
        {
            var raw = val.GetString()?.Replace("%", "");
            return double.TryParse(raw, out var parsed) ? parsed : fallback;
        }
        if (val.ValueKind == JsonValueKind.Number) return val.GetDouble();
        return fallback;
    }

    public static string? StringField(JsonElement? obj, string field, string? fallback)
    {
        if (obj == null || obj.Value.ValueKind != JsonValueKind.Object || !obj.Value.TryGetProperty(field, out var val) || val.ValueKind == JsonValueKind.Null)
            return fallback;
        return val.ValueKind == JsonValueKind.String ? val.GetString() : val.ToString();
    }

    public static JsonElement? Prop(JsonElement obj, string field)
        => obj.ValueKind == JsonValueKind.Object && obj.TryGetProperty(field, out var val) && val.ValueKind != JsonValueKind.Null ? val : null;
}
