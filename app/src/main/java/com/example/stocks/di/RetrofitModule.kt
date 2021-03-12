package com.example.stocks.di

import com.example.stocks.api.StocksApi
import com.example.stocks.util.Utils.Companion.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@InstallIn(SingletonComponent::class)
@Module
object RetrofitModule {
    @Provides
    fun provideBaseUrl(): String = BASE_URL

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

    @Provides
    fun provideOkHttpClient(logger: HttpLoggingInterceptor): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()

    @Provides
    fun provideConvectorFactory(): Converter.Factory = GsonConverterFactory.create()

    @Provides
    fun provideRetrofit(baseUrl: String, converter: Converter.Factory, client: OkHttpClient): Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(converter)
            .client(client)
            .build()

    @Provides
    fun provideApiStocks(retrofit: Retrofit): StocksApi = retrofit.create(StocksApi::class.java)
}