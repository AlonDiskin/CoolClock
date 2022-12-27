Feature: User journey to launch scheduled alarm

  Scenario: App launch alarm
    Given app has scheduled alarm
    When alarm trigger time arrive
    Then app should launch alarm as urgent message in device according to its config
    When user snooze alarm
    And lock device
    When snoozed time pass
    Then app should launch alarm in full screen, according to its config