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

  #Rule: Replace existing alarm,with newly created one,if alarm time is tha same

  @alarm-replaced
  Scenario: Existing alarm replaced
    Given app has existing scheduled alarm
    When user schedule new alarm with trigger time equal to existing one
    Then app should remove existing alarm,and add new one

  #Rule: Scheduling alarms notifies user

  @user-notified
  Scenario: User notified of alarm trigger time
    Given user edit new alarm
    When he confirm alarm schedule
    Then app should notify user about time left to alarm trigger

  #Rule: Confirming update edits,reschedule existing alarm

  @alarm-updated
  Scenario Outline: User update existing alarm
    Given app has "<scheduled_state>" existing alarm
    When user updates existing alarm time
    Then app should reschedule alarm according to update
    Examples:
      | scheduled_state |
      | scheduled       |
      | not scheduled   |

   @alarm-removed
   Scenario: Update remove existing alarm
     Given app has 2 existing scheduled alarms
     When user updates one of them to have trigger time same as other
     Then app should remove other alarm





