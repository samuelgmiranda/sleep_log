# API test scripts

## Purpose

`sleep/test-scripts/run-sleep-api-test-matrix.ps1` is a repeatable API verification script for the seeded sleep-history scenarios.

It performs 55 HTTP requests against the sleep API, covering:

- last night's sleep (`GET /sleep-logs`);
- history for 1, 7, 30 (default), and 365 days;
- the expected `404` case for user `5`;
- successful results for users `6` through `15`;
- user `15`, which has data for every history window.

For every request, it compares the HTTP status and complete JSON response with the expected value. It prints `PASSED` or `FAILED`, including the raw expected and actual JSON. A failed run exits with code `1`.

## Prerequisites

- Docker Desktop running.
- PowerShell and `curl.exe` available on Windows.
- Ports `5432` and `8080` available.

## Start the database and API

Run these commands from the repository root, `C:\workspace\sleep_log`.

```powershell
docker compose up -d db
docker compose build sleep_api
docker compose run -d --service-ports -e SPRING_PROFILES_ACTIVE=api-test sleep_api
```

The `api-test` profile fixes the application date to **September 5th, 2026** in the `America/Sao_Paulo` time zone. This makes the matrix results reproducible.

On startup, Flyway validates and applies any migration not yet recorded in `flyway_schema_history`, including the matrix seed migrations (`V1.4` through `V1.14`).

If port `8080` is already occupied by a previous API container, stop the current Compose services first, then run the commands above:

```powershell
docker compose down
```

`docker compose down` stops containers but does not delete the bind-mounted `db` directory. Do not add `-v` or delete `db` unless you intentionally want a fresh database.

## Run the test matrix

After the API is running, execute:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\sleep\test-scripts\run-sleep-api-test-matrix.ps1
```

The script targets `http://127.0.0.1:8080` by default. To test another compatible API address, pass `-BaseUrl`:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\sleep\test-scripts\run-sleep-api-test-matrix.ps1 -BaseUrl http://127.0.0.1:8080
```

If the API is not listening, the script stops immediately with a clear `app musts be running` message.

## Expected final summary

With the migrations applied and the API started using `api-test`, the summary is:

```text
Summary: 55 passed, 0 failed, 55 total.
```
