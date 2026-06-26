package com.example.scorelive.data.repository

import com.example.scorelive.data.NetworkResult
import com.example.scorelive.domain.model.*
import com.example.scorelive.domain.repository.FootballRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Drop-in replacement for FootballRepositoryImpl that returns hardcoded data.
 * Swap in App.kt by setting USE_MOCK = true.
 * Zero API calls, zero quota burned during UI development.
 */
class MockRepository : FootballRepository {

    // --- shared team/league objects ---

    private val premLeague = League(39, "Premier League", "https://media.api-sports.io/football/leagues/39.png", "England", 2025)
    private val bundesliga  = League(78, "Bundesliga", "https://media.api-sports.io/football/leagues/78.png", "Germany", 2025)
    private val ucl         = League(2,  "UEFA Champions League", "https://media.api-sports.io/football/leagues/2.png", "World", 2025)

    private val manu  = Team(33,  "Manchester United",   "https://media.api-sports.io/football/teams/33.png")
    private val city  = Team(50,  "Manchester City",     "https://media.api-sports.io/football/teams/50.png")
    private val liver = Team(40,  "Liverpool",           "https://media.api-sports.io/football/teams/40.png")
    private val arsenal = Team(42,"Arsenal",             "https://media.api-sports.io/football/teams/42.png")
    private val chelsea = Team(49,"Chelsea",             "https://media.api-sports.io/football/teams/49.png")
    private val spurs  = Team(47, "Tottenham",           "https://media.api-sports.io/football/teams/47.png")
    private val forest = Team(65, "Nottingham Forest",   "https://media.api-sports.io/football/teams/65.png")
    private val villa  = Team(66, "Aston Villa",         "https://media.api-sports.io/football/teams/66.png")
    private val bayer  = Team(168,"Bayer Leverkusen",    "https://media.api-sports.io/football/teams/168.png")
    private val dortmund = Team(165,"Borussia Dortmund", "https://media.api-sports.io/football/teams/165.png")
    private val realMadrid = Team(541,"Real Madrid",     "https://media.api-sports.io/football/teams/541.png")
    private val barcelona  = Team(529,"Barcelona",       "https://media.api-sports.io/football/teams/529.png")

    private val today = LocalDate.now().toString()
    private val yesterday = LocalDate.now().minusDays(1).toString()

    // --- matches ---

    private val liveMatch = Match(
        id = 1001,
        homeTeam = manu, awayTeam = liver,
        homeScore = 1, awayScore = 2,
        league = premLeague, status = MatchStatus.LIVE, minute = 67,
        date = "${today}T15:00:00+00:00"
    )

    private val liveMatch2 = Match(
        id = 1002,
        homeTeam = city, awayTeam = arsenal,
        homeScore = 0, awayScore = 0,
        league = premLeague, status = MatchStatus.LIVE, minute = 23,
        date = "${today}T15:00:00+00:00"
    )

    private val finishedMatch1 = Match(
        id = 1003,
        homeTeam = chelsea, awayTeam = spurs,
        homeScore = 2, awayScore = 1,
        league = premLeague, status = MatchStatus.FINISHED, minute = null,
        date = "${today}T12:30:00+00:00"
    )

    private val finishedMatch2 = Match(
        id = 1004,
        homeTeam = forest, awayTeam = villa,
        homeScore = 0, awayScore = 3,
        league = premLeague, status = MatchStatus.FINISHED, minute = null,
        date = "${today}T12:30:00+00:00"
    )

    private val finishedMatch3 = Match(
        id = 1005,
        homeTeam = bayer, awayTeam = dortmund,
        homeScore = 3, awayScore = 1,
        league = bundesliga, status = MatchStatus.FINISHED, minute = null,
        date = "${today}T14:30:00+00:00"
    )

    private val upcomingMatch1 = Match(
        id = 1006,
        homeTeam = arsenal, awayTeam = chelsea,
        homeScore = null, awayScore = null,
        league = premLeague, status = MatchStatus.SCHEDULED, minute = null,
        date = "${today}T19:45:00+00:00"
    )

    private val upcomingMatch2 = Match(
        id = 1007,
        homeTeam = realMadrid, awayTeam = barcelona,
        homeScore = null, awayScore = null,
        league = ucl, status = MatchStatus.SCHEDULED, minute = null,
        date = "${today}T20:00:00+00:00"
    )

    private val allMatches = listOf(
        liveMatch, liveMatch2,
        finishedMatch1, finishedMatch2, finishedMatch3,
        upcomingMatch1, upcomingMatch2
    )

    // favorites state so add/remove actually works in mock mode
    private val favoriteIds = MutableStateFlow<Set<Int>>(setOf(1003))

    // --- DB reads ---

    override fun getMatchesByLeagueFromDb(leagueId: Int): Flow<List<Match>> =
        flow { emit(allMatches.filter { it.league.id == leagueId }) }

    override fun getLeaguesFromDb(): Flow<List<League>> =
        flow { emit(listOf(premLeague, bundesliga, ucl)) }

    override fun getTodayMatchesFromDb(): Flow<List<Match>> =
        flow { emit(allMatches) }

    override fun getMatchByIdFromDb(matchId: Int): Flow<Match?> =
        flow { emit(allMatches.find { it.id == matchId }) }

    override fun getMatchesByDateFromDb(date: String): Flow<List<Match>> =
        flow { emit(allMatches) }

    // --- API fetches (all instant, no network) ---

    override suspend fun fetchLiveMatches() = NetworkResult.Success(Unit)
    override suspend fun fetchMatchesByDate(date: String) = NetworkResult.Success(Unit)

    override suspend fun fetchMatchEvents(matchId: Int) = NetworkResult.Success(
        listOf(
            MatchEvent(12, manu, "Bruno Fernandes", "Rashford", EventType.GOAL, "Normal Goal"),
            MatchEvent(23, liver, "Mohamed Salah", null, EventType.GOAL, "Normal Goal"),
            MatchEvent(34, manu, "Casemiro", null, EventType.YELLOW_CARD, "Yellow Card"),
            MatchEvent(44, liver, "Darwin Nunez", "Diogo Jota", EventType.GOAL, "Normal Goal"),
            MatchEvent(58, manu, "Antony", "Scott McTominay", EventType.SUBSTITUTION, "Substitution"),
            MatchEvent(67, manu, "Harry Maguire", null, EventType.YELLOW_CARD, "Yellow Card"),
            MatchEvent(71, liver, "Harvey Elliott", "Curtis Jones", EventType.SUBSTITUTION, "Substitution"),
        )
    )

    override suspend fun fetchLineups(matchId: Int) = NetworkResult.Success(
        listOf(
            Lineup(
                team = manu,
                formation = "4-3-3",
                coachName = "Erik ten Hag",
                startXI = listOf(
                    Player(1, "Andre Onana", 24, "G", 1, 1),
                    Player(2, "Aaron Wan-Bissaka", 29, "D", 2, 1),
                    Player(3, "Victor Lindelof", 2, "D", 2, 2),
                    Player(4, "Harry Maguire", 5, "D", 2, 3),
                    Player(5, "Diogo Dalot", 20, "D", 2, 4),
                    Player(6, "Casemiro", 18, "M", 3, 1),
                    Player(7, "Scott McTominay", 39, "M", 3, 2),
                    Player(8, "Bruno Fernandes", 8, "M", 3, 3),
                    Player(9, "Antony", 21, "F", 4, 1),
                    Player(10, "Rasmus Hojlund", 11, "F", 4, 2),
                    Player(11, "Marcus Rashford", 10, "F", 4, 3),
                ),
                substitutes = listOf(
                    Player(12, "Tom Heaton", 22, "G"),
                    Player(13, "Jonny Evans", 35, "D"),
                    Player(14, "Luke Shaw", 23, "D"),
                    Player(15, "Fred", 17, "M"),
                    Player(16, "Christian Eriksen", 14, "M"),
                    Player(17, "Alejandro Garnacho", 49, "F"),
                    Player(18, "Facundo Pellistri", 28, "F"),
                )
            ),
            Lineup(
                team = liver,
                formation = "4-3-3",
                coachName = "Jurgen Klopp",
                startXI = listOf(
                    Player(21, "Alisson Becker", 1, "G", 1, 1),
                    Player(22, "Trent Alexander-Arnold", 66, "D", 2, 1),
                    Player(23, "Joel Matip", 32, "D", 2, 2),
                    Player(24, "Virgil van Dijk", 4, "D", 2, 3),
                    Player(25, "Andrew Robertson", 26, "D", 2, 4),
                    Player(26, "Alexis Mac Allister", 10, "M", 3, 1),
                    Player(27, "Dominik Szoboszlai", 8, "M", 3, 2),
                    Player(28, "Harvey Elliott", 19, "M", 3, 3),
                    Player(29, "Mohamed Salah", 11, "F", 4, 1),
                    Player(30, "Darwin Nunez", 9, "F", 4, 2),
                    Player(31, "Luis Diaz", 7, "F", 4, 3),
                ),
                substitutes = listOf(
                    Player(32, "Caoimhin Kelleher", 62, "G"),
                    Player(33, "Joe Gomez", 2, "D"),
                    Player(34, "Ibrahima Konate", 5, "D"),
                    Player(35, "Curtis Jones", 17, "M"),
                    Player(36, "Wataru Endo", 3, "M"),
                    Player(37, "Diogo Jota", 20, "F"),
                    Player(38, "Cody Gakpo", 18, "F"),
                )
            )
        )
    )

    override suspend fun fetchStatistics(matchId: Int) = NetworkResult.Success(
        listOf(
            FixtureStat(
                team = manu,
                statistics = listOf(
                    StatItem("Shots on Goal", "4"),
                    StatItem("Shots off Goal", "3"),
                    StatItem("Total Shots", "8"),
                    StatItem("Ball Possession", "42%"),
                    StatItem("Total passes", "387"),
                    StatItem("Corner Kicks", "3"),
                    StatItem("Offsides", "2"),
                    StatItem("Yellow Cards", "2"),
                    StatItem("Red Cards", "0"),
                    StatItem("Fouls", "11"),
                )
            ),
            FixtureStat(
                team = liver,
                statistics = listOf(
                    StatItem("Shots on Goal", "7"),
                    StatItem("Shots off Goal", "5"),
                    StatItem("Total Shots", "14"),
                    StatItem("Ball Possession", "58%"),
                    StatItem("Total passes", "541"),
                    StatItem("Corner Kicks", "6"),
                    StatItem("Offsides", "1"),
                    StatItem("Yellow Cards", "1"),
                    StatItem("Red Cards", "0"),
                    StatItem("Fouls", "9"),
                )
            )
        )
    )

    override suspend fun fetchH2H(homeTeamId: Int, awayTeamId: Int) = NetworkResult.Success(
        listOf(
            Match(2001, manu, liver, 2, 0, premLeague, MatchStatus.FINISHED, null, "2024-03-17T16:30:00+00:00"),
            Match(2002, liver, manu, 1, 0, premLeague, MatchStatus.FINISHED, null, "2023-12-17T16:30:00+00:00"),
            Match(2003, manu, liver, 0, 5, premLeague, MatchStatus.FINISHED, null, "2023-03-05T14:00:00+00:00"),
            Match(2004, liver, manu, 7, 0, premLeague, MatchStatus.FINISHED, null, "2022-10-16T15:30:00+00:00"),
            Match(2005, manu, liver, 0, 0, premLeague, MatchStatus.FINISHED, null, "2022-04-19T20:00:00+00:00"),
        )
    )

    override suspend fun fetchStandings(leagueId: Int, season: Int) = NetworkResult.Success(
        listOf(
            Standing(1, arsenal,  33, 23, 6, 4,  88, 43, 40, 75, "WWWWW", zoneDescription = "Promotion - Champions League (Group Stage)"),
            Standing(2, city,     31, 23, 4, 4,  76, 38, 53, 73, "WWWDW", zoneDescription = "Promotion - Champions League (Group Stage)"),
            Standing(3, liver,    32, 19, 7, 6,  72, 40, 22, 64, "WWDWW", zoneDescription = "Promotion - Champions League (Group Stage)"),
            Standing(4, manu,     32, 18, 5, 9,  58, 51, 9,  59, "WDWLW", zoneDescription = "Promotion - Champions League (Group Stage)"),
            Standing(5, spurs,    32, 16, 5, 11, 57, 52, 7,  53, "LWWWL", zoneDescription = "Promotion - Europa League (Group Stage)"),
            Standing(6, villa,    33, 16, 5, 12, 60, 58, 5,  53, "WLWDL", zoneDescription = "Promotion - Europa League (Group Stage)"),
            Standing(7, chelsea,  32, 13, 9, 10, 49, 43, 6,  48, "DLWDW", zoneDescription = "Promotion - Conference League (Qualification)"),
            Standing(8, forest,   32, 7,  9, 16, 35, 55, -20, 30, "LLLLD", zoneDescription = "Relegation - Championship"),
        )
    )

    // favorites — backed by MutableStateFlow so UI updates reactively
    override fun getFavoriteIds(): Flow<List<Int>> = favoriteIds.map { it.toList() }
    override fun isFavorite(matchId: Int): Flow<Boolean> = favoriteIds.map { matchId in it }

    override suspend fun addFavorite(matchId: Int) {
        favoriteIds.value = favoriteIds.value + matchId
    }
    override suspend fun removeFavorite(matchId: Int) {
        favoriteIds.value = favoriteIds.value - matchId
    }

    override suspend fun fetchLeagueSeason(leagueId: Int) = NetworkResult.Success(2025)

    override suspend fun searchTeams(query: String) = NetworkResult.Success(
        listOf(manu, city, liver, arsenal, chelsea, spurs)
            .filter { it.name.contains(query, ignoreCase = true) }
    )
}