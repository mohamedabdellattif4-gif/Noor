package com.noor.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.noor.data.local.dao.AyahDao
import com.noor.data.local.dao.BookmarkDao
import com.noor.data.local.dao.SurahDao
import com.noor.data.local.dao.TafsirDao
import com.noor.data.local.entity.AppMetadataEntity
import com.noor.data.local.entity.AyahEntity
import com.noor.data.local.entity.AyahFtsEntity
import com.noor.data.local.entity.BookmarkEntity
import com.noor.data.local.entity.SurahEntity
import com.noor.data.local.entity.TafsirEntity

@Database(
    entities = [
        SurahEntity::class,
        AyahEntity::class,
        AyahFtsEntity::class,
        BookmarkEntity::class,
        TafsirEntity::class,
        AppMetadataEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class NoorDatabase : RoomDatabase() {
    abstract fun surahDao(): SurahDao
    abstract fun ayahDao(): AyahDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun tafsirDao(): TafsirDao

    companion object {
        const val NAME: String = "noor.db"
        const val ASSET_PATH: String = "database/noor.db"
    }
}
