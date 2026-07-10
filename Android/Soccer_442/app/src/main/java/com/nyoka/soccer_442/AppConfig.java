package com.nyoka.soccer_442;

/**
 * Points the app at the new Soccer442.Api backend (see /Api at the repo root) instead of
 * calling ESPN/BBC/OpenLigaDB/SportsDB/News/Wikipedia directly from the device.
 *
 * Emulator-only for now (docker-compose in /Api publishes the API on host port 8080). This
 * uses the host machine's actual LAN IP rather than the usual 10.0.2.2 emulator-to-host
 * alias: on this dev machine's emulator image, 10.0.2.2 is reachable from a root/adb shell
 * (confirmed via `nc`) but every app-sandboxed socket (both OkHttp and Android's own
 * HttpURLConnection) times out connecting to it - a real quirk of this specific AVD build,
 * not of the API/app code. The host's real IP has no such restriction. If this stops
 * working after a network change (new Wi-Fi, VPN, etc.), re-check the host's current IP
 * (`ipconfig` / `Get-NetIPAddress`) and update this constant - or switch back to 10.0.2.2
 * first, in case a future AVD/emulator update fixes the underlying quirk.
 */
public class AppConfig {
    public static final String API_BASE_URL = "http://192.168.18.95:8080";
}
