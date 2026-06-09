package com.nicolas.ttsapp.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

// ── Converters ────────────────────────────────────────────────────────────────

class Converters {
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it) }
}

// ── Entity ────────────────────────────────────────────────────────────────────

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isFavorite: Boolean = false,
    val label: String = ""
)

// ── DAO ───────────────────────────────────────────────────────────────────────

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE isFavorite = 1 ORDER BY label ASC, createdAt DESC")
    fun getFavorites(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatest(): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntity): Long

    @Update
    suspend fun update(entry: HistoryEntity)

    @Delete
    suspend fun delete(entry: HistoryEntity)

    @Query("DELETE FROM history WHERE isFavorite = 0")
    suspend fun clearHistory()

    @Query("DELETE FROM history WHERE id IN (SELECT id FROM history WHERE isFavorite = 0 ORDER BY createdAt ASC LIMIT :count)")
    suspend fun pruneOldest(count: Int)

    @Query("SELECT COUNT(*) FROM history WHERE isFavorite = 0")
    suspend fun countHistory(): Int
}
