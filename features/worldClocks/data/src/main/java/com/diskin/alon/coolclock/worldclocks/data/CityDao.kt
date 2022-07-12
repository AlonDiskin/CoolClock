package com.diskin.alon.coolclock.worldclocks.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import io.reactivex.Completable

@Dao
interface CityDao {

    @Query("SELECT * FROM cities WHERE name LIKE :query || '%' ORDER BY population DESC ")
    fun getStartsWith(query: String): PagingSource<Int, CityEntity>

    @Query("UPDATE cities SET isSelected = 1,selectedDate = :selectedDate WHERE id = :id ")
    fun select(id: Long,selectedDate: Long): Completable

    @Query("SELECT * FROM cities WHERE isSelected ORDER BY selectedDate DESC")
    fun getSelected(): PagingSource<Int, CityEntity>

    @Query("UPDATE cities SET isSelected = 0 WHERE id = :id")
    fun unSelect(id: Long): Completable
}