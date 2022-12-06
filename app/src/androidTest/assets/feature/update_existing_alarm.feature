Feature: User journey to update an existing alarm

  Scenario: Existing alarm is updated
    Given user has an existing alarm
    And he launched app from home screen
    And open new alarm editor
    When update trigger time for existing alarm
    Then app should reschedule alarm
    And display it in alarms browser