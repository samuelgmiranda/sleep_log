# Requirement: REQ-004

ID: REQ-004
Artifact ID: ART-001
Name: Calculate user sleep-history averages
Description: Provide a REST endpoint that returns a user's local-date history range and aggregate sleep information.
Acceptance Criteria:
- The controller extends BaseController and scopes history to the integer X-User-Id header.
- The endpoint accepts nullable historyDays; when absent, it defaults to 30.
- The effective non-null historyDays value is mandatory for deriving the local-date range, which ends on the current local date, and for filtering data in the repository.
- The response includes the range, average total time in bed, average bedtime, average wake time, and morning-feeling frequencies.
- Average bedtime and wake time use midnight-aware minute normalization: early-morning values in a set spanning midnight are treated as continuing after the previous evening, then averaged arithmetically. Any fractional-minute mean is rounded up with ceiling, normalized modulo 1440, and formatted as HH:mm; feeling frequencies are percentages.
- Midnight-aware averages include 22:00 and 05:00 producing 01:30, 23:00 and 01:00 producing 00:00, and 23:50 and 00:10 producing 00:00.
- A requested range with no matching sleep logs returns 404.
Priority: High
Status: REFINED
Dependencies: REQ-001
Decision: Query date-filtered sleep logs in the repository and calculate aggregates in the service layer. The service orchestrates a repository dependency that can be mocked with date-window results, allowing unit tests to cover aggregation scenarios and edge cases without a database.
