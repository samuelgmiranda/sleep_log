# Requirement: REQ-002

ID: REQ-002
Artifact ID: ART-001
Name: Create a user sleep log
Description: Provide a REST endpoint that creates a sleep log for the integer user identified by the X-User-Id header.
Acceptance Criteria:
- The controller extends BaseController and obtains the user ID through getUserId.
- The request accepts startDate, endDate, and userFeel values BAD, OK, or GOOD.
- Missing fields, invalid date values, invalid userFeel values, invalid intervals, and duplicates return 400.
- A valid request stores the log with total time calculated in minutes and returns success.
Priority: High
Status: DRAFT
Dependencies: REQ-001
