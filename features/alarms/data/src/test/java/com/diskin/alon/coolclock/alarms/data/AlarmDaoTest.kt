package com.diskin.alon.coolclock.alarms.data

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.alarms.data.local.AlarmDao
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntity
import com.diskin.alon.coolclock.alarms.data.local.AlarmMapper
import com.diskin.alon.coolclock.alarms.domain.Alarm
import com.diskin.alon.coolclock.alarms.domain.Sound
import com.diskin.alon.coolclock.alarms.domain.WeekDay
import com.diskin.alon.coolclock.common.application.toMaybeAppResult
import com.google.common.truth.Truth.assertThat
import io.reactivex.Maybe
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
    fun insertAndQueryPaging() = runBlocking {
        // Given
        val entities = listOf(
            AlarmEntity(
            "name_1",
            12,
             10,
            setOf(WeekDay.SUN,WeekDay.MON),
            true,
            Sound.AlarmSound("sound_1"),
            false,
            5,
                1,
                5,
                false
            ),
            AlarmEntity(
                "name_2",
                11,
                15,
                emptySet(),
                false,
                Sound.AlarmSound("sound_2"),
                true,
                4,
                2,
                10,
                true
            )
        )
        val expected = listOf(
            AlarmEntity(
                "name_1",
                12,
                10,
                setOf(WeekDay.SUN,WeekDay.MON),
                true,
                Sound.AlarmSound("sound_1"),
                false,
                5,
                1,
                5,
                false,
                1
            ),
            AlarmEntity(
                "name_2",
                11,
                15,
                emptySet(),
                false,
                Sound.AlarmSound("sound_2"),
                true,
                4,
                2,
                10,
                true,
                2
            )
        ).asReversed()

        // When
        entities.forEach { dao.insert(it).blockingGet() }

        // Then
        val actual = dao.getAllPaging().load(
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
            setOf(WeekDay.SUN, WeekDay.MON),
            true,
            Sound.AlarmSound("sound_1"),
            false,
            5,
            1,
            5,
            false,
            1
        )

        dao.insert(entity).blockingGet()

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
            setOf(WeekDay.SUN, WeekDay.MON),
            true,
            Sound.AlarmSound("sound_1"),
            false,
            5,
            1,
            5,
            false,
            1
        )
        val expected = AlarmEntity(
            entity.name,
            entity.hour,
            entity.minute,
            entity.repeatDays,
            false,
            entity.sound,
            entity.isVibrate,
            entity.duration,
            entity.volume,
            entity.snooze,
            false,
            entity.id
        )

        dao.insert(entity).blockingGet()

        // When
        dao.updateScheduled(entity.id!!,!entity.isScheduled).blockingAwait()
        val actual = dao.get(entity.id!!).blockingGet()

        // Then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun insertAndDelete() = runBlocking {
        // Given
        val entities = listOf(
            AlarmEntity(
                "name_1",
                12,
                10,
                setOf(WeekDay.SUN,WeekDay.MON),
                true,
                Sound.AlarmSound("sound_1"),
                false,
                5,
                1,
                5,
                false
            ),
            AlarmEntity(
                "name_2",
                11,
                15,
                emptySet(),
                false,
                Sound.AlarmSound("sound_2"),
                true,
                4,
                2,
                10,
                true
            )
        )

        entities.forEach { dao.insert(it).blockingGet() }

        // When
        dao.delete(1).blockingAwait()

        // Then
        val actual = dao.getAllPaging().load(
            PagingSource.LoadParams.Refresh(null,20,false)
        ) as PagingSource.LoadResult.Page<Int, AlarmEntity>

        assertThat(actual.data.size).isEqualTo(1)
        assertThat(actual.data.first().id).isEqualTo(2)
    }

    @Test
    fun insertAndReadAll() {
        // Given
        val entities = listOf(
            AlarmEntity(
                "name_1",
                12,
                10,
                setOf(WeekDay.SUN,WeekDay.MON),
                true,
                Sound.AlarmSound("sound_1"),
                false,
                5,
                1,
                5,
                false
            ),
            AlarmEntity(
                "name_2",
                11,
                15,
                emptySet(),
                false,
                Sound.AlarmSound("sound_2"),
                true,
                4,
                2,
                10,
                true
            )
        )
        val expected = listOf(
            AlarmEntity(
                "name_1",
                12,
                10,
                setOf(WeekDay.SUN,WeekDay.MON),
                true,
                Sound.AlarmSound("sound_1"),
                false,
                5,
                1,
                5,
                false,
                1
            ),
            AlarmEntity(
                "name_2",
                11,
                15,
                emptySet(),
                false,
                Sound.AlarmSound("sound_2"),
                true,
                4,
                2,
                10,
                true,
                2
            )
        )

        // When
        entities.forEach { dao.insert(it).blockingGet() }

        // And
        val observer = dao.getAll().test()

        // Then
        observer.assertValue(expected)
    }

    @Test
    fun name() {
        val entities = listOf(
            AlarmEntity(
                "name_1",
                12,
                10,
                setOf(WeekDay.SUN,WeekDay.MON),
                true,
                Sound.AlarmSound("sound_1"),
                false,
                5,
                1,
                5,
                false
            ),
            AlarmEntity(
                "name_2",
                11,
                15,
                emptySet(),
                false,
                Sound.AlarmSound("sound_2"),
                true,
                4,
                2,
                10,
                true
            )
        )

        entities.forEach { dao.insert(it).blockingGet() }

        val mapper = AlarmMapper()
        val observer = dao.getAll()
            .map { it.map(mapper::map) }
            .flatMapMaybe { alarms ->
                Maybe.create<Alarm> { emitter ->

                    alarms.find { it.id == 12 }?.let {
                        emitter.onSuccess(it)
                    }
                }
            }
            .toMaybeAppResult()
            .subscribe({
                println("RES:$it")
            },{
                println("ERROR:$it")
            },{
                println("COMPLETE")
            })
    }
}