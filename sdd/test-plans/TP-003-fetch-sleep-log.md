# Test Plan: TP-003

ID: TP-003
Requirement ID: REQ-003
Objective: Verify date-based retrieval is scoped to the header user.
Scenario: Request a log with an explicit targetDate and without one.
Input: Integer X-User-Id; valid, invalid, omitted, and unmatched MM/dd/uuuu targetDate values.
Expected Result: The matching user's log is returned when startDate falls from the resolved date at 00:00:00 through 23:59:59; omitted targetDate uses the previous local date; response fields use the specified human-readable formats; missing logs return 404 and malformed dates return 400.
Edge Cases: Ordinal suffixes `st`, `nd`, `rd`, and `th`, including 11th–13th; records exactly at 00:00:00 and 23:59:59; a record at the next date's 00:00:00; a second user's log on the same date; a cross-midnight log; no log for the previous local date.
Priority: High
Status: APPROVED
