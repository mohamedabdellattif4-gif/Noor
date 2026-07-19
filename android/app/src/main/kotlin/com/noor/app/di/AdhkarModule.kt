package com.noor.app.di

import com.noor.app.adhkar.WorkManagerAdhkarReminderScheduler
import com.noor.domain.repository.AdhkarReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AdhkarModule {
    @Binds
    @Singleton
    abstract fun bindAdhkarReminderScheduler(
        implementation: WorkManagerAdhkarReminderScheduler,
    ): AdhkarReminderScheduler
}
