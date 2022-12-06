package com.diskin.alon.coolclock.alarms.data.local

import androidx.paging.PagingSource
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface AlarmDao {

    @Query("SELECT * FROM user_alarms ORDER BY id DESC")
    fun getAllPaging(): PagingSource<Int, AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(alarm: AlarmEntity): Single<Long>

    @Query("SELECT * FROM user_alarms WHERE id = :id")
    fun get(id: Int): Single<AlarmEntity>

    @Query("UPDATE user_alarms SET isScheduled = :scheduled WHERE id = :id")
    fun updateScheduled(id: Int,scheduled: Boolean): Completable

    @Query("DELETE FROM user_alarms WHERE id = :id")
    fun delete(id: Int): Completable

    @Query("SELECT * FROM user_alarms")
    fun getAll(): Single<List<AlarmEntity>>

    @Update
    fun update(alarm: AlarmEntity): Completable
}