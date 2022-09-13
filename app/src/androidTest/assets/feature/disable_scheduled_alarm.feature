Feature: User journey to disable previously scheduled alarm

  Scenario: Alarm disabled
    Given user open alarms browser screen
    And activate non active alarm
    Then app should schedule it
    When he delete scheduled active alarm
    Then app should cancel alarm
    And delete it from user alarms data