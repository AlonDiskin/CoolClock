Feature: User selected world clocks browser feature rules

  #Rule: Show a listing of user world clocks

  @show-clocks
  Scenario: User browse clocks listing
    Given user opened clocks screen
    Then app should show all clocks in adding order

  #Rule: Enable clock deletion

  @clock-deleted
  Scenario: User remove clock from listing
    Given user opened clocks screen
    When he select to remove the first shown clock
    Then app should remove it from listing
    And update shown clocks accordingly

  #Rule: Enable clock time sharing

  @time-shared
  Scenario: City time shared
    Given user selected to share the time of first listed world city clock
    Then app should share city time via device sharing menu