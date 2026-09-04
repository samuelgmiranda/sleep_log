# Requirement: REQ-005

ID: REQ-005
Artifact ID: ART-001
Name: Support sleep intervals that cross midnight
Description: Correctly validate, calculate, persist, and retrieve a sleep interval whose end timestamp falls on the local calendar day after its start timestamp.
Acceptance Criteria:
- An interval starting before midnight and ending after midnight is accepted when endDate is after startDate.
- Its total time is calculated from the complete date-time interval in minutes using java.time.Duration; raw LocalTime comparison alone must not be used for this calculation.
- The local date of startDate defines the sleep date for duplicate prevention and date retrieval; endDate falling on the following local date does not change it.
- For example, a log for userId 1 from 11/01/2026 21:00 to 11/02/2026 07:00 prevents another log for userId 1 starting on 11/01/2026, including one from 11/01/2026 22:00 to 11/02/2026 05:00.
- The requirement has explicit validation scenarios for a valid cross-midnight interval, endDate not after startDate, and a duplicate log on the same user and sleep date.
Priority: High
Status: REFINED
Dependencies: REQ-001, REQ-002, REQ-003
