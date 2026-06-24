package com.example.scorelive.presentation.competition

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.scorelive.App
import com.example.scorelive.domain.model.*
import kotlinx.coroutines.flow.*

// Only Results and Fixtures — from local DB filtered by leagueId
// No API call needed, data comes from the date-based fetches on the Home screen
enum class CompetitionDetailTab { RESULTS, FIXTURES }

class CompetitionDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = (application as App).repository

    val leagueId: Int = savedStateHandle.get<Int>("leagueId") ?: 0
    val leagueName: String = java.net.URLDecoder.decode(
        savedStateHandle.get<String>("leagueName") ?: "", "UTF-8"
    )
    val logoUrl: String = java.net.URLDecoder.decode(
        savedStateHandle.get<String>("logoUrl") ?: "", "UTF-8"
    )
    val country: String = java.net.URLDecoder.decode(
        savedStateHandle.get<String>("country") ?: "", "UTF-8"
    )

    private val _selectedTab = MutableStateFlow(CompetitionDetailTab.RESULTS)
    val selectedTab: StateFlow<CompetitionDetailTab> = _selectedTab.asStateFlow()

    // reactive DB read — updates automatically if new matches are fetched on Home screen
    // split into results/fixtures by status, sorted appropriately
    val results: StateFlow<List<Match>> = repository
        .getMatchesByLeagueFromDb(leagueId)
        .map { matches ->
            matches
                .filter { it.status == MatchStatus.FINISHED }
                .sortedByDescending { it.date }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fixtures: StateFlow<List<Match>> = repository
        .getMatchesByLeagueFromDb(leagueId)
        .map { matches ->
            matches
                .filter { it.status == MatchStatus.SCHEDULED || it.status == MatchStatus.LIVE }
                .sortedBy { it.date }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onTabSelected(tab: CompetitionDetailTab) {
        _selectedTab.value = tab
    }
}