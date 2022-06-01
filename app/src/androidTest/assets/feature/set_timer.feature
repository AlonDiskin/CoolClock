Feature: User journey to use app timer

  Scenario: User set timer
    Given user started a timer
    When timer finish
    Then app should show urgent status bar notification with alarm sound
    When user dismiss notification
    Then app should remove notification with alarm sound sound