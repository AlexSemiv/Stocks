package com.example.stocks.di

import android.content.Context
import androidx.room.Room
import com.example.stocks.db.SavedStockDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.junit.runner.manipulation.Ordering
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Named("test_database")
    fun provideInMemoryDatabase(@ApplicationContext context: Context) =
        Room.inMemoryDatabaseBuilder(
            context,
            SavedStockDatabase::class.java
        ).allowMainThreadQueries().build()
}