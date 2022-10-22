Feature: User journey to edit an alarm

  Scenario: Alarm is edited
    Given user has edited a silent alarm
    When selected alarm time arrive
    Then app should set of alarm
    And app should not play ringtone sound