Feature: Alarms alarm controller feature rules

  #Rule: Launch alarm as urgent message notification

  @alarm-launched
  Scenario Outline: Alarm launched as with user configuration
    Given user has scheduled alarm with "<sound>","<vibration>","<name>","<repeated>", and "<snooze>" configurations
    When alarm launched by app
    Then app should set off alarm, according to user configurations
    Examples:
      | sound      | vibration  | name         | repeated | snooze      |
      | active     | active     | jym session  | true     | enabled     |
      | non active | non active | bank meeting | false    | disabled    |

  @alarm-controlled
  Scenario Outline: Ongoing alarm controlled
    Given "<alarm_type>" alarm is launched by app
    When user perform "<user_action>" on alarm
    Then app should disable alarm
    And reschedule it if is snoozed
    Examples:
      | alarm_type   | user_action |
      | one off      | cancel      |
      | one off      | snooze      |
      | repeated     | cancel      |
      | repeated     | snooze      |