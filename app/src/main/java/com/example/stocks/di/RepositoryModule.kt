package com.example.stocks.di

import com.example.stocks.api.StocksApi
import com.example.stocks.db.StocksDao
import com.example.stocks.repository.StocksRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@InstallIn(ActivityRetainedComponent::class)
@Module
object RepositoryModule {

    @Provides
    fun provideStocksRepository(api: StocksApi, dao: StocksDao): StocksRepository = StocksRepository(api, dao)
}