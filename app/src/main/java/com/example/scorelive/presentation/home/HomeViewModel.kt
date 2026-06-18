package com.example.scorelive.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scorelive.App
import com.example.scorelive.data.NetworkResult
import com.example.scorelive.domain.model.Match
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val matches: List<Match>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

sealed class HomeEvent {
    data class NavigateToDetail(val matchId: Int) : HomeEvent()
    data class ShowError(val message: String) : HomeEvent()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as App).repository

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<HomeEvent>()
    val event = _event.asSharedFlow()

    init {
        observeMatches()
        fetchMatches()
        startLivePolling()
    }

    // observes Room DB — updates UI automatically when DB changes
    private fun observeMatches() {
        viewModelScope.launch {
            repository.getTodayMatchesFromDb().collect { matches ->
                _uiState.value = HomeUiState.Success(matches)
            }
        }
    }

    // fetches today's matches from API → saves to Room
    private fun fetchMatches() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            when (val result = repository.fetchTodayMatches(today)) {
                is NetworkResult.Failure -> {
                    if (_uiState.value is HomeUiState.Loading) {
                        _uiState.value = HomeUiState.Error(result.message)
                    }
                }
                is NetworkResult.Success -> {}
            }
        }
    }

    // polls live matches every 15 seconds
    private fun startLivePolling() {
        viewModelScope.launch {
            while (true) {
                delay(15_000)
                repository.fetchLiveMatches()
            }
        }
    }

    fun onMatchClicked(matchId: Int) {
        viewModelScope.launch {
            _event.emit(HomeEvent.NavigateToDetail(matchId))
        }
    }

    fun retry() {
        _uiState.value = HomeUiState.Loading
        fetchMatches()
    }
}