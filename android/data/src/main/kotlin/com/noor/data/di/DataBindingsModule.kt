package com.noor.data.di

import com.noor.data.local.QuranLocalDataSource
import com.noor.data.local.RoomQuranLocalDataSource
import com.noor.data.remote.QuranEncTafsirDataSource
import com.noor.data.remote.TafsirRemoteDataSource
import com.noor.data.repository.DefaultAdhkarRepository
import com.noor.data.repository.DefaultBookmarkRepository
import com.noor.data.repository.DefaultQuranRepository
import com.noor.data.repository.DefaultTafsirRepository
import com.noor.data.settings.PreferencesSettingsRepository
import com.noor.domain.repository.AdhkarRepository
import com.noor.domain.repository.BookmarkRepository
import com.noor.domain.repository.QuranRepository
import com.noor.domain.repository.SettingsRepository
import com.noor.domain.repository.TafsirRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataBindingsModule {
    @Binds @Singleton abstract fun bindQuranLocalDataSource(implementation: RoomQuranLocalDataSource): QuranLocalDataSource
    @Binds @Singleton abstract fun bindQuranRepository(implementation: DefaultQuranRepository): QuranRepository
    @Binds @Singleton abstract fun bindAdhkarRepository(implementation: DefaultAdhkarRepository): AdhkarRepository
    @Binds @Singleton abstract fun bindBookmarkRepository(implementation: DefaultBookmarkRepository): BookmarkRepository
    @Binds @Singleton abstract fun bindTafsirRemoteDataSource(implementation: QuranEncTafsirDataSource): TafsirRemoteDataSource
    @Binds @Singleton abstract fun bindTafsirRepository(implementation: DefaultTafsirRepository): TafsirRepository
    @Binds @Singleton abstract fun bindSettingsRepository(implementation: PreferencesSettingsRepository): SettingsRepository
}
