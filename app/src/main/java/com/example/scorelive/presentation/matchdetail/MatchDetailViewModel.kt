package com.example.scorelive.presentation.matchdetail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.scorelive.App
import com.example.scorelive.data.NetworkResult
import com.example.scorelive.domain.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class MatchDetailUiState {
    object Loading : MatchDetailUiState()
    data class Success(val matchDetail: MatchDetail) : MatchDetailUiState()
    data class Error(val message: String) : MatchDetailUiState()
}

enum class MatchDetailTab {
    SUMMARY, LINEUP, STATS, H2H, STANDINGS
}

class MatchDetailViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = (application as App).repository
    private val matchId: Int = savedStateHandle.get<Int>("matchId") ?: 0

    private val _uiState = MutableStateFlow<MatchDetailUiState>(MatchDetailUiState.Loading)
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(MatchDetailTab.SUMMARY)
    val selectedTab: StateFlow<MatchDetailTab> = _selectedTab.asStateFlow()

    val isFavoriteState: StateFlow<Boolean> = repository.isFavorite(matchId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // single source of truth for all match detail data — stored in companion object
    // so it survives ViewModel recreation (navigate away and back)
    private var data: MatchDetail
        get() = cache.getOrPut(matchId) { MatchDetail(emptyMatch) }
        set(value) { cache[matchId] = value }

    private val emptyMatch = Match(
        id = 0, homeTeam = Team(0, "", ""), awayTeam = Team(0, "", ""),
        homeScore = null, awayScore = null, league = League(0, "", "", ""),
        status = MatchStatus.SCHEDULED, minute = null, date = ""
    )

    init {
        // restore immediately from cache if available (second open of same match)
        val cached = cache[matchId]
        if (cached != null && cached.match.id != 0) {
            _uiState.value = MatchDetailUiState.Success(cached)
        }
        loadInitialData()
    }

    private fun loadInitialData() {
        // don't re-fetch events/lineups if already loaded this session
        if (matchId in eventsLoaded) return

        viewModelScope.launch {
            Log.d("MATCH_DETAIL", "Loading matchId: $matchId")

            val match = repository.getMatchByIdFromDb(matchId).first()
            if (match == null) {
                Log.e("MATCH_DETAIL", "Match not found in DB")
                _uiState.value = MatchDetailUiState.Error("Match not found")
                return@launch
            }

            Log.d("MATCH_DETAIL", "Match: ${match.homeTeam.name} vs ${match.awayTeam.name}")
            data = data.copy(match = match)
            _uiState.value = MatchDetailUiState.Success(data)

            eventsLoaded.add(matchId)

            // fetch events and lineups in parallel
            val eventsDeferred = async { repository.fetchMatchEvents(matchId) }
            val lineupsDeferred = async { repository.fetchLineups(matchId) }

            val eventsResult = eventsDeferred.await()
            val lineupsResult = lineupsDeferred.await()

            Log.d("MATCH_DETAIL", "Events: ${if (eventsResult is NetworkResult.Success) eventsResult.data.size else "failed"}")
            Log.d("MATCH_DETAIL", "Lineups: ${if (lineupsResult is NetworkResult.Success) lineupsResult.data.size else "failed"}")

            if (eventsResult is NetworkResult.Failure) eventsLoaded.remove(matchId)

            data = data.copy(
                events = if (eventsResult is NetworkResult.Success) eventsResult.data else data.events,
                lineups = if (lineupsResult is NetworkResult.Success) lineupsResult.data else data.lineups
            )
            _uiState.value = MatchDetailUiState.Success(data)
        }
    }

    fun onTabSelected(tab: MatchDetailTab) {
        _selectedTab.value = tab
        when (tab) {
            MatchDetailTab.STATS -> loadStats()
            MatchDetailTab.H2H -> loadH2H()
            MatchDetailTab.STANDINGS -> loadStandings()
            else -> {}
        }
    }

    private fun loadStats() {
        if (data.statistics.isNotEmpty()) return
        if (matchId in statsLoaded) return
        viewModelScope.launch {
            statsLoaded.add(matchId)
            when (val result = repository.fetchStatistics(matchId)) {
                is NetworkResult.Success -> {
                    Log.d("MATCH_DETAIL", "Stats: ${result.data.size} teams")
                    data = data.copy(statistics = result.data)
                    _uiState.value = MatchDetailUiState.Success(data)
                }
                is NetworkResult.Failure -> {
                    Log.e("MATCH_DETAIL", "Stats failed: ${result.message}")
                    statsLoaded.remove(matchId)
                }
            }
        }
    }

    private fun loadH2H() {
        if (data.h2h.isNotEmpty()) return
        if (matchId in h2hLoaded) return
        viewModelScope.launch {
            h2hLoaded.add(matchId)
            val match = data.match
            when (val result = repository.fetchH2H(match.homeTeam.id, match.awayTeam.id)) {
                is NetworkResult.Success -> {
                    Log.d("MATCH_DETAIL", "H2H: ${result.data.size} matches")
                    data = data.copy(h2h = result.data)
                    _uiState.value = MatchDetailUiState.Success(data)
                }
                is NetworkResult.Failure -> {
                    Log.e("MATCH_DETAIL", "H2H failed: ${result.message}")
                    h2hLoaded.remove(matchId)
                }
            }
        }
    }

    private fun loadStandings() {
        if (data.standings.isNotEmpty()) return
        if (matchId in standingsLoaded) return
        viewModelScope.launch {
            standingsLoaded.add(matchId)
            val leagueId = data.match.league.id

            when (val seasonResult = repository.fetchLeagueSeason(leagueId)) {
                is NetworkResult.Failure -> {
                    // season not accessible on free plan — show empty rather than wrong data
                    Log.d("MATCH_DETAIL", "Standings not available on free plan for leagueId=$leagueId")
                    _uiState.value = MatchDetailUiState.Success(data)
                }
                is NetworkResult.Success -> {
                    val season = seasonResult.data
                    Log.d("MATCH_DETAIL", "Fetching standings leagueId=$leagueId season=$season")
                    when (val result = repository.fetchStandings(leagueId, season)) {
                        is NetworkResult.Success -> {
                            Log.d("MATCH_DETAIL", "Standings: ${result.data.size} teams")
                            data = data.copy(standings = result.data)
                            _uiState.value = MatchDetailUiState.Success(data)
                        }
                        is NetworkResult.Failure -> {
                            Log.e("MATCH_DETAIL", "Standings failed: ${result.message}")
                            standingsLoaded.remove(matchId)
                        }
                    }
                }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            if (isFavoriteState.value) repository.removeFavorite(matchId)
            else repository.addFavorite(matchId)
        }
    }

    fun retry() {
        eventsLoaded.remove(matchId)
        statsLoaded.remove(matchId)
        h2hLoaded.remove(matchId)
        standingsLoaded.remove(matchId)
        cache.remove(matchId)
        _uiState.value = MatchDetailUiState.Loading
        loadInitialData()
    }

    companion object {
        // companion object = survives ViewModel recreation (navigate away + back)
        // simple sets — no TTL, no SharedPreferences, no overlapping cache layers
        private val eventsLoaded = mutableSetOf<Int>()
        private val statsLoaded = mutableSetOf<Int>()
        private val h2hLoaded = mutableSetOf<Int>()
        private val standingsLoaded = mutableSetOf<Int>()
        private val cache = mutableMapOf<Int, MatchDetail>()
    }
}