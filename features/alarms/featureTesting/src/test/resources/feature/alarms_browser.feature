Feature: User alarms browser feature rules

  #Rule: Show a listing of user world clocks

  @list-alarms
  Scenario: User browse alarms listing
    Given user opened alarms screen
    Then app should show all alarms listing in descending adding order