package com.diskin.alon.coolclock.alarms.data

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.coolclock.alarms.data.local.AlarmDao
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntity
import com.diskin.alon.coolclock.alarms.data.local.AlarmEntityConverters
import com.diskin.alon.coolclock.alarms.domain.Sound
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
    fun updateAlarm() {
        // Given
        val dbConverters = AlarmEntityConverters()
        val existingAlarm = "INSERT INTO user_alarms (name,hour,minute,repeatDays,isScheduled,sound" +
                ",isVibrate,duration,volume,snooze,isSnoozed,id)" +
                "VALUES ('name_1',15,10,'empty',0,'ringtone_1',0,5,5,0,0,1)"
        val updatedAlarm = AlarmEntity(
            "name_2",
            14,
            30,
            emptySet(),
            true,
            Sound.AlarmSound("sound_5"),
            true,
            10,
            5,
            10,
            true,
            1
        )

        db.compileStatement(existingAlarm).executeInsert()

        // When
        dao.update(updatedAlarm).blockingAwait()

        // Then
        val actualName = db.compileStatement("SELECT name FROM user_alarms WHERE id = 1").simpleQueryForString()
        val actualHour = db.compileStatement("SELECT hour FROM user_alarms WHERE id = 1").simpleQueryForString().toInt()
        val actualMinute = db.compileStatement("SELECT minute FROM user_alarms WHERE id = 1").simpleQueryForString().toInt()
        val actualRepeat = dbConverters.stringToWeekDays(db.compileStatement("SELECT repeatDays FROM user_alarms WHERE id = 1").simpleQueryForString())
        val actualScheduled = when(db.compileStatement("SELECT isScheduled FROM user_alarms WHERE id = 1").simpleQueryForString()) {
            "1" -> true
            else -> false
        }
        val actualSound = dbConverters.stringToSound(db.compileStatement("SELECT sound FROM user_alarms WHERE id = 1").simpleQueryForString())
        val actualVibration = when(db.compileStatement("SELECT isVibrate FROM user_alarms WHERE id = 1").simpleQueryForString()) {
            "1" -> true
            else -> false
        }
        val actualDuration = db.compileStatement("SELECT duration FROM user_alarms WHERE id = 1").simpleQueryForString().toInt()
        val actualVolume = db.compileStatement("SELECT volume FROM user_alarms WHERE id = 1").simpleQueryForString().toInt()
        val actualSnooze = db.compileStatement("SELECT snooze FROM user_alarms WHERE id = 1").simpleQueryForString().toInt()
        val actualIsSnoozed = when(db.compileStatement("SELECT isSnoozed FROM user_alarms WHERE id = 1").simpleQueryForString()) {
            "1" -> true
            else -> false
        }
        val actualUpdated = AlarmEntity(
            actualName,
            actualHour,
            actualMinute,
            actualRepeat,
            actualScheduled,
            actualSound,
            actualVibration,
            actualDuration,
            actualVolume,
            actualSnooze,
            actualIsSnoozed,
            1
        )

        assertThat(actualUpdated).isEqualTo(updatedAlarm)
    }

    @Test
    fun updateSnoozedState() {
        // Given
        val id = 1
        val existingAlarm = "INSERT INTO user_alarms (name,hour,minute,repeatDays,isScheduled,sound" +
                ",isVibrate,duration,volume,snooze,isSnoozed,id)" +
                "VALUES ('name_1',15,10,'empty',0,'ringtone_1',0,5,5,0,0,$id)"
        val updatedSnoozedState = false

        db.compileStatement(existingAlarm).executeInsert()

        // When
        dao.updateSnoozed(id,updatedSnoozedState).blockingAwait()

        // Then
        val actualIsSnoozed = when(db.compileStatement("SELECT isSnoozed FROM user_alarms WHERE id = $id").simpleQueryForString()) {
            "1" -> true
            else -> false
        }

        assertThat(actualIsSnoozed).isEqualTo(updatedSnoozedState)
    }
}