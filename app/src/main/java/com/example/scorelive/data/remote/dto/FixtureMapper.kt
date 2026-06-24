package com.example.scorelive.data.remote.dto

import com.example.scorelive.data.local.entity.MatchEntity

// FixtureResponseItemDto → MatchEntity
// same pattern as lecturer's toDatabase() extension function
fun FixtureResponseItemDto.toEntity(): MatchEntity {
    return MatchEntity(
        id = fixture.id,
        homeTeamId = teams.home.id,
        homeTeamName = teams.home.name,
        homeTeamLogo = teams.home.logo,
        awayTeamId = teams.away.id,
        awayTeamName = teams.away.name,
        awayTeamLogo = teams.away.logo,
        homeScore = goals.home,
        awayScore = goals.away,
        leagueId = league.id,
        leagueName = league.name,
        leagueLogo = league.logo,
        leagueCountry = league.country,
        leagueSeason = league.season,
        status = fixture.status.short,
        minute = fixture.status.elapsed,
        date = fixture.date,
        isLive = fixture.status.short in listOf("1H", "2H", "HT", "ET", "BT", "P", "INT")
    )
}