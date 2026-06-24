package com.example.scorelive.presentation.competition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.scorelive.domain.model.Match
import com.example.scorelive.presentation.home.MatchCard
import com.example.scorelive.presentation.theme.*

@Composable
fun CompetitionDetailScreen(
    leagueId: Int,
    leagueName: String,
    logoUrl: String,
    country: String,
    onBackClicked: () -> Unit,
    onMatchClicked: (Int) -> Unit,
    viewModel: CompetitionDetailViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val results by viewModel.results.collectAsState()
    val fixtures by viewModel.fixtures.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClicked) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Competition",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = {}) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = TextPrimary)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.StarBorder, contentDescription = "Favorite", tint = TextPrimary)
            }
        }

        // league header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = logoUrl,
                contentDescription = leagueName,
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = country, color = TextSecondary, fontSize = 13.sp)
            Text(
                text = leagueName,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Results / Fixtures tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CompetitionDetailTab.entries.forEach { tab ->
                CompDetailTabItem(
                    label = when (tab) {
                        CompetitionDetailTab.RESULTS -> "Results"
                        CompetitionDetailTab.FIXTURES -> "Fixtures"
                    },
                    isSelected = selectedTab == tab,
                    onClick = { viewModel.onTabSelected(tab) }
                )
            }
        }

        // content — pure DB read, no loading state needed
        when (selectedTab) {
            CompetitionDetailTab.RESULTS -> CompMatchList(
                matches = results,
                onMatchClicked = onMatchClicked,
                emptyMessage = "No results found.\nResults appear here once matches from\nthe Home screen have been loaded."
            )
            CompetitionDetailTab.FIXTURES -> CompMatchList(
                matches = fixtures,
                onMatchClicked = onMatchClicked,
                emptyMessage = "No upcoming fixtures found.\nFixtures appear here once matches\nhave been loaded on the Home screen."
            )
        }
    }
}

@Composable
fun CompDetailTabItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(50.dp)
                .background(
                    color = if (isSelected) AccentRed else Color.Transparent,
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}

@Composable
fun CompMatchList(matches: List<Match>, onMatchClicked: (Int) -> Unit, emptyMessage: String) {
    if (matches.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyMessage,
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
        return
    }

    val grouped = matches.groupBy { it.date.substringBefore("T") }

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        grouped.forEach { (date, dayMatches) ->
            item {
                val parts = date.split("-")
                val label = if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else date
                Text(
                    text = label,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(dayMatches, key = { it.id }) { match ->
                MatchCard(
                    match = match,
                    onClick = { onMatchClicked(match.id) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}