# Requirement: REQ-001

ID: REQ-001
Artifact ID: ART-001
Name: Persist sleep-log data
Description: Create the PostgreSQL SLEEPLOG schema through Flyway so a sleep log is stored with its identifier, integer user ID, start and end timestamps, total time in minutes, and morning-feeling value.
Acceptance Criteria:
- Flyway creates the required sleep-log table on an empty database.
- Each stored log includes id, userId, startDate, endDate, totalTime, and userFeel.
- The createSleepLog service validates that a user has no existing sleep log with the same startDate local date; this rule is not enforced by the database schema.
- The startDate local date defines the sleep date. For example, a log for userId 1 from 11/01/2026 21:00 to 11/02/2026 07:00 prevents another log for userId 1 starting on 11/01/2026, including one from 11/01/2026 22:00 to 11/02/2026 05:00.
Priority: High
Status: REFINED
Dependencies: None
Refinement: startDate and endDate retain both local date and time. totalTime is persisted as an integer number of minutes. userFeel is persisted as the diagram's integer value: BAD=1, OK=2, GOOD=3.
