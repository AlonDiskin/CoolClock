Feature: User journey to disable previously scheduled alarm

  Scenario: Alarm disabled
    Given user disabled the last created alarm
    When when alarm time arrive
    Then app should not fire the alarm