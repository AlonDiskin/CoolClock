package com.diskin.alon.coolclock.common.uitesting

class UnknownScenarioArgumentException(scenarioArgumentName: String) : IllegalArgumentException(
    "Unknown scenario argument: $scenarioArgumentName"
)