Feature: User selected world clocks browser feature rules

  #Rule: Show a listing of user world clocks

  @show-clocks
  Scenario: User browse clocks listing
    Given user opened clocks screen
    Then app should show all clocks in adding order

  @clock-deleted
  Scenario: User remove clock from listing
    Given user opened clocks screen
    When he select to remove the first shown clock
    Then app should remove it from listing
    And update shown clocks accordingly