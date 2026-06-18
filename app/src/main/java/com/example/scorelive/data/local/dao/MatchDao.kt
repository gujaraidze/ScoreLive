package com.example.scorelive.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.scorelive.data.local.entity.MatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(matches: List<MatchEntity>)

    @Query("SELECT * FROM matches ORDER BY date ASC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE isLive = 1")
    fun getLiveMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE leagueId = :leagueId ORDER BY date ASC")
    fun getMatchesByLeague(leagueId: Int): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun getMatchById(matchId: Int): Flow<MatchEntity?>

    @Query("DELETE FROM matches WHERE isLive = 0")
    suspend fun deleteNonLiveMatches()
}