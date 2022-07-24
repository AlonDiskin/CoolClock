package com.diskin.alon.coolclock.alarms.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarms ORDER BY id DESC")
    fun getAll(): PagingSource<Int, AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(alarm: AlarmEntity): Completable
}