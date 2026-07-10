using System.Text.Json;

namespace Soccer442.Api.Services;

/// <summary>Single shared JSON policy for both the HTTP responses and the RawJson cache
/// columns - camelCase to match the Android app's existing Gson POJOs field-for-field.</summary>
public static class ApiJson
{
    public static readonly JsonSerializerOptions Options = new(JsonSerializerDefaults.Web);
}
