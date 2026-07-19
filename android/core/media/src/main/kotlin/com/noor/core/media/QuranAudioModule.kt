package com.noor.core.media

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class QuranAudioModule {
    @Binds
    @Singleton
    abstract fun bindQuranAudioPlayer(implementation: QuranAudioController): QuranAudioPlayer
}
