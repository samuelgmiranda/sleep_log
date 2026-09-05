# Validation: VAL-003

ID: VAL-003
Specification ID: SPEC-003
Requirement ID: REQ-003
Validation Type: Unit Test
Scenario: Validate service-layer SleepLogDTO composition and formatting for a user-scoped sleep-log lookup.
Expected Result: The service returns the required DTO fields with ordinal target-date suffixes (`st`, `nd`, `rd`, `th`, including 11th–13th exceptions), zero-padded duration, lowercase 12-hour start/end times, and BAD/OK/GOOD feeling names. Previous-day default and missing-record behavior remain correct.
Evidence: Test file: sleep/src/test/java/com/noom/interview/fullstack/sleep/service/SleepLogServiceTest.java. Command: `.\\gradlew.bat test --console=plain --no-daemon '-Dkotlin.compiler.execution.strategy=in-process'`. Assertions cover all requested ordinalities, 11th–13th exceptions, 540-minute `09:00` duration, `11:00 pm`/`08:00 am` times, all userFeel values, previous local date default, no log for the previous local date returning ResourceNotFoundException, and missing-record behavior. Tests use JUnit 5 and Mockito; PowerMock is not technically necessary. Result: BUILD SUCCESSFUL on 2026-09-05; 11 tests passed.
Status: DONE
