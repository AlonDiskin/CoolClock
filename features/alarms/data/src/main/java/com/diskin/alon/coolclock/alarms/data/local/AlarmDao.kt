package com.diskin.alon.coolclock.alarms.data.local

import androidx.paging.PagingSource
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarms ORDER BY id DESC")
    fun getAll(): PagingSource<Int, AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(alarm: AlarmEntity): Completable

    @Query("SELECT * FROM alarms WHERE id = :id")
    fun get(id: Int): Single<AlarmEntity>

    @Query("UPDATE alarms SET isActive = :isActive WHERE id = :id")
    fun updateIsActive(id: Int,isActive: Boolean): Completable

    @Query("DELETE FROM alarms WHERE id = :id")
    fun delete(id: Int): Completable
}