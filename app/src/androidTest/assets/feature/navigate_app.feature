Feature: Navigate through app features

  Scenario: User navigate through app features
    Given User launched app fro device home screen
    Then app should open alarms screen
    When he navigates to world clocks feature
    Then app should open world clocks screen
    When he navigates to timer feature
    Then app should open timer screen
    When he navigates to settings feature
    Then app should open settings screen
