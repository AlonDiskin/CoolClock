Feature: Alarms editor feature rules

  #Rule: Confirming edits,schedules new active alarms

  @new-scheduled
  Scenario Outline: New alarm scheduled
    Given user edited new alarm with selected "<sound>","<vibration>","<snooze>","<duration>","<repeat>","<hour>","<minute>","<name>","<volume>"
    When he confirms edit selection
    Then app should schedule an alarm according to selected alarms values
    Examples:
      | sound   | vibration | snooze    | duration   | repeat   | hour    | minute  | name       | volume  |
      | default | default   | default   | default    | default  | default | default | default    | default |
      | sound 1 | on        | 5 minutes | 5 minutes  | all week | 16      | 15      | yoga class | min     |

  @unconfirmed-not-scheduled
  Scenario: Unconfirmed edit not scheduled
    Given user edited a new alarm wth selected values
      | Property  | Value         |
      | Hour      | 16            |
      | Minute    | 15            |
      | Name      | go for a walk |
      | Vibration | true          |
    When he leave editor without confirming edit selection
    Then app should not schedule selected edited alarm

