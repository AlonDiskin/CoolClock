Feature: World clocks search feature rules

  #Rule: Provide world locations search by name

  @search-city
  Scenario Outline: Show results for city search
    Given user opened cities search screen
    When he perform a city search with query that has "<existing>" results
    Then app "<display>" show results start with query,ordered by city population
    Examples:
      | existing    | display    |
      | matching    | should     |
      | no matching | should not |

  #Rule: Enable adding world city to user list

  @add-city
  Scenario Outline: User adds world city
    Given user found a "<city>" via search
    When he select to add it to his list
    Then app should "<add>" city to user list
    Examples:
      | city           | add     |
      | not added city | add     |
      | added city     | not add |

