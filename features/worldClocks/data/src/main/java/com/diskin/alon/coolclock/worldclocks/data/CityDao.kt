package com.diskin.alon.coolclock.worldclocks.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query

@Dao
interface CityDao {

    @Query("SELECT * FROM cities WHERE name LIKE :query || '%' ORDER BY population DESC ")
    fun getStartsWith(query: String): PagingSource<Int, CityEntity>
}