package com.noor.data.repository

import com.noor.data.local.dao.TafsirDao
import com.noor.data.local.entity.TafsirEntity
import com.noor.data.remote.TafsirRemoteDataSource
import com.noor.domain.model.Ayah
import com.noor.domain.model.Tafsir
import com.noor.domain.model.TafsirOrigin
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultTafsirRepositoryTest {
    @Test
    fun cachedTafsirIsReturnedWithoutNetworkAccess() = runTest {
        val cached = sampleEntity()
        val dao = FakeTafsirDao(cached)
        val remote = FakeRemoteDataSource()
        val repository = DefaultTafsirRepository(dao, remote)

        val result = repository.getTafsir(sampleAyah())

        assertEquals(TafsirOrigin.CACHE, result.origin)
        assertEquals("حاشية محفوظة", result.footnotes)
        assertEquals(0, remote.calls)
        assertSame(cached, dao.entity)
    }

    @Test
    fun networkResultIsValidatedAndCached() = runTest {
        val dao = FakeTafsirDao()
        val remote = FakeRemoteDataSource(result = sampleTafsir())
        val repository = DefaultTafsirRepository(dao, remote)

        val result = repository.getTafsir(sampleAyah())

        assertEquals(TafsirOrigin.NETWORK, result.origin)
        assertEquals(1, remote.calls)
        assertEquals(result.text, dao.entity?.text)
        assertEquals(result.footnotes, dao.entity?.footnotes)
    }

    @Test
    fun concurrentCacheMissesShareOneNetworkRequest() = runTest {
        val gate = CompletableDeferred<Unit>()
        val dao = FakeTafsirDao()
        val remote = FakeRemoteDataSource(gate = gate)
        val repository = DefaultTafsirRepository(dao, remote)

        val first = async { repository.getTafsir(sampleAyah()) }
        runCurrent()
        val second = async { repository.getTafsir(sampleAyah()) }
        runCurrent()

        assertEquals(1, remote.calls)
        gate.complete(Unit)
        assertEquals(TafsirOrigin.NETWORK, first.await().origin)
        assertEquals(TafsirOrigin.CACHE, second.await().origin)
        assertEquals(1, remote.calls)
    }

    @Test
    fun concurrentForcedRefreshesShareOneCompletedRefresh() = runTest {
        val gate = CompletableDeferred<Unit>()
        val dao = FakeTafsirDao(sampleEntity())
        val remote = FakeRemoteDataSource(gate = gate)
        val repository = DefaultTafsirRepository(dao, remote)

        val first = async { repository.getTafsir(sampleAyah(), forceRefresh = true) }
        runCurrent()
        val second = async { repository.getTafsir(sampleAyah(), forceRefresh = true) }
        runCurrent()

        assertEquals(1, remote.calls)
        gate.complete(Unit)
        assertEquals(TafsirOrigin.NETWORK, first.await().origin)
        assertEquals(TafsirOrigin.CACHE, second.await().origin)
        assertEquals(1, remote.calls)
    }

    @Test
    fun failedRefreshDoesNotOverwriteTheExistingCache() = runTest {
        val cached = sampleEntity()
        val dao = FakeTafsirDao(cached)
        val remote = FakeRemoteDataSource(failure = IOException("offline"))
        val repository = DefaultTafsirRepository(dao, remote)

        val failed = runCatching {
            repository.getTafsir(sampleAyah(), forceRefresh = true)
        }.isFailure

        assertTrue(failed)
        assertSame(cached, dao.entity)
    }

    @Test
    fun mismatchedRemoteAyahIsRejectedBeforeCaching() = runTest {
        val dao = FakeTafsirDao()
        val remote = FakeRemoteDataSource(result = sampleTafsir(ayahId = 2))
        val repository = DefaultTafsirRepository(dao, remote)

        val failed = runCatching { repository.getTafsir(sampleAyah()) }.isFailure

        assertTrue(failed)
        assertNull(dao.entity)
    }

    private class FakeTafsirDao(initial: TafsirEntity? = null) : TafsirDao {
        var entity: TafsirEntity? = initial
            private set

        override suspend fun getByAyahId(ayahId: Int): TafsirEntity? =
            entity?.takeIf { it.ayahId == ayahId }

        override suspend fun upsert(tafsir: TafsirEntity) {
            entity = tafsir
        }

        override suspend fun deleteOlderThan(epochMillis: Long): Int {
            val current = entity ?: return 0
            return if (current.fetchedAtEpochMillis < epochMillis) {
                entity = null
                1
            } else {
                0
            }
        }
    }

    private class FakeRemoteDataSource(
        private val result: Tafsir = sampleTafsir(),
        private val gate: CompletableDeferred<Unit>? = null,
        private val failure: Exception? = null,
    ) : TafsirRemoteDataSource {
        var calls = 0
            private set

        override suspend fun fetch(ayah: Ayah): Tafsir {
            calls += 1
            gate?.await()
            failure?.let { throw it }
            return result
        }
    }

    private companion object {
        fun sampleAyah() = Ayah(
            id = 1,
            surahNumber = 1,
            numberInSurah = 1,
            textUthmani = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            juzNumber = 1,
            hizbQuarter = 1,
            pageNumber = 1,
        )

        fun sampleTafsir(ayahId: Int = 1) = Tafsir(
            ayahId = ayahId,
            text = "تفسير شبكي",
            footnotes = "حاشية شبكية",
            sourceKey = "arabic_moyassar",
            sourceName = "التفسير الميسر — QuranEnc.com",
            fetchedAtEpochMillis = 2L,
            origin = TafsirOrigin.NETWORK,
        )

        fun sampleEntity() = TafsirEntity(
            ayahId = 1,
            text = "تفسير محفوظ",
            footnotes = "حاشية محفوظة",
            sourceKey = "arabic_moyassar",
            sourceName = "التفسير الميسر — QuranEnc.com",
            fetchedAtEpochMillis = 1L,
        )
    }
}
