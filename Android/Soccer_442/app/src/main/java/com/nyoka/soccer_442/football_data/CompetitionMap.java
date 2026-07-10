package com.nyoka.soccer_442.football_data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps the football-data.org-style competition codes the rest of the app already
 * uses (see FootballData.GetCompetitionCode) to each free data source's own
 * identifier for that competition. A code missing a given source's id simply
 * means that source is skipped for that competition.
 */
public class CompetitionMap {

    public static class Info {
        public final String code;
        public final String name;
        public final String areaName;
        public final String espnSlug;
        public final String openLigaDbShortcut;
        // TheSportsDB's own numeric league id (thesportsdb.com/api) - null for the two
        // competitions (EC, CLI) where a confirmed id wasn't verified, so that source is
        // simply skipped for them, same as any other source missing an id for a competition.
        public final String sportsDbLeagueId;

        Info(String code, String name, String areaName, String espnSlug, String openLigaDbShortcut, String sportsDbLeagueId) {
            this.code = code;
            this.name = name;
            this.areaName = areaName;
            this.espnSlug = espnSlug;
            this.openLigaDbShortcut = openLigaDbShortcut;
            this.sportsDbLeagueId = sportsDbLeagueId;
        }
    }

    private static final Map<String, Info> COMPETITIONS = new LinkedHashMap<>();

    static {
        add("PL", "Premier League", "England", "eng.1", null, "4328");
        add("ELC", "Championship", "England", "eng.2", null, "4329");
        add("CL", "UEFA Champions League", "Europe", "uefa.champions", null, "4480");
        add("EC", "European Championship", "Europe", "uefa.euro", null, null);
        add("BL1", "Bundesliga", "Germany", "ger.1", "bl1", "4331");
        add("SA", "Serie A", "Italy", "ita.1", null, "4332");
        add("FL1", "Ligue 1", "France", "fra.1", null, "4334");
        add("PD", "Primera Division", "Spain", "esp.1", null, "4335");
        add("DED", "Eredivisie", "Netherlands", "ned.1", null, "4337");
        add("PPL", "Primeira Liga", "Portugal", "por.1", null, "4344");
        add("BSA", "Campeonato Brasileiro Série A", "Brazil", "bra.1", null, "4351");
        add("CLI", "Copa Libertadores", "South America", "conmebol.libertadores", null, null);
        add("WC", "FIFA World Cup", "World", "fifa.world", null, "4429");
    }

    private static void add(String code, String name, String area, String espnSlug, String openLigaDbShortcut, String sportsDbLeagueId) {
        COMPETITIONS.put(code, new Info(code, name, area, espnSlug, openLigaDbShortcut, sportsDbLeagueId));
    }

    public static Info byCode(String code) {
        return COMPETITIONS.get(code);
    }

    public static Iterable<Info> all() {
        return COMPETITIONS.values();
    }
}
