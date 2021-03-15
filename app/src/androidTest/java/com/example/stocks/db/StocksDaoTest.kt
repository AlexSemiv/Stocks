 package com.example.stocks.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.stocks.HiltTestRunner
import com.example.stocks.model.CandleResponse
import com.example.stocks.model.CompanyProfileResponse
import com.example.stocks.model.QuoteResponse
import com.example.stocks.model.news.CompanyNewsResponse
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Named

 @ExperimentalCoroutinesApi
@SmallTest
@HiltAndroidTest
class StocksDaoTest {

     // make testing with hilt
     @get:Rule
     val hiltRule = HiltAndroidRule(this)

    // make all async test func for db in main tread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject @Named("test_database")
    lateinit var database: SavedStockDatabase
    private lateinit var dao: StocksDao

    @Before
    fun before(){
        // to inject dependencies
        hiltRule.inject()
        dao = database.getStockDao()
    }

    @After
    fun after(){
        database.close()
    }

    @Test
    fun insertStock() = runBlockingTest {
        val stock = Stock(CompanyProfileResponse("country", "currency", "exchange",
                "finn", "ipo", "logo", 0.0, "name",
                "phone", 0.0, "ticker", "web"),
                QuoteResponse(0.0, 0.0, 0.0, 0.0),
                CompanyNewsResponse(),
                CandleResponse(listOf(), listOf(), listOf(), listOf(), "s"))

        dao.insertStock(stock)
        val allSavedStocks = dao.getAllSavedStocks()

        assertThat(allSavedStocks).contains(stock)
    }

    @Test
    fun deleteStock() = runBlockingTest {
        val stock = Stock(CompanyProfileResponse("country", "currency", "exchange",
                "finn", "ipo", "logo", 0.0, "name",
                "phone", 0.0, "ticker", "web"),
                QuoteResponse(0.0, 0.0, 0.0, 0.0),
                CompanyNewsResponse(),
                CandleResponse(listOf(), listOf(), listOf(), listOf(), "s"))

        dao.insertStock(stock)
        dao.deleteStock(stock)
        val allSavedStocks = dao.getAllSavedStocks()

        assertThat(allSavedStocks).doesNotContain(stock)
    }

    @Test
    fun deleteAllSavedStock() = runBlockingTest {
        val stock1 = Stock(CompanyProfileResponse("country", "currency", "exchange",
                "finn", "ipo", "logo", 0.0, "name",
                "phone", 0.0, "ticker1", "web"),
                QuoteResponse(0.0, 0.0, 0.0, 0.0),
                CompanyNewsResponse(),
                CandleResponse(listOf(), listOf(), listOf(), listOf(), "s"))
        val stock2 = Stock(CompanyProfileResponse("country", "currency", "exchange",
                "finn", "ipo", "logo", 0.0, "name",
                "phone", 0.0, "ticker2", "web"),
                QuoteResponse(0.0, 0.0, 0.0, 0.0),
                CompanyNewsResponse(),
                CandleResponse(listOf(), listOf(), listOf(), listOf(), "s"))

        dao.insertStock(stock1)
        dao.insertStock(stock2)
        val allSavedStocksBeforeDeleting = dao.getAllSavedStocks()
        dao.deleteAllSavedStocks()
        val allSavedStocksAfterDeleting = dao.getAllSavedStocks()

        assertThat(allSavedStocksBeforeDeleting).contains(stock1)
        assertThat(allSavedStocksBeforeDeleting).contains(stock2)
        assertThat(allSavedStocksAfterDeleting).isEmpty()
    }
}