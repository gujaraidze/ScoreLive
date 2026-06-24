package com.example.scorelive.presentation.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scorelive.App
import com.example.scorelive.domain.model.Match
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    data class Success(val matches: List<Match>) : FavoritesUiState()
    object Empty : FavoritesUiState()
}

sealed class FavoritesEvent {
    data class NavigateToDetail(val matchId: Int) : FavoritesEvent()
}

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as App).repository

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<FavoritesEvent>()
    val event = _event.asSharedFlow()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            // combine merges two flows into one
            // whenever favorites list OR matches list changes — UI updates
            combine(
                repository.getFavoriteIds(),
                repository.getTodayMatchesFromDb()
            ) { favoriteIds: List<Int>, allMatches: List<Match> ->
                // filter matches to only show favorited ones
                allMatches.filter { match -> match.id in favoriteIds }
            }.collect { favoriteMatches: List<Match> ->
                _uiState.value = if (favoriteMatches.isEmpty()) {
                    FavoritesUiState.Empty
                } else {
                    FavoritesUiState.Success(favoriteMatches)
                }
            }
        }
    }

    fun onMatchClicked(matchId: Int) {
        viewModelScope.launch {
            _event.emit(FavoritesEvent.NavigateToDetail(matchId))
        }
    }

    fun removeFavorite(matchId: Int) {
        viewModelScope.launch {
            repository.removeFavorite(matchId)
        }
    }
}