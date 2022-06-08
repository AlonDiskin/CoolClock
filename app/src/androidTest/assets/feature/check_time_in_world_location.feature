Feature: User journey check time at world location

  Scenario: User check time at different world city
    Given user added a world location from
    When he scroll to location in world clocks listing
    Then app should show location local time