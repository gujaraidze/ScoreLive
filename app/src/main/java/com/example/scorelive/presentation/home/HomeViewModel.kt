package com.example.scorelive.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.scorelive.App
import com.example.scorelive.data.NetworkResult
import com.example.scorelive.domain.model.Match
import com.example.scorelive.domain.model.MatchStatus
import com.example.scorelive.domain.model.LeaguePriority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val liveMatches: List<Match>,
        val leagueGroups: List<LeagueGroup>,
        val availableTabs: List<HomeTab>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

enum class HomeTab { UPCOMING, SCORE }

// one league's worth of matches, in the order leagues first appeared in the API response
data class LeagueGroup(
    val leagueId: Int,
    val leagueName: String,
    val leagueLogoUrl: String,
    val matches: List<Match>
)

sealed class HomeEvent {
    data class NavigateToDetail(val matchId: Int) : HomeEvent()
    data class ShowError(val message: String) : HomeEvent()
}

// represents one day in the date strip
data class DateOption(
    val date: LocalDate,
    val dayLabel: String,   // "Fri", "Today", "Sat"
    val dateLabel: String   // "18 Apr"
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as App).repository

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<HomeEvent>()
    val event = _event.asSharedFlow()

    // currently selected date in the strip — defaults to today
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // window always keeps today visible — starts 1 day back so we can see yesterday's results
    private val _windowStart = MutableStateFlow(LocalDate.now().minusDays(2))
    val windowStart: StateFlow<LocalDate> = _windowStart.asStateFlow()

    // which of the Upcoming / Score tabs is active
    private val _selectedTab = MutableStateFlow(HomeTab.SCORE)
    val selectedTab: StateFlow<HomeTab> = _selectedTab.asStateFlow()

    // 5 visible dates, recalculated whenever windowStart changes
    val dateOptions: StateFlow<List<DateOption>> = MutableStateFlow(buildDateOptions(_windowStart.value)).also { state ->
        viewModelScope.launch {
            _windowStart.collect { start ->
                state.value = buildDateOptions(start)
            }
        }
    }

    private val fetchedDates = mutableSetOf<String>()

    // SharedPreferences stores last-fetch timestamps so rebuilding doesn't
    // trigger redundant API calls — past dates are considered fresh for 24h,
    // today/future for 5 minutes (since live match data changes frequently)
    private val prefs = application.getSharedPreferences("fetch_cache", android.content.Context.MODE_PRIVATE)

    private fun shouldFetchDate(dateStr: String): Boolean {
        val lastFetch = prefs.getLong("lastFetch_$dateStr", 0L)
        if (lastFetch == 0L) return true
        val ageMs = System.currentTimeMillis() - lastFetch
        val today = LocalDate.now()
        val date = LocalDate.parse(dateStr)
        return when {
            // today and future: 5 min TTL (live scores change frequently)
            !date.isBefore(today) -> ageMs > 5 * 60 * 1000L
            // yesterday: 30 min TTL (late matches may still be finishing)
            date == today.minusDays(1) -> ageMs > 30 * 60 * 1000L
            // older past dates: 24h TTL (fully finished, won't change)
            else -> ageMs > 24 * 60 * 60 * 1000L
        }
    }

    private fun markDateFetched(dateStr: String) {
        prefs.edit().putLong("lastFetch_$dateStr", System.currentTimeMillis()).apply()
    }

    init {
        observeMatches()
        // pre-fetch all 5 visible dates in parallel so the strip is populated on launch
        // TTL gates in fetchMatchesForDate prevent redundant calls if already fresh
        val windowStart = _windowStart.value
        (0..4).forEach { i ->
            fetchMatchesForDate(windowStart.plusDays(i.toLong()))
        }
    }

    private fun buildDateOptions(windowStart: LocalDate): List<DateOption> {
        val today = LocalDate.now()
        val dayFormatter = DateTimeFormatter.ofPattern("EEE")
        val dateFormatter = DateTimeFormatter.ofPattern("d MMM")
        return (0..4).map { i ->
            val date = windowStart.plusDays(i.toLong())
            DateOption(
                date = date,
                dayLabel = if (date == today) "Today" else date.format(dayFormatter),
                dateLabel = date.format(dateFormatter)
            )
        }
    }

    // flatMapLatest cancels the previous date's Flow and subscribes to the new one.
    // combine() layers in the active tab + favorite ids so switching tabs filters
    // the league-grouped list without needing a new network fetch.
    private fun observeMatches() {
        val matchesFlow = _selectedDate.flatMapLatest { date ->
            repository.getMatchesByDateFromDb(date.toString())
        }

        kotlinx.coroutines.flow.combine(
            matchesFlow,
            _selectedTab
        ) { matches, tab ->
            Pair(matches, tab)
        }
            .onEach { (matches, tab) ->
                val currentDateStr = _selectedDate.value.toString()
                _uiState.value = if (matches.isEmpty() && currentDateStr !in fetchedDates) {
                    HomeUiState.Loading
                } else {
                    // live matches are always pinned at the top regardless of tab,
                    // exactly as they are today
                    val live = matches
                        .filter { it.status == MatchStatus.LIVE }
                        .sortedWith(
                            compareBy(
                                { LeaguePriority.rankFor(it.league.id) },
                                { it.league.name }
                            )
                        )

                    // compute which tabs to show:
                    // Score: only when there are finished matches
                    // Upcoming: only for today and future dates
                    val today = java.time.LocalDate.now()
                    val selectedDateLocal = java.time.LocalDate.parse(_selectedDate.value.toString())
                    val isPastDate = selectedDateLocal.isBefore(today)

                    val hasFinished = matches.any { it.status == MatchStatus.FINISHED }
                    val hasScheduled = matches.any { it.status == MatchStatus.SCHEDULED }

                    val availableTabs = buildList {
                        if (hasFinished) add(HomeTab.SCORE)
                        if (hasScheduled && !isPastDate) add(HomeTab.UPCOMING)
                    }

                    val activeTab = if (availableTabs.contains(tab)) tab
                    else availableTabs.firstOrNull() ?: HomeTab.SCORE
                    if (activeTab != tab) _selectedTab.value = activeTab

                    val filtered = when (activeTab) {
                        HomeTab.SCORE -> matches.filter { it.status == MatchStatus.FINISHED }
                        HomeTab.UPCOMING -> matches.filter { it.status == MatchStatus.SCHEDULED }
                    }

                    val groups = filtered
                        .groupBy { it.league.id }
                        .map { (_, leagueMatches) ->
                            val league = leagueMatches.first().league
                            LeagueGroup(
                                leagueId = league.id,
                                leagueName = league.name,
                                leagueLogoUrl = league.logoUrl,
                                matches = leagueMatches
                            )
                        }
                        .sortedWith(
                            compareBy(
                                { LeaguePriority.rankFor(it.leagueId) },
                                { it.leagueName }
                            )
                        )
                    HomeUiState.Success(
                        liveMatches = live,
                        leagueGroups = groups,
                        availableTabs = availableTabs
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onTabSelected(tab: HomeTab) {
        _selectedTab.value = tab
    }

    // pick the most sensible tab for a given date:
    // past dates → Score (results), today → Score (mix of live/finished), future → Upcoming
    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
        val windowEnd = _windowStart.value.plusDays(4)
        if (date < _windowStart.value || date > windowEnd) {
            _windowStart.value = date.minusDays(1)
        }
        fetchMatchesForDate(date)
    }

    fun onPreviousDay() {
        _windowStart.value = _windowStart.value.minusDays(1)
        val newDate = _selectedDate.value.minusDays(1)
        _selectedDate.value = newDate
        fetchMatchesForDate(newDate)
        fetchMatchesForDate(_windowStart.value)
    }

    fun onNextDay() {
        _windowStart.value = _windowStart.value.plusDays(1)
        val newDate = _selectedDate.value.plusDays(1)
        _selectedDate.value = newDate
        fetchMatchesForDate(newDate)
        fetchMatchesForDate(_windowStart.value.plusDays(4))
    }

    private fun fetchMatchesForDate(date: LocalDate) {
        val dateStr = date.toString()
        if (dateStr in fetchedDates) return
        // skip API call if Room already has fresh data for this date
        if (!shouldFetchDate(dateStr)) {
            android.util.Log.d("API_LIMIT", "Skipping fetch for $dateStr — data is fresh (TTL not expired)")
            return
        }
        viewModelScope.launch {
            fetchedDates.add(dateStr)
            android.util.Log.d("API_LIMIT", "Fetching date: $dateStr — 1 call used")
            when (val result = repository.fetchMatchesByDate(dateStr)) {
                is NetworkResult.Failure -> {
                    fetchedDates.remove(dateStr)
                    if (_uiState.value is HomeUiState.Loading) {
                        _uiState.value = HomeUiState.Error(result.message)
                    }
                }
                is NetworkResult.Success -> {
                    markDateFetched(dateStr)
                }
            }
        }
    }

    fun onMatchClicked(matchId: Int) {
        viewModelScope.launch {
            _event.emit(HomeEvent.NavigateToDetail(matchId))
        }
    }

    fun retry() {
        fetchedDates.remove(_selectedDate.value.toString())
        _uiState.value = HomeUiState.Loading
        fetchMatchesForDate(_selectedDate.value)
    }
}