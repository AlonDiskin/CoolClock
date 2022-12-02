Feature: User journey to edit an alarm

  Scenario: New alarm is scheduled
    Given user launch app from home screen
    And open new alarm editor
    When he confirm new alarm edit
    Then app should schedule alarm
    And display it in alarms browser