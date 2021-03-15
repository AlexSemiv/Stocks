package com.example.stocks.di

import com.example.stocks.adapters.NewsAdapter
import com.example.stocks.adapters.StocksAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
object AdaptersModule {

    @Provides
    fun provideNewsAdapter(): NewsAdapter = NewsAdapter()

    @Provides
    fun provideStocksAdapter(): StocksAdapter = StocksAdapter()
}