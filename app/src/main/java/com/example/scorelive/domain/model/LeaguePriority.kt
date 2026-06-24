package com.example.scorelive.domain.model

/**
 * Ranks leagues by importance so the Home screen shows the biggest competitions
 * first (World Cup, Champions League, top-5 European leagues) and pushes smaller
 * or less-followed leagues further down — regardless of alphabetical order.
 *
 * Lower rank number = shown first. League IDs are API-Football's fixed ids,
 * which stay the same across seasons (e.g. Premier League is always 39).
 *
 * Any league id not listed here falls back to [DEFAULT_RANK], so new/unknown
 * leagues still show up, just below everything we've explicitly ranked.
 */
object LeaguePriority {

    // tier 0 — international tournaments, the most-watched competitions globally
    private val tier0 = setOf(
        1,   // World Cup
        4,   // Euro Championship
        2,   // Champions League
        3,   // Europa League
        848  // Conference League
    )

    // tier 1 — the "big five" European domestic leagues
    private val tier1 = setOf(
        39,  // Premier League (England)
        140, // La Liga (Spain)
        135, // Serie A (Italy)
        78,  // Bundesliga (Germany)
        61   // Ligue 1 (France)
    )

    // tier 2 — other major European leagues + strong continental competitions
    private val tier2 = setOf(
        88,  // Eredivisie (Netherlands)
        94,  // Primeira Liga (Portugal)
        144, // Belgian Pro League
        203, // Super Lig (Turkey)
        179, // Scottish Premiership
        253  // MLS (USA) — high audience interest even if not "elite" football
    )

    // tier 3 — second divisions of major countries (Championship, 2. Bundesliga, etc.)
    private val tier3 = setOf(
        40,  // Championship (England)
        141, // La Liga 2 (Spain)
        79,  // 2. Bundesliga (Germany)
        62,  // Ligue 2 (France)
        136  // Serie B (Italy)
    )

    private const val TIER_0_RANK = 0
    private const val TIER_1_RANK = 10
    private const val TIER_2_RANK = 20
    private const val TIER_3_RANK = 30

    // everything else (including smaller South American, African, Asian leagues,
    // friendlies, youth/reserve competitions) sits here, sorted alphabetically among themselves
    const val DEFAULT_RANK = 100

    /**
     * Lower number sorts first. Use together with league name as a secondary sort key
     * so leagues within the same tier are still alphabetical.
     */
    fun rankFor(leagueId: Int): Int = when {
        leagueId in tier0 -> TIER_0_RANK
        leagueId in tier1 -> TIER_1_RANK
        leagueId in tier2 -> TIER_2_RANK
        leagueId in tier3 -> TIER_3_RANK
        else -> DEFAULT_RANK
    }
}