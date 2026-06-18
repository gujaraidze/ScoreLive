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
        observeLiveMatches()
    }

    private fun observeLiveMatches() {
        viewModelScope.launch {
            repository.getLiveMatchesFromDb().collect { matches ->
                _uiState.value = if (matches.isEmpty()) {
                    FavoritesUiState.Empty
                } else {
                    FavoritesUiState.Success(matches)
                }
            }
        }
    }

    fun onMatchClicked(matchId: Int) {
        viewModelScope.launch {
            _event.emit(FavoritesEvent.NavigateToDetail(matchId))
        }
    }
}