package com.example.scorelive.presentation.matchdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.scorelive.presentation.home.LiveMinutePill
import com.example.scorelive.presentation.home.liveMinuteLabel
import com.example.scorelive.domain.model.*
import com.example.scorelive.presentation.theme.*

@Composable
fun MatchDetailScreen(
    matchId: Int,
    onBackClicked: () -> Unit,
    viewModel: MatchDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isFavorite by viewModel.isFavoriteState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        when (val state = uiState) {
            is MatchDetailUiState.Loading -> {
                // top bar still shown while loading
                TopBar(
                    title = "Match Detail",
                    subtitle = "",
                    isFavorite = false,
                    onBackClicked = onBackClicked,
                    onShareClicked = {},
                    onFavoriteClicked = {}
                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentRed)
                }
            }

            is MatchDetailUiState.Error -> {
                TopBar(
                    title = "Match Detail",
                    subtitle = "",
                    isFavorite = false,
                    onBackClicked = onBackClicked,
                    onShareClicked = {},
                    onFavoriteClicked = {}
                )
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
                val match = state.matchDetail.match
                val context = androidx.compose.ui.platform.LocalContext.current

                TopBar(
                    title = match.league.name,
                    subtitle = matchDateSubtitle(match.date),
                    isFavorite = isFavorite,
                    onBackClicked = onBackClicked,
                    onShareClicked = {
                        val shareText = "${match.homeTeam.name} ${match.homeScore ?: 0} - " +
                                "${match.awayScore ?: 0} ${match.awayTeam.name} · ${match.league.name}"
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                    },
                    onFavoriteClicked = { viewModel.toggleFavorite() }
                )

                // score header — always visible
                MatchScoreHeader(match = match, events = state.matchDetail.events)

                // custom horizontally-scrollable tab row — same underline language as
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MatchDetailTab.entries.forEach { tab ->
                        DetailTabItem(
                            label = when (tab) {
                                MatchDetailTab.SUMMARY -> "Summary"
                                MatchDetailTab.LINEUP -> "Line Up"
                                MatchDetailTab.STATS -> "Stats"
                                MatchDetailTab.H2H -> "H2H"
                                MatchDetailTab.STANDINGS -> "Standings"
                            },
                            isSelected = selectedTab == tab,
                            onClick = { viewModel.onTabSelected(tab) }
                        )
                    }
                }

                // tab content — weight(1f) gives it all the remaining space and it scrolls
                // internally, so a tall score header can't squeeze it (or the tabs) off-screen
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (selectedTab) {
                        MatchDetailTab.SUMMARY -> SummaryTab(
                            events = state.matchDetail.events,
                            match = match
                        )
                        MatchDetailTab.LINEUP -> LineupTab(
                            lineups = state.matchDetail.lineups,
                            match = match
                        )
                        MatchDetailTab.STATS -> StatsTab(
                            statistics = state.matchDetail.statistics,
                            match = match
                        )
                        MatchDetailTab.H2H -> H2HTab(
                            matches = state.matchDetail.h2h,
                            homeTeam = match.homeTeam,
                            awayTeam = match.awayTeam
                        )
                        MatchDetailTab.STANDINGS -> StandingsTab(
                            standings = state.matchDetail.standings,
                            homeTeamId = match.homeTeam.id,
                            awayTeamId = match.awayTeam.id
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(
    title: String,
    subtitle: String,
    isFavorite: Boolean,
    onBackClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onFavoriteClicked: () -> Unit
) {
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

        // two-line centered title block — league/match name on top, "Today"/date below
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1
            )
        }

        IconButton(onClick = onShareClicked) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = TextPrimary
            )
        }
        // star icon — filled if favorited, outline if not
        IconButton(onClick = onFavoriteClicked) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) AccentRed else TextPrimary
            )
        }
    }
}

// "Today" if the match date matches today's date, otherwise a short "16 Apr" style label
private fun matchDateSubtitle(isoDate: String): String {
    return try {
        val datePart = isoDate.substringBefore("T")
        val matchDate = java.time.LocalDate.parse(datePart)
        if (matchDate == java.time.LocalDate.now()) {
            "Today"
        } else {
            val month = matchDate.month.getDisplayName(
                java.time.format.TextStyle.SHORT,
                java.util.Locale.ENGLISH
            )
            "${matchDate.dayOfMonth} $month"
        }
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun DetailTabItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
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
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(if (isSelected) 50.dp else 0.dp)
                .background(
                    color = if (isSelected) AccentRed else Color.Transparent,
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}

@Composable
fun MatchScoreHeader(match: Match, events: List<MatchEvent> = emptyList()) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundCard)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

            // league row — crest + name on the left, live-minute pill on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = match.league.logoUrl,
                        contentDescription = match.league.name,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = match.league.name,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                if (match.status == MatchStatus.LIVE) {
                    LiveMinutePill(label = liveMinuteLabel(match))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // crests + score row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = match.homeTeam.logoUrl,
                        contentDescription = match.homeTeam.name,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.homeTeam.name,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = if (match.homeScore != null && match.awayScore != null)
                        "${match.homeScore} - ${match.awayScore}" else "vs",
                    color = TextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = match.awayTeam.logoUrl,
                        contentDescription = match.awayTeam.name,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.awayTeam.name,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // goal-scorers footer — only shown once the match has kicked off and has goals
            val scorers = events.filter {
                it.type == EventType.GOAL || it.type == EventType.OWN_GOAL || it.type == EventType.PENALTY
            }
            if (scorers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = DividerColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // split by team so each side's scorers sit under its own crest.
                // goalCountsForHomeTeam handles own goals (they count for the other team).
                val homeScorers = scorers.filter { goalCountsForHomeTeam(it, match) }
                val awayScorers = scorers.filter { !goalCountsForHomeTeam(it, match) }
                // safety cap so a freak scoreline can't make the header grow without bound
                val maxPerSide = 8

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // home scorers — left
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        homeScorers.take(maxPerSide).forEach { event ->
                            Text(
                                text = "${event.playerName} ${event.minute}'",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        if (homeScorers.size > maxPerSide) {
                            Text(
                                text = "+${homeScorers.size - maxPerSide} more",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = "Goals",
                        tint = TextSecondary,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(18.dp)
                    )

                    // away scorers — right
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        awayScorers.take(maxPerSide).forEach { event ->
                            Text(
                                text = "${event.minute}' ${event.playerName}",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.End
                            )
                        }
                        if (awayScorers.size > maxPerSide) {
                            Text(
                                text = "+${awayScorers.size - maxPerSide} more",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---- Summary Tab ----
@Composable
fun SummaryTab(events: List<MatchEvent>, match: Match) {
    if (events.isEmpty()) {
        EmptyTabMessage("No events yet")
        return
    }

    // sort chronologically, then figure out where Half Time belongs by minute,
    // and compute the half-time score by counting goals up to and including minute 45
    val sortedEvents = events.sortedBy { it.minute }
    val firstHalfGoals = sortedEvents.filter {
        it.minute <= 45 && (it.type == EventType.GOAL || it.type == EventType.OWN_GOAL || it.type == EventType.PENALTY)
    }
    val htHomeScore = firstHalfGoals.count { goalCountsForHomeTeam(it, match) }
    val htAwayScore = firstHalfGoals.size - htHomeScore
    val hasSecondHalfEvents = sortedEvents.any { it.minute > 45 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // the whole timeline lives inside one card — rows are separated by spacing only,
        // no per-row background, matching the rest of the app's card language
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BackgroundCard)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            var halfTimeInserted = false
            sortedEvents.forEach { event ->
                // insert the Half Time marker right before the first second-half event
                if (!halfTimeInserted && event.minute > 45 && hasSecondHalfEvents) {
                    HalfTimeMarker(
                        homeTeamName = match.homeTeam.name,
                        awayTeamName = match.awayTeam.name,
                        homeScore = htHomeScore,
                        awayScore = htAwayScore
                    )
                    halfTimeInserted = true
                }
                MatchEventRow(
                    event = event,
                    isHomeTeam = event.team.id == match.homeTeam.id
                )
            }
        }
    }
}

// own goals count for the opposing team's scoreline
private fun goalCountsForHomeTeam(event: MatchEvent, match: Match): Boolean {
    val scoredByHome = event.team.id == match.homeTeam.id
    return if (event.type == EventType.OWN_GOAL) !scoredByHome else scoredByHome
}

@Composable
fun HalfTimeMarker(homeTeamName: String, awayTeamName: String, homeScore: Int, awayScore: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Half Time",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$homeTeamName $homeScore-$awayScore $awayTeamName",
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
fun MatchEventRow(event: MatchEvent, isHomeTeam: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = if (isHomeTeam) Arrangement.Start else Arrangement.End
    ) {
        if (isHomeTeam) {
            // minute → icon → text, flush against the left edge, nothing on the right
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "${event.minute}'", color = TextSecondary, fontSize = 12.sp)
                EventIcon(event = event)
                EventContent(event = event, alignEnd = false)
            }
        } else {
            // text → icon → minute, flush against the right edge, nothing on the left
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                EventContent(event = event, alignEnd = true)
                EventIcon(event = event)
                Text(text = "${event.minute}'", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

// player name + secondary line (assist, or in/out for substitutions)
@Composable
fun EventContent(event: MatchEvent, alignEnd: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        when (event.type) {
            EventType.SUBSTITUTION -> {
                // API-Football convention for "subst" events: `player` is who comes OFF,
                // `assist` is who comes ON — so the "In" line uses assistName, "Out" uses playerName
                if (!event.assistName.isNullOrBlank()) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = LiveGreen)) { append("In: ") }
                            withStyle(SpanStyle(color = TextPrimary)) { append(event.assistName) }
                        },
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = AccentRed)) { append("Out: ") }
                        withStyle(SpanStyle(color = TextSecondary)) { append(event.playerName) }
                    },
                    fontSize = 13.sp
                )
            }
            EventType.GOAL, EventType.PENALTY -> {
                Text(text = event.playerName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (!event.assistName.isNullOrBlank()) {
                    Text(text = "Asst: ${event.assistName}", color = TextSecondary, fontSize = 12.sp)
                }
            }
            else -> {
                Text(text = event.playerName, color = TextPrimary, fontSize = 14.sp)
            }
        }
    }
}

// the icon (ball/card/swap) plus the minute, grouped together like the Figma reference —
// icon sits closer to center, minute sits on the outer edge
@Composable
fun EventIcon(event: MatchEvent) {
    when (event.type) {
        EventType.GOAL, EventType.PENALTY, EventType.OWN_GOAL -> {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = "Goal",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
        EventType.YELLOW_CARD -> CardIcon(color = Color(0xFFF7B500))
        EventType.RED_CARD -> CardIcon(color = AccentRed)
        EventType.YELLOW_RED_CARD -> CardIcon(color = AccentRed)
        EventType.SUBSTITUTION -> {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Substitution",
                tint = LiveGreen,
                modifier = Modifier.size(16.dp)
            )
        }
        EventType.VAR -> {
            Text(text = "VAR", color = TextSecondary, fontSize = 10.sp)
        }
    }
}

// small solid rounded rectangle standing in for a card icon — closer to the real
// card shape than a Material icon glyph would give us
@Composable
fun CardIcon(color: Color) {
    Box(
        modifier = Modifier
            .size(width = 11.dp, height = 15.dp)
            .background(color = color, shape = RoundedCornerShape(2.dp))
    )
}

// ---- Lineup Tab ----
@Composable
fun LineupTab(lineups: List<Lineup>, match: Match) {
    if (lineups.isEmpty()) {
        EmptyTabMessage("Lineups not available yet")
        return
    }

    // guarantee home is always shown on the left regardless of API response order
    val homeLineup = lineups.firstOrNull { it.team.id == match.homeTeam.id } ?: lineups.getOrNull(0)
    val awayLineup = lineups.firstOrNull { it.team.id == match.awayTeam.id } ?: lineups.getOrNull(1)

    if (homeLineup == null || awayLineup == null) {
        EmptyTabMessage("Lineups not available yet")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(16.dp)
    ) {
        FormationPitch(homeLineup = homeLineup, awayLineup = awayLineup)

        Spacer(modifier = Modifier.height(20.dp))

        LineupSection(
            title = "SUBSTITUTES",
            homeTeamLogo = homeLineup.team.logoUrl,
            awayTeamLogo = awayLineup.team.logoUrl,
            homePlayers = homeLineup.substitutes,
            awayPlayers = awayLineup.substitutes
        )

        if (!homeLineup.coachName.isNullOrBlank() || !awayLineup.coachName.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(20.dp))
            ManagerSection(
                homeCoachName = homeLineup.coachName,
                awayCoachName = awayLineup.coachName
            )
        }
    }
}

// mirrors LineupSection's layout but for a single name per side, no jersey number —
// matches Figma's "MANAGER" row exactly
@Composable
fun ManagerSection(homeCoachName: String?, awayCoachName: String?) {
    Divider(color = DividerColor, thickness = 1.dp)
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "MANAGER",
        color = TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = homeCoachName ?: "—",
            color = TextPrimary,
            fontSize = 13.sp
        )
        Text(
            text = awayCoachName ?: "—",
            color = TextPrimary,
            fontSize = 13.sp,
            textAlign = TextAlign.End
        )
    }
}

// the shared pitch for both teams — home team attacks downward from the top goal,
// away team attacks upward from the bottom goal, exactly like Figma's reference
@Composable
fun FormationPitch(homeLineup: Lineup, awayLineup: Lineup) {
    val homeRows = groupByGridRow(homeLineup.startXI)
    val awayRows = groupByGridRow(awayLineup.startXI)

    Column(modifier = Modifier.fillMaxWidth()) {
        // home team label + formation, top-left / top-right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = homeLineup.team.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = homeLineup.formation, color = TextSecondary, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1A1A))
        ) {
            PitchMarkings(modifier = Modifier.fillMaxSize())

            Column(modifier = Modifier.fillMaxSize()) {
                // home half — goalkeeper's row (row 1) nearest the top goal line,
                // increasing row numbers move down toward the center of the pitch
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    homeRows.forEach { row ->
                        PitchRow(players = row, badgeColor = AccentRed, numberColor = Color.White)
                    }
                }
                // away half — mirrored: goalkeeper's row nearest the bottom goal line,
                // so we reverse the row order when laying them out top-to-bottom.
                // away badges are white-filled with a dark number, matching Figma
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    awayRows.reversed().forEach { row ->
                        PitchRow(players = row, badgeColor = Color.White, numberColor = BackgroundDark)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // away team label + formation, bottom-left / bottom-right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = awayLineup.team.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = awayLineup.formation, color = TextSecondary, fontSize = 13.sp)
        }
    }
}

// groups players by their grid row, sorted by column so they render left-to-right correctly;
// players missing grid data (shouldn't happen for startXI, but defensive) are dropped silently
// rather than crashing the formation view
private fun groupByGridRow(players: List<Player>): List<List<Player>> {
    return players
        .filter { it.gridRow != null && it.gridCol != null }
        .groupBy { it.gridRow }
        .toList()
        .sortedBy { (row, _) -> row }
        .map { (_, rowPlayers) -> rowPlayers.sortedBy { it.gridCol } }
}

@Composable
fun PitchRow(players: List<Player>, badgeColor: Color, numberColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        players.forEach { player ->
            // fixed-width slot per player — keeps circles evenly spaced and vertically
            // aligned with each other even when names differ in length, since the name
            // truncates/wraps inside its own slot instead of pushing the circle around
            Box(
                modifier = Modifier.width(64.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                PitchPlayerBadge(player = player, badgeColor = badgeColor, numberColor = numberColor)
            }
        }
    }
}

// keeps only the first and last name segments, dropping any middle names —
// e.g. "Bruno Miguel Borges Fernandes" -> "Bruno Fernandes",
// while names that are already just two parts (or one) pass through unchanged
private fun shortDisplayName(fullName: String): String {
    val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size <= 2 -> fullName
        else -> "${parts.first()} ${parts.last()}"
    }
}

@Composable
fun PitchPlayerBadge(player: Player, badgeColor: Color, numberColor: Color) {
    // border should contrast with the fill — white badges get a dark border,
    // red badges keep the white border, otherwise the away team's circles
    // would blend into a flat blob with no visible edge
    val borderColor = if (badgeColor == Color.White) BackgroundDark else Color.White

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(badgeColor, androidx.compose.foundation.shape.CircleShape)
                .border(1.5.dp, borderColor, androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = player.number.toString(),
                color = numberColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = shortDisplayName(player.name),
            color = TextPrimary,
            fontSize = 11.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// draws the pitch lines with Canvas — goal boxes top and bottom, halfway line, center circle
@Composable
fun PitchMarkings(modifier: Modifier = Modifier) {
    val lineColor = Color.White.copy(alpha = 0.18f)
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()

        // halfway line
        drawLine(lineColor, androidx.compose.ui.geometry.Offset(0f, h / 2), androidx.compose.ui.geometry.Offset(w, h / 2), strokeWidth)

        // center circle
        drawCircle(lineColor, radius = w * 0.18f, center = androidx.compose.ui.geometry.Offset(w / 2, h / 2), style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))

        // top goal box (home team's goal)
        val boxWidth = w * 0.5f
        val boxHeight = h * 0.12f
        drawRect(
            color = lineColor,
            topLeft = androidx.compose.ui.geometry.Offset((w - boxWidth) / 2, 0f),
            size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
        )

        // bottom goal box (away team's goal)
        drawRect(
            color = lineColor,
            topLeft = androidx.compose.ui.geometry.Offset((w - boxWidth) / 2, h - boxHeight),
            size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
        )

        // outer pitch border
        drawRect(
            color = lineColor,
            topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(w, h),
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
        )
    }
}

// shared section used for both Starting XI and Substitutes — crest, crest, title centered,
// divider above, then home players in the left column / away players in the right column
@Composable
fun LineupSection(
    title: String,
    homeTeamLogo: String,
    awayTeamLogo: String,
    homePlayers: List<Player>,
    awayPlayers: List<Player>
) {
    Divider(color = DividerColor, thickness = 1.dp)
    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(model = homeTeamLogo, contentDescription = null, modifier = Modifier.size(20.dp))
        Text(text = title, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        AsyncImage(model = awayTeamLogo, contentDescription = null, modifier = Modifier.size(20.dp))
    }

    Spacer(modifier = Modifier.height(12.dp))

    // pair players up row by row — home on the left, away on the right
    val rowCount = maxOf(homePlayers.size, awayPlayers.size)
    for (i in 0 until rowCount) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                homePlayers.getOrNull(i)?.let { PlayerRow(player = it, alignEnd = false) }
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                awayPlayers.getOrNull(i)?.let { PlayerRow(player = it, alignEnd = true) }
            }
        }
    }
}

@Composable
fun PlayerRow(player: Player, alignEnd: Boolean) {
    val positionSuffix = if (player.position == "G") " (GK)" else ""
    val displayName = "${shortDisplayName(player.name)}$positionSuffix"
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (alignEnd) {
            Text(
                text = displayName,
                color = TextPrimary,
                fontSize = 13.sp,
                textAlign = TextAlign.End
            )
            JerseyNumberBadge(number = player.number)
        } else {
            JerseyNumberBadge(number = player.number)
            Text(
                text = displayName,
                color = TextPrimary,
                fontSize = 13.sp
            )
        }
    }
}

// small circular badge with the player's number — red outline, transparent fill,
// matching the jersey-number style used on the pitch view in Figma
@Composable
fun JerseyNumberBadge(number: Int) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(Color.Transparent, androidx.compose.foundation.shape.CircleShape)
            .border(1.dp, TextSecondary, androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ---- Stats Tab ----
@Composable
fun StatsTab(statistics: List<FixtureStat>, match: Match) {
    if (statistics.isEmpty()) {
        EmptyTabMessage("Statistics not available yet")
        return
    }
    if (statistics.size < 2) {
        EmptyTabMessage("Need both teams' stats")
        return
    }

    // guarantee home is always shown on the left regardless of API response order
    val homeStat = statistics.firstOrNull { it.team.id == match.homeTeam.id } ?: statistics[0]
    val awayStat = statistics.firstOrNull { it.team.id == match.awayTeam.id } ?: statistics[1]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(16.dp)
    ) {
        // crests only — no team names, matches Figma exactly
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AsyncImage(model = homeStat.team.logoUrl, contentDescription = homeStat.team.name, modifier = Modifier.size(28.dp))
            AsyncImage(model = awayStat.team.logoUrl, contentDescription = awayStat.team.name, modifier = Modifier.size(28.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        homeStat.statistics.forEach { statItem ->
            val awayItem = awayStat.statistics.find { it.type == statItem.type }
            if (awayItem != null) {
                StatBarRow(
                    statType = statItem.type,
                    homeValue = statItem.value,
                    awayValue = awayItem.value
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun StatBarRow(statType: String, homeValue: String, awayValue: String) {
    val homeNum = homeValue.replace("%", "").toFloatOrNull() ?: 0f
    val awayNum = awayValue.replace("%", "").toFloatOrNull() ?: 0f
    val total = homeNum + awayNum
    // when both sides are 0 (e.g. no cards in the match), split evenly with no red
    // accent rather than dividing by zero or showing a misleading 100/0 bar
    val homeFraction = if (total > 0) homeNum / total else 0.5f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = homeValue, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(text = statType, color = TextSecondary, fontSize = 13.sp)
            Text(text = awayValue, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        SplitStatBar(homeFraction = homeFraction, showAccent = total > 0)
    }
}

// two separate bars with a gap between them — NOT one continuous split bar.
// home's bar: light-gray track, red fill anchored at the center (inner) edge.
// away's bar: dark-gray track, light-gray fill anchored at the outer edge.
// both fills are sized as that team's share of the combined total.
@Composable
fun SplitStatBar(homeFraction: Float, showAccent: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // home bar — fill grows from the right (center) edge leftward as homeFraction increases
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFD2D2D2)),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(homeFraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (showAccent) AccentRed else Color(0xFFD2D2D2))
            )
        }
        // away bar — fill grows from the outer (right) edge leftward as awayFraction increases
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF3A3A3A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((1f - homeFraction).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFD2D2D2))
            )
        }
    }
}

// ---- H2H Tab ----
@Composable
fun H2HTab(matches: List<Match>, homeTeam: Team, awayTeam: Team) {
    if (matches.isEmpty()) {
        EmptyTabMessage("No head to head data")
        return
    }

    val homeWins = matches.count { m ->
        (m.homeTeam.id == homeTeam.id && (m.homeScore ?: 0) > (m.awayScore ?: 0)) ||
                (m.awayTeam.id == homeTeam.id && (m.awayScore ?: 0) > (m.homeScore ?: 0))
    }
    val draws = matches.count { m -> m.homeScore == m.awayScore }
    val awayWins = matches.size - homeWins - draws
    val maxWins = maxOf(homeWins, awayWins, 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(16.dp)
    ) {
        // "12 Matches" headline on the left, vertically centered against the whole
        // Total Wins block on the right (not top-aligned, which is what looked off)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${matches.size} Matches",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Column(modifier = Modifier.weight(1.3f)) {
                // left-aligned over the bars, not centered — and brighter than
                // TextSecondary so it's actually readable against the dark background
                Text(
                    text = "Total Wins",
                    color = Color(0xFFD2D2D2),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                TotalWinsBar(logoUrl = homeTeam.logoUrl, wins = homeWins, maxWins = maxWins)
                Spacer(modifier = Modifier.height(8.dp))
                TotalWinsBar(logoUrl = awayTeam.logoUrl, wins = awayWins, maxWins = maxWins)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$draws Draws",
                    color = Color(0xFFD2D2D2),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "LAST 5 MATCHES",
            color = Color(0xFFD2D2D2),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        matches.take(5).forEach { match ->
            H2HMatchRow(match = match)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// crest + proportional bar + win count, matches Figma's Total Wins row exactly
@Composable
fun TotalWinsBar(logoUrl: String, wins: Int, maxWins: Int) {
    val fraction = if (maxWins > 0) wins.toFloat() / maxWins.toFloat() else 0f
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(model = logoUrl, contentDescription = null, modifier = Modifier.size(18.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFD2D2D2))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(AccentRed)
            )
        }
        Text(
            text = wins.toString(),
            color = TextPrimary,
            fontSize = 13.sp,
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.End
        )
    }
}

// one historical match — date sits once on the left, spanning two stacked team rows
// (crest + name + score), exactly matching the Home screen's MatchCard pattern
@Composable
fun H2HMatchRow(match: Match) {
    val datePart = match.date.substringBefore("T")
    val dateParts = datePart.split("-")
    val dayMonth = if (dateParts.size == 3) "${dateParts[2]}/${dateParts[1]}" else datePart
    val year = if (dateParts.size == 3) dateParts[0] else ""

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.width(40.dp)) {
            Text(text = dayMonth, color = TextSecondary, fontSize = 11.sp)
            Text(text = year, color = TextSecondary, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            H2HTeamRow(name = match.homeTeam.name, logoUrl = match.homeTeam.logoUrl, score = match.homeScore)
            Spacer(modifier = Modifier.height(8.dp))
            H2HTeamRow(name = match.awayTeam.name, logoUrl = match.awayTeam.logoUrl, score = match.awayScore)
        }
    }
}

@Composable
fun H2HTeamRow(name: String, logoUrl: String, score: Int?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(model = logoUrl, contentDescription = name, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            color = TextPrimary,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Text(
            text = score?.toString() ?: "-",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---- Standings Tab ----
@Composable
fun StandingsTab(standings: List<Standing>, homeTeamId: Int, awayTeamId: Int) {
    if (standings.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Standings for the current season\nare not available on the free API plan",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
        return
    }

    val presentZones = standings.mapNotNull { classifyZone(it.zoneDescription) }.toSet()

    // group by the "group" field — for simple leagues all rows share the same
    // value (or null), producing a single entry with no header shown.
    // for MLS/USL/Champions League etc., each conference or group gets its own
    // labeled section so ranks restart correctly and make sense visually.
    val groups = standings.groupBy { it.group ?: "" }.toList()
    val isMultiGroup = groups.size > 1

    val columnHeader: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(22.dp))
            Text("Club", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text("PL", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            Text("W", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            Text("D", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            Text("L", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            Text("GD", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
            Text("PTS", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
        }
        Divider(color = DividerColor)
    }

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        groups.forEach { (groupName, groupStandings) ->
            if (isMultiGroup && groupName.isNotBlank()) {
                item {
                    Text(
                        text = groupName,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 4.dp)
                    )
                }
            }
            item { columnHeader() }
            items(groupStandings) { standing ->
                val isHighlighted = standing.team.id == homeTeamId || standing.team.id == awayTeamId
                StandingRow(standing = standing, isHighlighted = isHighlighted)
            }
        }
        if (presentZones.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                StandingsLegend(zones = presentZones)
            }
        }
    }
}

// the four zone categories we color-code, in legend display order
enum class StandingZone(val label: String, val color: Color) {
    CHAMPIONS_LEAGUE("Champions League - Group Stage", Color(0xFF2E90FA)),
    EUROPA_LEAGUE("Europa League - Group Stage", Color(0xFFFB6514)),
    CONFERENCE_LEAGUE("Conference League - Qualification", Color(0xFF12B76A)),
    RELEGATION("Relegated - Championship", Color(0xFFB32318))
}

// classifies the API's free-text zone description into one of our four buckets —
// matches on keywords since the exact wording varies by competition
// (e.g. "Promotion - Champions League (Group Stage)" vs "Champions League")
internal fun classifyZone(description: String?): StandingZone? {
    if (description.isNullOrBlank()) return null
    val text = description.lowercase()
    return when {
        "champions league" in text -> StandingZone.CHAMPIONS_LEAGUE
        "europa league" in text -> StandingZone.EUROPA_LEAGUE
        "conference league" in text -> StandingZone.CONFERENCE_LEAGUE
        "relegation" in text -> StandingZone.RELEGATION
        else -> null
    }
}

@Composable
fun StandingRow(standing: Standing, isHighlighted: Boolean) {
    val zone = classifyZone(standing.zoneDescription)
    val gdText = if (standing.goalDifference > 0) "+${standing.goalDifference}" else standing.goalDifference.toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHighlighted) BackgroundSurface else Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // colored zone stripe on the far left edge — 3dp wide, full row height
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(36.dp)
                .background(zone?.color ?: Color.Transparent)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = standing.rank.toString(),
            color = if (isHighlighted) AccentRed else TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.width(18.dp)
        )
        AsyncImage(
            model = standing.team.logoUrl,
            contentDescription = standing.team.name,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = standing.team.name,
            color = TextPrimary,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Text(standing.played.toString(), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
        Text(standing.won.toString(), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
        Text(standing.drawn.toString(), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
        Text(standing.lost.toString(), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
        Text(gdText, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
        Text(
            text = standing.points.toString(),
            color = AccentRed,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StandingsLegend(zones: Set<StandingZone>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // always render in a fixed, sensible order rather than Set's undefined iteration order
        StandingZone.entries.filter { it in zones }.forEach { zone ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(zone.color, androidx.compose.foundation.shape.CircleShape)
                )
                Text(text = zone.label, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun EmptyTabMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = TextSecondary, fontSize = 14.sp)
    }
}