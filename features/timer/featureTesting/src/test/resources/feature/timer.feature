Feature: Simple timer

  #Rule: Control timer

  @control-timer
  Scenario Outline: User control timer
    Given user started selected timer
    When user "<action>" timer
    Then then app should "<result>" timer
    Examples:
      | action | result |
      | pause  | pause  |
      | none   | start  |
      | cancel | stop   |

  @timer-relaunch
  Scenario: Timer continue after user leave timer screen
    Given user started timer
    When user leave timer screen
    And return to timer screen
    Then app should display current timer
