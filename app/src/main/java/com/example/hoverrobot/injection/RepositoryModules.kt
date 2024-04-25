package com.example.hoverrobot.injection

import android.content.Context
import com.example.hoverrobot.data.repository.CommsRepository
import com.example.hoverrobot.data.repository.CommsRepositoryImpl
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
}