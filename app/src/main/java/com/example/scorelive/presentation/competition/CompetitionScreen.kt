package com.example.scorelive.presentation.competition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorelive.presentation.home.MatchCard
import com.example.scorelive.presentation.theme.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CompetitionScreen(
    onMatchClicked: (Int) -> Unit,
    viewModel: CompetitionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedLeague by viewModel.selectedLeague.collectAsState()

    // collect navigation events from SharedFlow
    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is CompetitionEvent.NavigateToDetail -> onMatchClicked(event.matchId)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Text(
            text = "Competition",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // LazyRow scrolls horizontally — perfect for league chips
        // only renders visible items, efficient for many leagues
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FootballLeague.entries) { league ->
                LeagueChip(
                    league = league,
                    isSelected = league == selectedLeague,
                    onClick = { viewModel.onLeagueSelected(league) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (val state = uiState) {
            is CompetitionUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentRed)
                }
            }

            is CompetitionUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            is CompetitionUiState.Success -> {
                if (state.matches.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matches for ${selectedLeague.displayName}",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // LazyColumn for vertical list of matches
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = state.matches,
                            key = { it.id }
                        ) { match ->
                            // reusing MatchCard from HomeScreen — no duplication
                            MatchCard(
                                match = match,
                                onClick = { viewModel.onMatchClicked(match.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// chip composable for each league in the horizontal row
@Composable
fun LeagueChip(
    league: FootballLeague,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(
                // selected chip has red background, unselected is transparent
                color = if (isSelected) AccentRed else BackgroundCard,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) AccentRed else DividerColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = league.displayName,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}