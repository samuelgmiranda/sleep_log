# Specification: SPEC-003

ID: SPEC-003
Requirement ID: REQ-003
Name: Implement sleep-log retrieval API
Purpose: Return one user-scoped sleep log whose startDate falls within the complete target local date.
Implementation Scope: Add a Kotlin GET controller endpoint, dedicated target-date validation, service lookup behavior, and a DAO query. Omitted targetDate resolves to the previous local date on the application host; explicit dates resolve to the supplied local date.
Expected Code Changes:
- Add a GET endpoint in a BaseController subclass accepting nullable `targetDate` in `MM/dd/uuuu` format, matching `DateUtil` strict parsing.
- Add a service that resolves targetDate or `LocalDate.now().minusDays(1)`, derives the local date window using `DateUtil`, and returns 400 for malformed dates or 404 when no log exists.
- Extend the DAO interface and implementation with a query restricted to the integer user ID and `startDate >= startOfDay` and `startDate < startOfNextDay`, covering 00:00:00 through 23:59:59 of the target date.
- Map the selected row in the DAO implementation and compose a `SleepLogDTO` in the service with id, targetDate, sleepDuration, startSleep, endSleep, and userFeel.
- Keep all date, time, duration, and ordinal formatting utilities in DateUtil.
Dependencies: SPEC-001
Implementation Trace: REQ-003 -> SPEC-003 -> SleepLogController.kt, SleepLogValidator.kt, SleepLogService.kt, SleepLogDAO.kt, SleepLogDAOImpl.kt, SleepLog.kt, SleepLogDTO.kt, DateUtil.kt -> user-scoped full-day lookup and service-layer response composition using [startOfDay, startOfNextDay) -> Validation: none.
Implementation Evidence: `.\gradlew.bat test --console=plain --no-daemon '-Dkotlin.compiler.execution.strategy=in-process'` completed successfully on 2026-09-05. Service tests cover `st`, `nd`, `rd`, and `th` ordinal suffixes, including 11th–13th exceptions, response formatting, feeling mapping, previous-day default, and missing records. No Validation entity was created because `VALIDATE SPEC-003` was not requested.
Status: DONE
