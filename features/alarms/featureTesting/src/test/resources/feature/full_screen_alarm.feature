Feature: Full screen alarm display

  #Rule: Launch alarm in full screen,when device locked

  @alarm-launched
  Scenario Outline: Full screen alarm launched
    Given user has scheduled alarm with "<sound>","<vibration>","<name>","<repeated>", and "<snooze>" configurations
    When full screen alarm launched by app
    Then app should set off alarm, according to user configurations
    Examples:
      | sound      | vibration  | name         | repeated | snooze      |
      | active     | active     | jym session  | true     | enabled     |
      | non active | non active | bank meeting | false    | disabled    |

  @alarm-controlled
  Scenario Outline: Ongoing alarm controlled
    Given full screen alarm was launched by app
    When user perform "<user_action>" on alarm
    Then app should disable alarm
    And close alarm screen
    Examples:
      | user_action |
      | dismiss     |
      | snooze      |

  #Rule: Dismiss alarm screen, when alarm stopped from notification

  @alarm-dismissed
  Scenario Outline: Alarm screen dismissed from notification
    Given full screen alarm launched by app
    When user perform "<notification_action>" from alarm notification
    Then app should close alarm screen
    Examples:
      | notification_action |
      | dismiss             |
      | snooze              |


