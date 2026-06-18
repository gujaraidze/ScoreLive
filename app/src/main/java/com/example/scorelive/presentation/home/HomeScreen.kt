package com.example.scorelive.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun HomeScreen(
    onMatchClicked: (Int) -> Unit,
    // viewModel() creates the ViewModel and ties its lifecycle to this screen
    viewModel: HomeViewModel = viewModel()
) {
    // collectAsState() observes the StateFlow and recomposes when it changes
    val uiState by viewModel.uiState.collectAsState()

    // LaunchedEffect collects one-time events like navigation
    // collectLatest cancels previous collection if new event arrives
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
        // top bar
        Text(
            text = "ScoreLive",
            color = AccentRed,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // shows different UI based on current state
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                // centered loading spinner
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentRed)
                }
            }

            is HomeUiState.Error -> {
                // error message with retry button
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
                val liveMatches = state.matches.filter {
                    it.status == MatchStatus.LIVE
                }
                val otherMatches = state.matches.filter {
                    it.status != MatchStatus.LIVE
                }

                // LazyColumn only renders visible items — efficient for long lists
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // live matches section
                    if (liveMatches.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Live Now")
                        }
                        items(
                            items = liveMatches,
                            // key helps Compose track items efficiently during updates
                            key = { it.id }
                        ) { match ->
                            MatchCard(
                                match = match,
                                onClick = { viewModel.onMatchClicked(match.id) }
                            )
                        }
                    }

                    // today's matches section
                    if (otherMatches.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Today")
                        }
                        items(
                            items = otherMatches,
                            key = { it.id }
                        ) { match ->
                            MatchCard(
                                match = match,
                                onClick = { viewModel.onMatchClicked(match.id) }
                            )
                        }
                    }

                    if (state.matches.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No matches today",
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

// reusable section header composable
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

// reusable match card composable — shown for every match in the list
@Composable
fun MatchCard(
    match: Match,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // clickable modifier makes the whole card tappable
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundCard)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // league name row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.league.name,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                // live badge or match status
                MatchStatusBadge(match = match)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // teams and score row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // home team
                TeamInfo(
                    name = match.homeTeam.name,
                    logoUrl = match.homeTeam.logoUrl,
                    modifier = Modifier.weight(1f)
                )

                // score in the middle
                ScoreDisplay(
                    homeScore = match.homeScore,
                    awayScore = match.awayScore,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // away team
                TeamInfo(
                    name = match.awayTeam.name,
                    logoUrl = match.awayTeam.logoUrl,
                    modifier = Modifier.weight(1f),
                    isAway = true
                )
            }
        }
    }
}

@Composable
fun TeamInfo(
    name: String,
    logoUrl: String,
    modifier: Modifier = Modifier,
    isAway: Boolean = false
) {
    Row(
        modifier = modifier,
        // away team is right-aligned, home team is left-aligned
        horizontalArrangement = if (isAway) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isAway) {
            // AsyncImage from Coil loads image from URL automatically
            AsyncImage(
                model = logoUrl,
                contentDescription = name,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = name,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        if (isAway) {
            Spacer(modifier = Modifier.width(6.dp))
            AsyncImage(
                model = logoUrl,
                contentDescription = name,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun ScoreDisplay(
    homeScore: Int?,
    awayScore: Int?,
    modifier: Modifier = Modifier
) {
    // if scores are null the match hasn't started yet
    val scoreText = if (homeScore != null && awayScore != null) {
        "$homeScore - $awayScore"
    } else {
        "vs"
    }

    Text(
        text = scoreText,
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun MatchStatusBadge(match: Match) {
    when (match.status) {
        MatchStatus.LIVE -> {
            // red pill badge with minute shown for live matches
            Box(
                modifier = Modifier
                    .background(
                        color = AccentRed,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (match.minute != null) "LIVE ${match.minute}'" else "LIVE",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        MatchStatus.FINISHED -> {
            Text(text = "FT", color = TextSecondary, fontSize = 12.sp)
        }
        MatchStatus.SCHEDULED -> {
            // shows kick off time for scheduled matches
            Text(
                text = match.date.take(10),
                color = StatusScheduled,
                fontSize = 12.sp
            )
        }
        else -> {
            Text(text = match.status.name, color = TextSecondary, fontSize = 12.sp)
        }
    }
}