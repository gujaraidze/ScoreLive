package com.example.scorelive.presentation.matchdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.scorelive.domain.model.EventType
import com.example.scorelive.domain.model.MatchDetail
import com.example.scorelive.domain.model.MatchEvent
import com.example.scorelive.domain.model.MatchStatus
import com.example.scorelive.presentation.theme.*

@Composable
fun MatchDetailScreen(
    matchId: Int,
    onBackClicked: () -> Unit,
    viewModel: MatchDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // top bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Match Detail",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        when (val state = uiState) {
            is MatchDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentRed)
                }
            }

            is MatchDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.message, color = TextSecondary)
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

            is MatchDetailUiState.Success -> {
                MatchDetailContent(matchDetail = state.matchDetail)
            }
        }
    }
}

@Composable
fun MatchDetailContent(matchDetail: MatchDetail) {
    val match = matchDetail.match

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // score header card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // league name
                    Text(
                        text = match.league.name,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // teams and score row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // home team
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            AsyncImage(
                                model = match.homeTeam.logoUrl,
                                contentDescription = match.homeTeam.name,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = match.homeTeam.name,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // score in center
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (match.homeScore != null && match.awayScore != null)
                                    "${match.homeScore} - ${match.awayScore}"
                                else "vs",
                                color = TextPrimary,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            // live indicator with minute
                            if (match.status == MatchStatus.LIVE) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (match.minute != null) "LIVE ${match.minute}'" else "LIVE",
                                    color = AccentRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = match.status.name,
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // away team
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            AsyncImage(
                                model = match.awayTeam.logoUrl,
                                contentDescription = match.awayTeam.name,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = match.awayTeam.name,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // events section header
        if (matchDetail.events.isNotEmpty()) {
            item {
                Text(
                    text = "Match Events",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // one row per event — goal, card, substitution
            items(
                items = matchDetail.events,
                key = { "${it.minute}${it.playerName}${it.type}" }
            ) { event ->
                MatchEventRow(
                    event = event,
                    isHomeTeam = event.team.id == match.homeTeam.id
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No events yet",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// one row in the events list
// home team events on left, away team events on right
@Composable
fun MatchEventRow(
    event: MatchEvent,
    isHomeTeam: Boolean
) {
    // emoji for each event type — easy to read at a glance
    val eventIcon = when (event.type) {
        EventType.GOAL -> "⚽"
        EventType.OWN_GOAL -> "⚽ OG"
        EventType.PENALTY -> "⚽ P"
        EventType.YELLOW_CARD -> "🟨"
        EventType.RED_CARD -> "🟥"
        EventType.YELLOW_RED_CARD -> "🟨🟥"
        EventType.SUBSTITUTION -> "🔄"
        EventType.VAR -> "📺 VAR"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // home team event — left side
        if (isHomeTeam) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = eventIcon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = event.playerName,
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // minute in center
        Text(
            text = "${event.minute}'",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // away team event — right side
        if (!isHomeTeam) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.playerName,
                    color = TextPrimary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = eventIcon, fontSize = 16.sp)
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}