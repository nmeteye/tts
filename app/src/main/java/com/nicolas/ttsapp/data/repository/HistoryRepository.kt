package com.nicolas.ttsapp.data.repository

import com.nicolas.ttsapp.data.db.AppDatabase
import com.nicolas.ttsapp.data.db.HistoryEntity
import com.nicolas.ttsapp.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    db: AppDatabase
) {
    private val dao = db.historyDao()

    val history: Flow<List<HistoryEntry>> = dao.getAllHistory().map { list ->
        list.map { it.toDomain() }
    }

    val favorites: Flow<List<HistoryEntry>> = dao.getFavorites().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun add(text: String, maxSize: Int) {
        // Évite les doublons consécutifs
        val latest = dao.getLatest()
        if (latest?.text == text) return

        dao.insert(HistoryEntity(text = text))

        // Élagage si dépassement de la limite
        val count = dao.countHistory()
        if (count > maxSize) {
            dao.pruneOldest(count - maxSize)
        }
    }

    suspend fun toggleFavorite(entry: HistoryEntry) {
        dao.update(
            HistoryEntity(
                id = entry.id,
                text = entry.text,
                createdAt = entry.createdAt,
                isFavorite = !entry.isFavorite,
                label = entry.label
            )
        )
    }

    suspend fun updateLabel(entry: HistoryEntry, label: String) {
        dao.update(
            HistoryEntity(
                id = entry.id,
                text = entry.text,
                createdAt = entry.createdAt,
                isFavorite = entry.isFavorite,
                label = label
            )
        )
    }

    suspend fun delete(entry: HistoryEntry) {
        dao.delete(HistoryEntity(
            id = entry.id,
            text = entry.text,
            createdAt = entry.createdAt,
            isFavorite = entry.isFavorite,
            label = entry.label
        ))
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }
}

private fun HistoryEntity.toDomain() = HistoryEntry(
    id = id,
    text = text,
    createdAt = createdAt,
    isFavorite = isFavorite,
    label = label
)
