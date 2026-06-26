package com.example.scorelive.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.scorelive.domain.model.Team
import com.example.scorelive.presentation.home.MatchCard
import com.example.scorelive.presentation.theme.*

@Composable
fun SearchScreen(
    onMatchClicked: (Int) -> Unit,
    onBackClicked: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()

    // auto-focus the keyboard when screen opens
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // clear search when leaving so it doesn't persist
    DisposableEffect(Unit) {
        onDispose { viewModel.onQueryChanged("") }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // top bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            // search input inline in the top bar
            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = { viewModel.onQueryChanged(it) },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TextPrimary,
                    fontSize = 16.sp
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(AccentRed),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text("Search teams or leagues...", color = TextSecondary, fontSize = 16.sp)
                    }
                    innerTextField()
                }
            )
        }

        Divider(color = DividerColor)

        when (val state = uiState) {
            is SearchUiState.Idle -> {
                // hint shown before user types
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Search for teams or leagues",
                        color = TextHint,
                        fontSize = 14.sp
                    )
                }
            }

            is SearchUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentRed)
                }
            }

            is SearchUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No results for \"$query\"",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            is SearchUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = TextSecondary, fontSize = 14.sp)
                }
            }

            is SearchUiState.Success -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // teams section
                    if (state.teams.isNotEmpty()) {
                        item {
                            Text(
                                text = "Teams",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(state.teams) { team ->
                            TeamSearchRow(team = team)
                        }
                    }

                    // matches section
                    if (state.matches.isNotEmpty()) {
                        item {
                            Text(
                                text = "Matches",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(
                            items = state.matches,
                            key = { it.id }
                        ) { match ->
                            MatchCard(
                                match = match,
                                onClick = { onMatchClicked(match.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamSearchRow(team: Team) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundCard, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = team.logoUrl,
            contentDescription = team.name,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = team.name,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}