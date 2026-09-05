# Requirement: REQ-003

ID: REQ-003
Artifact ID: ART-001
Name: Fetch a user sleep log by date
Description: Provide a REST endpoint that returns a user's sleep log for an optional local target date; when omitted, it returns the previous local day's sleep log.
Acceptance Criteria:
- The controller extends BaseController and scopes lookup to the integer X-User-Id header.
- targetDate uses MM/dd/uuuu format; an omitted targetDate resolves to the previous local date where the application runs.
- The lookup covers the complete resolved local date, from 00:00:00 through 23:59:59, implemented as start-of-day inclusive and next-day start exclusive.
- A matching log is selected when startDate is within the resolved date window and returns id, targetDate, sleepDuration, startSleep, endSleep, and userFeel.
- The response formats targetDate as `MMMM d<ordinal suffix>`, sleepDuration as zero-padded `HH:mm`, startSleep and endSleep as lowercase `h:mm am/pm`, and userFeel as BAD, OK, or GOOD.
- A missing log returns 404; an invalid targetDate returns 400.
- The request flow is strictly controller -> dedicated validator -> service -> DAO interface -> DAO implementation; no controller-to-DAO call is allowed.
- The dedicated validator owns targetDate format validation, while service code owns lookup/business decisions and DAO code owns SQL only.
- BaseController is the sole caller of BaseControllerValidator for X-User-Id validation.
- Date defaults, parsing, local-date extraction, and day boundaries use DateUtil; controllers do not perform date manipulation.
- ResourceNotFoundException and InvalidRequestException are handled centrally by ApiExceptionHandler with HTTP 404/400 and the `{status:"error",message:"..."}` response shape.
Priority: High
Status: DONE
Dependencies: REQ-001
Structure Constraints: Production files remain in the established exact packages; the service depends on a DAO abstraction and never on a JDBC implementation or HTTP request object.
