package com.noor.data.remote

import com.noor.domain.model.Ayah
import com.noor.domain.model.Tafsir

internal interface TafsirRemoteDataSource {
    suspend fun fetch(ayah: Ayah): Tafsir
}
