package com.example.scorelive.presentation.competition

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scorelive.App
import com.example.scorelive.data.NetworkResult
import com.example.scorelive.domain.model.Match
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// popular leagues with their API-Football IDs
enum class FootballLeague(val id: Int, val displayName: String) {
    PREMIER_LEAGUE(39, "Premier League"),
    LA_LIGA(140, "La Liga"),
    BUNDESLIGA(78, "Bundesliga"),
    SERIE_A(135, "Serie A"),
    LIGUE_1(61, "Ligue 1"),
    CHAMPIONS_LEAGUE(2, "Champions League")
}

sealed class CompetitionUiState {
    object Loading : CompetitionUiState()
    data class Success(val matches: List<Match>) : CompetitionUiState()
    data class Error(val message: String) : CompetitionUiState()
}

sealed class CompetitionEvent {
    data class NavigateToDetail(val matchId: Int) : CompetitionEvent()
}

class CompetitionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as App).repository

    private val _uiState = MutableStateFlow<CompetitionUiState>(CompetitionUiState.Loading)
    val uiState: StateFlow<CompetitionUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<CompetitionEvent>()
    val event = _event.asSharedFlow()

    private val _selectedLeague = MutableStateFlow(FootballLeague.PREMIER_LEAGUE)
    val selectedLeague: StateFlow<FootballLeague> = _selectedLeague.asStateFlow()

    init {
        observeMatches()
        fetchLeagueMatches()
    }

    private fun observeMatches() {
        viewModelScope.launch {
            repository.getTodayMatchesFromDb().collect { matches ->
                val filtered = matches.filter {
                    it.league.id == _selectedLeague.value.id
                }
                _uiState.value = CompetitionUiState.Success(filtered)
            }
        }
    }

    private fun fetchLeagueMatches() {
        viewModelScope.launch {
            _uiState.value = CompetitionUiState.Loading
            when (val result = repository.fetchTodayMatches(
                java.time.LocalDate.now().toString()
            )) {
                is NetworkResult.Failure -> {
                    _uiState.value = CompetitionUiState.Error(result.message)
                }
                is NetworkResult.Success -> {}
            }
        }
    }

    fun onLeagueSelected(league: FootballLeague) {
        _selectedLeague.value = league
        observeMatches()
    }

    fun onMatchClicked(matchId: Int) {
        viewModelScope.launch {
            _event.emit(CompetitionEvent.NavigateToDetail(matchId))
        }
    }
}