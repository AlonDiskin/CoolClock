package com.diskin.alon.coolclock.common.presentation

sealed class ViewUpdateState {

    object EndLoading : ViewUpdateState()

    object Loading : ViewUpdateState()
}