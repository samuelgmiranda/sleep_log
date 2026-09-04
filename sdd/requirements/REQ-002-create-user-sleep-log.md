# Requirement: REQ-002

ID: REQ-002
Artifact ID: ART-001
Name: Create a user sleep log
Description: Provide a REST endpoint that creates a sleep log for the integer user identified by the X-User-Id header.
Acceptance Criteria:
- The controller extends BaseController and obtains the user ID through getUserId.
- The request accepts startDate and endDate in MM/dd/yyyy HH:mm format, and userFeel values BAD, OK, or GOOD.
- Missing fields, invalid date values, invalid userFeel values, an endDate not later than startDate, and duplicate sleep dates return 400.
- The service validates duplicate sleep dates for the authenticated user before persistence.
- A valid request stores the log with total time calculated in minutes and returns 200 with the saved-successfully response.
Priority: High
Status: REFINED
Dependencies: REQ-001
