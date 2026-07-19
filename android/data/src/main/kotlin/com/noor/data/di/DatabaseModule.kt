package com.noor.data.di

import android.content.Context
import androidx.room.Room
import com.noor.data.local.dao.AyahDao
import com.noor.data.local.dao.BookmarkDao
import com.noor.data.local.dao.SurahDao
import com.noor.data.local.dao.TafsirDao
import com.noor.data.local.database.NoorDatabase
import com.noor.data.local.database.NoorDatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideNoorDatabase(@ApplicationContext context: Context): NoorDatabase =
        Room.databaseBuilder(context, NoorDatabase::class.java, NoorDatabase.NAME)
            .createFromAsset(NoorDatabase.ASSET_PATH)
            .addMigrations(*NoorDatabaseMigrations.ALL)
            .build()

    @Provides fun provideSurahDao(database: NoorDatabase): SurahDao = database.surahDao()
    @Provides fun provideAyahDao(database: NoorDatabase): AyahDao = database.ayahDao()
    @Provides fun provideBookmarkDao(database: NoorDatabase): BookmarkDao = database.bookmarkDao()
    @Provides fun provideTafsirDao(database: NoorDatabase): TafsirDao = database.tafsirDao()
}
