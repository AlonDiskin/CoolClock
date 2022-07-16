package com.diskin.alon.coolclock.worldclocks.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class CityEntity(val name: String,
                      val country: String,
                      val state: String,
                      val population: Int,
                      val timezone: String,
                      val isSelected: Boolean = false,
                      val selectedDate: Long? = null,
                      @PrimaryKey(autoGenerate = true) val id: Long? = null)