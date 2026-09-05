# Requirement: REQ-006

ID: REQ-006
Artifact ID: ART-001
Name: Unify sleep-log DAO lookup methods
Description: Replace the separate `existsForUserAndSleepDate` Boolean lookup and `findByUserAndStartDateBetween` SleepLog lookup with one user-and-date DAO operation that returns the matching SleepLog object or null. Preserve the established `[startOfDay, startOfNextDay)` date-window semantics and keep SQL and row mapping inside the DAO implementation.
Acceptance Criteria:
- SleepLogDAO exposes one unified user-and-sleep-date lookup for retrieval and duplicate detection.
- The unified DAO operation returns a SleepLog object when a record exists and null when no record exists; it does not return Boolean.
- The DAO implementation preserves user ID scoping and the `[startOfDay, startOfNextDay)` start-date window.
- SleepLogService duplicate validation remains behaviorally equivalent by checking whether the returned SleepLog is non-null.
- SleepLogService retrieval remains behaviorally equivalent, including ResourceNotFoundException when the returned object is null.
- Existing create and fetch API behavior remains compatible after the service-layer adjustment.
- No controller, validator, or database schema responsibility is introduced by this change.
Priority: Medium
Status: DRAFT
Dependencies: REQ-001, REQ-003
