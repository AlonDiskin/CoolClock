package com.diskin.alon.coolclock.worldclocks.data

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.diskin.alon.coolclock.worldclocks.domain.City
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class CitiesRepositoryImplTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var repository: CitiesRepositoryImpl

    // Collaborators
    private val dao: CityDao = mockk()
    private val mapper: CityMapper = mockk()

    @Before
    fun setUp() {
        repository = CitiesRepositoryImpl(dao, mapper)
    }

    @Test
    fun getAllMatchingCities_WhenSearched() {
        // Given
        val query = "query"
        val results = mockk<List<CityEntity>>()
        val resultsPagingSource = object : PagingSource<Int,CityEntity>() {
            override fun getRefreshKey(state: PagingState<Int, CityEntity>): Int? {
                return null
            }

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CityEntity> {
                return LoadResult.Page(results,null,null)
            }

        }
        val mappedResults = mockk<PagingData<City>>()

        every { dao.getStartsWith(query) } returns resultsPagingSource
        every { mapper.map(any()) } returns mappedResults

        // When
        val observer = repository.search(query).test()

        // Then
        verify { dao.getStartsWith(query) }
        verify { mapper.map(any()) }
        observer.assertValue(mappedResults)
    }
}