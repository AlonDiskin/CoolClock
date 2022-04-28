Feature: User journey to use app timer

  Scenario: User activates timer
    Given User launched app fro device home screen
    Then app should open timer screen
    When he set a timer
    And pause it
    And leave app
    Then app should show paused timer in device status bar notification