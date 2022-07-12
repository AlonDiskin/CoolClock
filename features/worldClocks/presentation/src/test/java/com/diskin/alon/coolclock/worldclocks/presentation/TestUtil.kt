package com.diskin.alon.coolclock.worldclocks.presentation

import com.diskin.alon.coolclock.worldclocks.application.model.CityDto
import com.diskin.alon.coolclock.worldclocks.presentation.model.UiCityClock
import com.diskin.alon.coolclock.worldclocks.presentation.model.UiCitySearchResult

fun createUiCitySearchResults() = listOf(
    UiCitySearchResult(1,"new york","usa","NY",false),
    UiCitySearchResult(2,"london","england","",false),
    UiCitySearchResult(3,"jerusalem","israel","",true),
    UiCitySearchResult(4,"rome","italy","",true)
)

fun createCityDtoSearchResults() = listOf(
    CityDto(1,"city_1","country_1","state_1","gmt1",true),
    CityDto(2,"city_1","country_2","state_2","gmt2",false),
    CityDto(3,"city_1","country_3","state_3","gmt3",false)
)

fun createUiCityClocks() = listOf(
    UiCityClock(1,"new york","usa","NY","America/Chicago"),
    UiCityClock(2,"london","england","","America/Detroit"),
    UiCityClock(3,"jerusalem","israel","","Asia/Jerusalem"),
    UiCityClock(4,"rome","italy","","Europe/Rome")
)