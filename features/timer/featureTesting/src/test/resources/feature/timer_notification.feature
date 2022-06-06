Feature: Timer notification features

  #Rule: Show urgent notification upon time up

  @alert-notification
  Scenario: Notification shown after timer done
    Given user started timer
    When timer is finished
    Then app should show urgent notification with ongoing alarm sound

  #Rule: Show ongoing status bar notification while timer is active

  @timer-notification
  Scenario Outline: Active timer notification shown
    Given user started timer
    When he close timer screen
    Then app should show timer notification in status bar
    When user "<action>" timer via notification
    Then app should "<result>" timer
    Examples:
      | action  | result       |
      | pause   | pause timer  |
      | cancel  | cancel timer |


