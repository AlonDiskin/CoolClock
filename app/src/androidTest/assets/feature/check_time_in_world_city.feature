Feature: User journey check time at world city

  Scenario: User check time at different world city
    Given user added a city after searching for it
    When he browse to city in world clocks listing
    Then app should show city local time