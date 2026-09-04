# Requirement: REQ-003

ID: REQ-003
Artifact ID: ART-001
Name: Fetch a user sleep log by date
Description: Provide a REST endpoint that returns a user's sleep log for an optional local target date.
Acceptance Criteria:
- The controller extends BaseController and scopes lookup to the integer X-User-Id header.
- An omitted targetDate uses the current local date where the application runs.
- A matching log returns its sleep information.
- A missing log returns 404; an invalid targetDate returns 400.
Priority: High
Status: DRAFT
Dependencies: REQ-001
