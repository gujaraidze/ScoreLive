package com.example.scorelive.presentation.competition

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scorelive.App
import com.example.scorelive.domain.model.League
import com.example.scorelive.domain.model.LeaguePriority
import kotlinx.coroutines.flow.*

enum class CompetitionBrowseTab { TOP, REGION, FAVORITES }

class CompetitionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as App).repository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(CompetitionBrowseTab.TOP)
    val selectedTab: StateFlow<CompetitionBrowseTab> = _selectedTab.asStateFlow()

    private val prefs = application.getSharedPreferences("competition_prefs", android.content.Context.MODE_PRIVATE)
    private val _favoriteLeagueIds = MutableStateFlow(
        prefs.getStringSet("fav_leagues", emptySet())!!.map { it.toInt() }.toSet()
    )
    val favoriteLeagueIds: StateFlow<Set<Int>> = _favoriteLeagueIds.asStateFlow()

    // curated list as fallback — always searchable even before any date has been fetched
    private val curatedLeagues = listOf(
        League(1,   "World Cup",             "https://media.api-sports.io/football/leagues/1.png",   "World"),
        League(2,   "UEFA Champions League", "https://media.api-sports.io/football/leagues/2.png",   "Europe"),
        League(3,   "UEFA Europa League",    "https://media.api-sports.io/football/leagues/3.png",   "Europe"),
        League(39,  "Premier League",        "https://media.api-sports.io/football/leagues/39.png",  "England"),
        League(140, "La Liga",               "https://media.api-sports.io/football/leagues/140.png", "Spain"),
        League(135, "Serie A",               "https://media.api-sports.io/football/leagues/135.png", "Italy"),
        League(78,  "Bundesliga",            "https://media.api-sports.io/football/leagues/78.png",  "Germany"),
        League(61,  "Ligue 1",               "https://media.api-sports.io/football/leagues/61.png",  "France"),
        League(848, "Conference League",     "https://media.api-sports.io/football/leagues/848.png", "Europe"),
        League(88,  "Eredivisie",            "https://media.api-sports.io/football/leagues/88.png",  "Netherlands"),
        League(94,  "Primeira Liga",         "https://media.api-sports.io/football/leagues/94.png",  "Portugal"),
        League(253, "MLS",                   "https://media.api-sports.io/football/leagues/253.png", "USA"),
    )

    // leagues from DB (real matches seen) merged with curated list, deduped by id
    private val leaguesFromDb: Flow<List<League>> = repository.getLeaguesFromDb()
        .map { dbLeagues ->
            val dbIds = dbLeagues.map { it.id }.toSet()
            // db leagues first (have real data), then curated ones not already in db
            dbLeagues + curatedLeagues.filter { it.id !in dbIds }
        }

    val displayedLeagues: StateFlow<List<League>> = combine(
        leaguesFromDb, _searchQuery, _selectedTab, _favoriteLeagueIds
    ) { leagues, query, tab, favIds ->
        val sorted = when (tab) {
            CompetitionBrowseTab.TOP ->
                // known major leagues first by priority, then alphabetical for the rest
                leagues.sortedWith(compareBy(
                    { LeaguePriority.rankFor(it.id) },
                    { it.name }
                ))
            CompetitionBrowseTab.REGION ->
                leagues.sortedWith(compareBy({ it.country }, { it.name }))
            CompetitionBrowseTab.FAVORITES ->
                leagues.filter { it.id in favIds }
                    .sortedBy { it.name }
        }
        if (query.isBlank()) sorted
        else sorted.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.country.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }
    fun onTabSelected(tab: CompetitionBrowseTab) { _selectedTab.value = tab }

    fun toggleFavoriteLeague(leagueId: Int) {
        val current = _favoriteLeagueIds.value
        _favoriteLeagueIds.value = if (leagueId in current) current - leagueId else current + leagueId
        prefs.edit().putStringSet("fav_leagues", _favoriteLeagueIds.value.map { it.toString() }.toSet()).apply()
    }

    fun isFavoriteLeague(leagueId: Int) = leagueId in _favoriteLeagueIds.value
}