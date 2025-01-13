package com.example.hoverrobot.injection

import android.content.Context
import com.example.hoverrobot.data.repositories.CommsRepository
import com.example.hoverrobot.data.repositories.CommsRepositoryImpl
import com.example.hoverrobot.data.repositories.StoreSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoriesModules {

    @Provides
    @Singleton
    fun provideCommsRepository(
        @ApplicationContext context: Context
    ): CommsRepository = CommsRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideStoreSettings(
        @ApplicationContext context: Context
    ): StoreSettings = StoreSettings(context)
}