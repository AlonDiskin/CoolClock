package com.diskin.alon.coolclock.alarms.data

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.alarms.data.local.AlarmDao
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntity
import com.diskin.alon.coolclock.alarms.domain.WeekDay
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlarmDaoTest {

    // System under test
    private lateinit var dao: AlarmDao
    private lateinit var db: TestDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.alarmDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndQueryAll() = runBlocking {
        // Given
        val entities = listOf(
            AlarmEntity(
            "name_1",
            12,
             10,
            setOf(WeekDay.SUN,WeekDay.MON),
            true,
            "sound_1",
            false,
            true,
                1,
                5,
                false,
                5,
                1,
            ),
            AlarmEntity(
                "name_2",
                11,
                15,
                emptySet(),
                false,
                "sound_2",
                true,
                true,
                2,
                10,
                true,
                5,
                1,
            )
        )
        val expected = listOf(
            AlarmEntity(
                "name_1",
                12,
                10,
                setOf(WeekDay.SUN,WeekDay.MON),
                true,
                "sound_1",
                false,
                true,
                1,
                5,
                false,
                5,
                1,
                1
            ),
            AlarmEntity(
                "name_2",
                11,
                15,
                emptySet(),
                false,
                "sound_2",
                true,
                true,
                2,
                10,
                true,
                5,
                1,
                2
            )
        ).asReversed()

        // When
        entities.forEach { dao.insert(it).blockingAwait() }

        // Then
        val actual = dao.getAll().load(
            PagingSource.LoadParams.Refresh(null,20,false)
        ) as PagingSource.LoadResult.Page<Int, AlarmEntity>

        assertThat(actual.data).isEqualTo(expected)
    }

    @Test
    fun insertAndQueryAlarmById() {
        // Given
        val entity = AlarmEntity(
            "name_1",
            12,
            10,
            setOf(WeekDay.SUN,WeekDay.MON),
            true,
            "sound_1",
            false,
            true,
            1,
            5,
            false,
            5,
            1,
            1
        )

        dao.insert(entity).blockingAwait()

        // When
        val actual = dao.get(entity.id!!).blockingGet()

        // Then
        assertThat(actual).isEqualTo(entity)
    }

    @Test
    fun insertAndUpdateAlarmActiveState() {
        // Given
        val entity = AlarmEntity(
            "name_1",
            12,
            10,
            setOf(WeekDay.SUN,WeekDay.MON),
            true,
            "sound_1",
            false,
            true,
            1,
            5,
            false,
            5,
            1,
            1
        )
        val expected = AlarmEntity(
            "name_1",
            12,
            10,
            setOf(WeekDay.SUN,WeekDay.MON),
            false,
            "sound_1",
            false,
            true,
            1,
            5,
            false,
            5,
            1,
            1
        )

        dao.insert(entity).blockingAwait()

        // When
        dao.updateIsActive(entity.id!!,!entity.isActive).blockingAwait()
        val actual = dao.get(entity.id!!).blockingGet()

        // Then
        assertThat(actual).isEqualTo(expected)
    }
}