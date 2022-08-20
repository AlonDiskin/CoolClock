Feature: User alarms browser feature rules

  #Rule: Show a listing of user world clocks

  @list-alarms
  Scenario: User browse alarms listing
    Given user opened alarms screen
    Then app should show all alarms listing in descending adding order

  #Rule: Config alarm activation from browser

  @set-alarm-activation
  Scenario Outline: Alarm activation is changed
    Given user browsed to alarm that is in "<current activation>" state
    When he switch activation to "<changed activation>"
    Then app should change alarm activation to "<changed activation>"
    Examples:
      | current activation | changed activation |
      | active             | not active         |
      | not active         | active             |