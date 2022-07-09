package com.diskin.alon.coolclock.worldclocks.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import io.reactivex.Completable

@Dao
interface CityDao {

    @Query("SELECT * FROM cities WHERE name LIKE :query || '%' ORDER BY population DESC ")
    fun getStartsWith(query: String): PagingSource<Int, CityEntity>

    @Query("UPDATE cities SET isSelected = 1 WHERE id = :id ")
    fun select(id: Long): Completable
}