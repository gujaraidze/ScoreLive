package com.example.scorelive.presentation.matchdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.scorelive.App
import com.example.scorelive.data.NetworkResult
import com.example.scorelive.domain.model.Match
import com.example.scorelive.domain.model.MatchDetail
import com.example.scorelive.domain.model.MatchEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MatchDetailUiState {
    object Loading : MatchDetailUiState()
    data class Success(val matchDetail: MatchDetail) : MatchDetailUiState()
    data class Error(val message: String) : MatchDetailUiState()
}

class MatchDetailViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = (application as App).repository

    // matchId passed via navigation — same as lecturer's savedStateHandle pattern
    private val matchId: Int = savedStateHandle.get<Int>("matchId") ?: 0

    private val _uiState = MutableStateFlow<MatchDetailUiState>(MatchDetailUiState.Loading)
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    init {
        loadMatchDetail()
    }

    private fun loadMatchDetail() {
        viewModelScope.launch {
            repository.getMatchByIdFromDb(matchId).collect { match ->
                if (match != null) {
                    fetchEvents(match)
                } else {
                    _uiState.value = MatchDetailUiState.Error("Match not found")
                }
            }
        }
    }

    private suspend fun fetchEvents(match: Match) {
        when (val result = repository.fetchMatchEvents(matchId)) {
            is NetworkResult.Success -> {
                _uiState.value = MatchDetailUiState.Success(
                    MatchDetail(
                        match = match,
                        events = result.data
                    )
                )
            }
            is NetworkResult.Failure -> {
                // show match without events if events fail
                _uiState.value = MatchDetailUiState.Success(
                    MatchDetail(
                        match = match,
                        events = emptyList()
                    )
                )
            }
        }
    }

    fun retry() {
        _uiState.value = MatchDetailUiState.Loading
        loadMatchDetail()
    }
}