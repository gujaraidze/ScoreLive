package com.example.scorelive.presentation.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scorelive.App
import com.example.scorelive.data.NetworkResult
import com.example.scorelive.domain.model.Match
import com.example.scorelive.domain.model.Team
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(
        val teams: List<Team>,
        val matches: List<Match>
    ) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
    object Empty : SearchUiState()
}

@OptIn(FlowPreview::class)
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as App).repository

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // all today's matches for local filtering
    private var allMatches: List<Match> = emptyList()

    init {
        loadAllMatches()
        observeQuery()
    }

    private fun loadAllMatches() {
        viewModelScope.launch {
            repository.getTodayMatchesFromDb().collect { matches: List<Match> ->
                allMatches = matches
            }
        }
    }

    private fun observeQuery() {
        viewModelScope.launch {
            _query
                // debounce waits 500ms after user stops typing before searching
                // avoids making API call on every keystroke
                .debounce(500)
                .distinctUntilChanged()
                .filter { it.length >= 2 }
                .collect { query: String ->
                    search(query)
                }
        }
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isEmpty()) {
            _uiState.value = SearchUiState.Idle
        }
    }

    private fun search(query: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading

            // filter local matches by team name — instant, no API call
            val matchedMatches: List<Match> = allMatches.filter { match ->
                match.homeTeam.name.contains(query, ignoreCase = true) ||
                        match.awayTeam.name.contains(query, ignoreCase = true) ||
                        match.league.name.contains(query, ignoreCase = true)
            }

            // search teams from API
            val teamsResult = repository.searchTeams(query)
            val teams: List<Team> = if (teamsResult is NetworkResult.Success) {
                teamsResult.data
            } else {
                emptyList()
            }

            if (matchedMatches.isEmpty() && teams.isEmpty()) {
                _uiState.value = SearchUiState.Empty
            } else {
                _uiState.value = SearchUiState.Success(
                    teams = teams,
                    matches = matchedMatches
                )
            }
        }
    }
}