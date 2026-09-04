# Requirement: REQ-004

ID: REQ-004
Artifact ID: ART-001
Name: Calculate user sleep-history averages
Description: Provide a REST endpoint that returns a user's local-date history range and aggregate sleep information.
Acceptance Criteria:
- The controller extends BaseController and scopes history to the integer X-User-Id header.
- The range is derived from historyDays, ending on the current local date.
- The response includes the range, average total time in bed, average bedtime, average wake time, and morning-feeling frequencies.
- Aggregate times are represented as HH:mm and feeling frequencies as percentages.
Priority: High
Status: DRAFT
Dependencies: REQ-001
Decision Required: Choose the aggregation strategy: a PostgreSQL stored procedure that returns complete averages; fetching date-filtered logs and calculating in the service layer; or another reviewed approach that best meets this API's performance and maintainability needs.
