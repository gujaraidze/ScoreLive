package com.example.scorelive.presentation.competition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.scorelive.domain.model.League
import com.example.scorelive.presentation.theme.*

@Composable
fun CompetitionScreen(
    onLeagueClicked: (League) -> Unit,
    viewModel: CompetitionViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val displayedLeagues by viewModel.displayedLeagues.collectAsState()
    val favoriteIds by viewModel.favoriteLeagueIds.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // title
        Text(
            text = "Browse Competition",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
        )

        // search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF3A3A3A))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            androidx.compose.foundation.text.BasicTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TextPrimary,
                    fontSize = 14.sp
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(AccentRed),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text("Search for competition, club...", color = TextSecondary, fontSize = 14.sp)
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Top / Region / Favorites tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CompetitionBrowseTab.entries.forEach { tab ->
                BrowseTabItem(
                    label = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                    isSelected = selectedTab == tab,
                    onClick = { viewModel.onTabSelected(tab) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // section label
        if (searchQuery.isBlank()) {
            Text(
                text = when (selectedTab) {
                    CompetitionBrowseTab.TOP -> "TOP COMPETITIONS"
                    CompetitionBrowseTab.REGION -> "BY REGION"
                    CompetitionBrowseTab.FAVORITES -> "FAVORITE COMPETITIONS"
                },
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (displayedLeagues.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedTab == CompetitionBrowseTab.FAVORITES)
                        "No favorites yet — tap ☆ to add"
                    else "No results for \"$searchQuery\"",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn {
                items(displayedLeagues, key = { it.id }) { league ->
                    LeagueRow(
                        league = league,
                        isFavorite = league.id in favoriteIds,
                        onFavoriteToggle = { viewModel.toggleFavoriteLeague(league.id) },
                        onClick = { onLeagueClicked(league) }
                    )
                }
            }
        }
    }
}

@Composable
fun BrowseTabItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
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
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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

@Composable
fun LeagueRow(
    league: League,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = league.logoUrl,
            contentDescription = league.name,
            modifier = Modifier.size(36.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = league.country,
                color = TextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = league.name,
                color = Color(0xFFD2D2D2),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) AccentRed else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}