package com.diskin.alon.coolclock.alarms.featuretesting.util

import java.lang.reflect.Field
import java.lang.reflect.Modifier

fun setFinalStatic(field: Field, newValue: Any?) {
    field.isAccessible = true
    val modifiersField: Field = try {
        Field::class.java.getDeclaredField("accessFlags")
    } catch (e: NoSuchFieldException) {
        //This is an emulator JVM  ¯\_(ツ)_/¯
        Field::class.java.getDeclaredField("modifiers")
    }
    modifiersField.isAccessible = true
    modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
    field.set(null, newValue)
}