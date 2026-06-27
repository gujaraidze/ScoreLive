package com.example.scorelive.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.scorelive.domain.model.Match
import com.example.scorelive.domain.model.MatchStatus
import com.example.scorelive.presentation.theme.*
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

@Composable
fun HomeScreen(
    onMatchClicked: (Int) -> Unit,
    onSearchClicked: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dateOptions by viewModel.dateOptions.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is HomeEvent.NavigateToDetail -> onMatchClicked(event.matchId)
                is HomeEvent.ShowError -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // top bar — lowercase wordmark with red notification dot, search + bell icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "scorelive",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .padding(start = 2.dp, top = 2.dp)
                        .size(7.dp)
                        .background(color = AccentRed, shape = androidx.compose.foundation.shape.CircleShape)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextPrimary,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onSearchClicked() }
                )
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // date selector strip — 5 fixed days + arrow navigation
        DateSelectorRow(
            dateOptions = dateOptions,
            selectedDate = selectedDate,
            onDateSelected = { viewModel.onDateSelected(it) },
            onPreviousDay = { viewModel.onPreviousDay() },
            onNextDay = { viewModel.onNextDay() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentRed)
                }
            }

            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.retry() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            is HomeUiState.Success -> {
                val isEmpty = state.liveMatches.isEmpty() && state.leagueGroups.isEmpty()
                val listState = androidx.compose.foundation.lazy.rememberLazyListState()

                // scroll to top instantly whenever the tab or date changes
                LaunchedEffect(selectedTab, selectedDate) {
                    listState.scrollToItem(0)
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Live Now — horizontal scrollable carousel, mixed across all leagues,
                    // unaffected by the Upcoming/Score/Favorites tab below
                    if (state.liveMatches.isNotEmpty()) {
                        item(key = "live_now_header") {
                            Text(
                                text = "Live Now",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        item(key = "live_now_carousel") {
                            if (state.liveMatches.size == 1) {
                                // single card — center it instead of left-aligning
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LiveMatchCard(
                                        match = state.liveMatches.first(),
                                        onClick = { viewModel.onMatchClicked(state.liveMatches.first().id) }
                                    )
                                }
                            } else {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(
                                        items = state.liveMatches,
                                        key = { "live_${it.id}" }
                                    ) { match ->
                                        LiveMatchCard(
                                            match = match,
                                            onClick = { viewModel.onMatchClicked(match.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Upcoming / Score / Favorites — only show tabs that have matches
                    if (state.availableTabs.isNotEmpty()) {
                        item(key = "home_tabs") {
                            HomeTabRow(
                                selectedTab = selectedTab,
                                availableTabs = state.availableTabs,
                                onTabSelected = { viewModel.onTabSelected(it) }
                            )
                        }
                    }

                    // remaining matches (filtered by the active tab), grouped by league
                    state.leagueGroups.forEach { group ->
                        item(key = "league_header_${group.leagueId}") {
                            LeagueHeader(
                                name = group.leagueName,
                                logoUrl = group.leagueLogoUrl,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        items(
                            items = group.matches,
                            key = { "match_${it.id}" }
                        ) { match ->
                            MatchCard(
                                match = match,
                                onClick = { viewModel.onMatchClicked(match.id) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    if (isEmpty || (state.liveMatches.isEmpty() && state.leagueGroups.isEmpty())) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (selectedTab) {
                                        HomeTab.UPCOMING -> "No upcoming matches on this day"
                                        HomeTab.SCORE -> "No matches on this day"
                                    },
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Figma style date strip — fixed 5 days, arrows shift the window by 1 day
@Composable
fun DateSelectorRow(
    dateOptions: List<DateOption>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousDay, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous day",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dateOptions.forEach { option ->
                val isSelected = option.date == selectedDate
                DateChip(
                    option = option,
                    isSelected = isSelected,
                    onClick = { onDateSelected(option.date) }
                )
            }
        }

        IconButton(onClick = onNextDay, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next day",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DateChip(
    option: DateOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = option.dayLabel,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
        Text(
            text = option.dateLabel,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(6.dp))
        // red underline only shows beneath the selected day
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(36.dp)
                .background(
                    color = if (isSelected) AccentRed else Color.Transparent,
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}

@Composable
fun LeagueHeader(name: String, logoUrl: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = logoUrl,
            contentDescription = name,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = name,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
fun MatchCard(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // status/time shown once on the left, stacked above the home/away rows
    val statusLines = matchStatusLines(match)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundCard)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // left column — status (FT/LIVE/time) + date, same for both rows
        Column(
            modifier = Modifier.width(34.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = statusLines.first,
                color = if (match.status == MatchStatus.LIVE) AccentRed else TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            val secondaryLabel = statusLines.second
            if (secondaryLabel != null) {
                Text(
                    text = secondaryLabel,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // two stacked team rows: crest + name on the left, score on the right
        Column(modifier = Modifier.weight(1f)) {
            MatchTeamRow(
                name = match.homeTeam.name,
                logoUrl = match.homeTeam.logoUrl,
                score = match.homeScore
            )
            Spacer(modifier = Modifier.height(8.dp))
            MatchTeamRow(
                name = match.awayTeam.name,
                logoUrl = match.awayTeam.logoUrl,
                score = match.awayScore
            )
        }
    }
}

// one team's crest + name + score, used for both rows inside MatchCard
@Composable
fun MatchTeamRow(name: String, logoUrl: String, score: Int?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = logoUrl,
            contentDescription = name,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = score?.toString() ?: "",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// returns (primary status label, optional date/secondary label) for the left column
// For a live match, shows "HT" at half-time, "PEN" during penalties, "ET" during the
// pre-extra-time break, and otherwise the running minute ("67'"). Without this, half-time
// would sit frozen on "45'" because the API keeps elapsed=45 until the second half starts.
fun liveMinuteLabel(match: Match): String = when (match.statusShort) {
    "HT" -> "HT"
    "P" -> "PEN"
    "BT" -> "ET"
    else -> match.minute?.let { "$it'" } ?: "LIVE"
}

private fun matchStatusLines(match: Match): Pair<String, String?> {
    return when (match.status) {
        MatchStatus.LIVE -> liveMinuteLabel(match) to null
        MatchStatus.FINISHED -> "FT" to match.date.substringBefore("T").let {
            val parts = it.split("-")
            if (parts.size == 3) "${parts[2]}/${parts[1]}" else it
        }
        MatchStatus.SCHEDULED -> match.date.substringAfter("T").take(5) to null
        else -> match.status.name to null
    }
}

// Live Now horizontal carousel card — bigger, with league header + Details button
@Composable
fun LiveMatchCard(match: Match, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(310.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AsyncImage(
                    model = match.league.logoUrl,
                    contentDescription = match.league.name,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = match.league.name,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
            LiveMinutePill(label = liveMinuteLabel(match))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            LiveTeamColumn(name = match.homeTeam.name, logoUrl = match.homeTeam.logoUrl, modifier = Modifier.weight(1f))

            Text(
                text = "${match.homeScore ?: 0} - ${match.awayScore ?: 0}",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            LiveTeamColumn(name = match.awayTeam.name, logoUrl = match.awayTeam.logoUrl, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
        ) {
            Text(text = "Details", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun LiveTeamColumn(name: String, logoUrl: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = logoUrl,
            contentDescription = name,
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = shortDisplayName(name),
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

// pale-mint pill with a pulsing green dot + minute count, matches Figma exactly
@Composable
fun LiveMinutePill(label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFECFDF3))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color = LiveGreen, shape = androidx.compose.foundation.shape.CircleShape)
        )
        Text(
            text = label,
            color = Color(0xFF0F1729),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Upcoming / Score / Favorites — only shows tabs that have matches for the current date
@Composable
fun HomeTabRow(
    selectedTab: HomeTab,
    availableTabs: List<HomeTab>,
    onTabSelected: (HomeTab) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            availableTabs.forEach { tab ->
                val label = when (tab) {
                    HomeTab.UPCOMING -> "Upcoming"
                    HomeTab.SCORE -> "Score"
                }
                HomeTabItem(label = label, isSelected = selectedTab == tab) {
                    onTabSelected(tab)
                }
            }
        }
    }
}

@Composable
fun HomeTabItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(60.dp)
                .background(
                    color = if (isSelected) AccentRed else Color.Transparent,
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}

private fun shortDisplayName(fullName: String): String {
    val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
    return if (parts.size <= 2) fullName else "${parts.first()} ${parts.last()}"
}