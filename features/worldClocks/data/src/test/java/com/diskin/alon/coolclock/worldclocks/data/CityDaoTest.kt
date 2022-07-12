package com.diskin.alon.coolclock.worldclocks.data

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CityDaoTest {

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
        val insert1 = "INSERT INTO cities (name,country,state,population,timezone,isSelected,selectedDate)" +
                "VALUES ('London','United Kingdom','',8961989,'Europe/London',1,1234)"
        val insert2 = "INSERT INTO cities (name,country,state,population,timezone,isSelected)" +
                "VALUES ('Moscow','Russian Federation','',10381222,'Europe/Moscow',0)"
        val insert3 = "INSERT INTO cities (name,country,state,population,timezone,isSelected)" +
                "VALUES ('Rome','Italy','',2318895,'Europe/Rome', 0)"
        val insert4 = "INSERT INTO cities (name,country,state,population,timezone,isSelected)" +
                "VALUES ('Jerusalem','Israel','',855234,'Asia/Jerusalem',0)"
        val insert5 = "INSERT INTO cities (name,country,state,population,timezone,isSelected,selectedDate)" +
                "VALUES ('Los Angeles','United States','CA',3971883,'America/Los Angeles',1,12345)"
        val insert6 = "INSERT INTO cities (name,country,state,population,timezone,isSelected)" +
                "VALUES ('Chicago','United States','IL',2921853,'America/Chicago',0)"
        val insert7 = "INSERT INTO cities (name,country,state,population,timezone,isSelected)" +
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
        var query = "lon"
        var expectedResults = listOf(
            CityEntity("London","United Kingdom","",8961989,"Europe/London",true, selectedDate = 1234,id = 1),
            CityEntity("London","United States","CA",1869,"America/Los Angeles",false,id = 7)
        )

        // When
        var result = dao.getStartsWith(query).load(
            PagingSource.LoadParams.Refresh(null,20,false)
        ) as PagingSource.LoadResult.Page<Int, CityEntity>

        // Then
        assertThat(result.data).isEqualTo(expectedResults)

        // When
        query = "l"
        expectedResults = listOf(
            CityEntity("London","United Kingdom","",8961989,"Europe/London",true, selectedDate = 1234,id = 1),
            CityEntity("Los Angeles","United States","CA",3971883,"America/Los Angeles",true, selectedDate = 12345,id = 5),
            CityEntity("London","United States","CA",1869,"America/Los Angeles",false,id = 7)
        )
        result = dao.getStartsWith(query).load(
            PagingSource.LoadParams.Refresh(null,20,false)
        ) as PagingSource.LoadResult.Page<Int, CityEntity>

        // Then
        assertThat(result.data).isEqualTo(expectedResults)
    }

    @Test
    fun setCityAsSelected_WhenUpdatedForSelection() {
        // Given
        val unSelectedRecordId = 2L
        val expectedSelected = 3

        // When
        dao.select(unSelectedRecordId,7896).blockingAwait()

        // Then
        val selectedCities = db.compileStatement("SELECT COUNT(*) FROM cities WHERE isSelected = 1")
            .simpleQueryForLong()

        assertThat(selectedCities).isEqualTo(expectedSelected)
    }

    @Test
    fun getSelectedOrderedByDateInDesc_WhenQueried() = runBlocking {
        // Given

        // When
        val result = dao.getSelected().load(
            PagingSource.LoadParams.Refresh(null,20,false)
        ) as PagingSource.LoadResult.Page<Int, CityEntity>

        // Then
        val expectedResults = listOf(
            CityEntity("Los Angeles","United States","CA",3971883,"America/Los Angeles",true, selectedDate = 12345,id = 5),
            CityEntity("London","United Kingdom","",8961989,"Europe/London",true, selectedDate = 1234,id = 1)
        )

        assertThat(result.data).isEqualTo(expectedResults)
    }

    @Test
    fun setCityAsUnSelected_WhenUpdatedForUnSelection() {
        // Given
        val selectedRecordId = 1L
        val expectedSelected = 1

        // When
        dao.unSelect(selectedRecordId).blockingAwait()

        // Then
        val selectedCities = db.compileStatement("SELECT COUNT(*) FROM cities WHERE isSelected = 1")
            .simpleQueryForLong()

        assertThat(selectedCities).isEqualTo(expectedSelected)
    }
}