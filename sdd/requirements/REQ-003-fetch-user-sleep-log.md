# Requirement: REQ-003

ID: REQ-003
Artifact ID: ART-001
Name: Fetch a user sleep log by date
Description: Provide a REST endpoint that returns a user's sleep log for an optional local target date; when omitted, it returns the previous local day's sleep log.
Acceptance Criteria:
- The controller extends BaseController and scopes lookup to the integer X-User-Id header.
- targetDate uses MM/dd/yyyy format; an omitted targetDate uses the previous local date where the application runs.
- A matching log is selected by the local date portion of startDate and returns its id, startDate, endDate, totalTime, and userFeel.
- A missing log returns 404; an invalid targetDate returns 400.
Priority: High
Status: REFINED
Dependencies: REQ-001
