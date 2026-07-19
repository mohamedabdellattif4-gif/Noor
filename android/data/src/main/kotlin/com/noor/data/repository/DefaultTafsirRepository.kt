package com.noor.data.repository

import com.noor.data.local.dao.TafsirDao
import com.noor.data.local.entity.TafsirEntity
import com.noor.data.remote.TafsirRemoteDataSource
import com.noor.domain.model.Ayah
import com.noor.domain.model.Tafsir
import com.noor.domain.model.TafsirOrigin
import com.noor.domain.repository.TafsirRepository
import javax.inject.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class DefaultTafsirRepository @Inject constructor(
    private val dao: TafsirDao,
    private val remote: TafsirRemoteDataSource,
) : TafsirRepository {
    private val registryMutex = Mutex()
    private val ayahLocks = mutableMapOf<Int, Mutex>()
    private val completedFetchGenerations = mutableMapOf<Int, Long>()

    override suspend fun getTafsir(ayah: Ayah, forceRefresh: Boolean): Tafsir {
        val cachedBeforeLock = dao.getByAyahId(ayah.id)
        if (!forceRefresh && cachedBeforeLock != null) return cachedBeforeLock.asDomain()

        val ticket = registryMutex.withLock {
            FetchTicket(
                mutex = ayahLocks.getOrPut(ayah.id, ::Mutex),
                completedGeneration = completedFetchGenerations[ayah.id] ?: 0L,
            )
        }
        return ticket.mutex.withLock {
            val cachedInsideLock = dao.getByAyahId(ayah.id)
            if (!forceRefresh && cachedInsideLock != null) return@withLock cachedInsideLock.asDomain()

            val latestGeneration = registryMutex.withLock {
                completedFetchGenerations[ayah.id] ?: 0L
            }
            if (forceRefresh && latestGeneration > ticket.completedGeneration && cachedInsideLock != null) {
                return@withLock cachedInsideLock.asDomain()
            }

            val fetched = remote.fetch(ayah)
            require(fetched.ayahId == ayah.id) { "Remote tafsir returned a different ayah id." }
            dao.upsert(fetched.asEntity())
            registryMutex.withLock {
                completedFetchGenerations[ayah.id] = (completedFetchGenerations[ayah.id] ?: 0L) + 1L
            }
            fetched
        }
    }

    private fun TafsirEntity.asDomain(): Tafsir = Tafsir(
        ayahId = ayahId,
        text = text,
        footnotes = footnotes,
        sourceKey = sourceKey,
        sourceName = sourceName,
        fetchedAtEpochMillis = fetchedAtEpochMillis,
        origin = TafsirOrigin.CACHE,
    )

    private fun Tafsir.asEntity(): TafsirEntity = TafsirEntity(
        ayahId = ayahId,
        text = text,
        footnotes = footnotes,
        sourceKey = sourceKey,
        sourceName = sourceName,
        fetchedAtEpochMillis = fetchedAtEpochMillis,
    )

    private data class FetchTicket(
        val mutex: Mutex,
        val completedGeneration: Long,
    )
}
