package com.diskin.alon.coolclock.worldclocks.data

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class CityDaoTest(
    private val query: String,
    private val expectedResults: List<CityEntity>
) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun testCases() = listOf(
            arrayOf(
                "lon",
                listOf(
                    CityEntity("London","United Kingdom","",8961989,"Europe/London",true,1),
                    CityEntity("London","United States","CA",1869,"America/Los Angeles",false,7)
                )
            ),
            arrayOf(
                "l",
                listOf(
                    CityEntity("London","United Kingdom","",8961989,"Europe/London",true,1),
                    CityEntity("Los Angeles","United States","CA",3971883,"America/Los Angeles",true,5),
                    CityEntity("London","United States","CA",1869,"America/Los Angeles",false,7)
                )
            )
        )
    }

    // System under test
    private lateinit var dao: CityDao
    private lateinit var db: TestDatabase

    @Before
    fun setUp() {
        // Init system under test
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.cityDao()

        // Prepopulate test db
        val insert1 = "INSERT INTO cities (name, country, state, population, timezone, isSelected)" +
                "VALUES ('London','United Kingdom','',8961989,'Europe/London',1)"
        val insert2 = "INSERT INTO cities (name, country, state, population, timezone, isSelected)" +
                "VALUES ('Moscow','Russian Federation','',10381222,'Europe/Moscow',0)"
        val insert3 = "INSERT INTO cities (name, country, state, population, timezone, isSelected)" +
                "VALUES ('Rome','Italy','',2318895,'Europe/Rome', 0)"
        val insert4 = "INSERT INTO cities (name, country, state, population, timezone, isSelected)" +
                "VALUES ('Jerusalem','Israel','',855234,'Asia/Jerusalem',0)"
        val insert5 = "INSERT INTO cities (name, country, state, population, timezone, isSelected)" +
                "VALUES ('Los Angeles','United States','CA',3971883,'America/Los Angeles',1)"
        val insert6 = "INSERT INTO cities (name, country, state, population, timezone, isSelected)" +
                "VALUES ('Chicago','United States','IL',2921853,'America/Chicago',0)"
        val insert7 = "INSERT INTO cities (name, country, state, population, timezone, isSelected)" +
                "VALUES ('London','United States','CA',1869,'America/Los Angeles',0)"

        db.compileStatement(insert1).executeInsert()
        db.compileStatement(insert2).executeInsert()
        db.compileStatement(insert3).executeInsert()
        db.compileStatement(insert4).executeInsert()
        db.compileStatement(insert5).executeInsert()
        db.compileStatement(insert6).executeInsert()
        db.compileStatement(insert7).executeInsert()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getMatchingCitiesOrderedByPopulation_WhenSearched() = runBlocking {
        // Given

        // When
        val result = dao.getStartsWith(query).load(
            PagingSource.LoadParams.Refresh(null,20,false)
        ) as PagingSource.LoadResult.Page<Int, CityEntity>

        // Then
        assertThat(result.data).isEqualTo(expectedResults)
    }
}