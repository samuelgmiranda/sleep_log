[CmdletBinding()]
param(
    [string]$BaseUrl = "http://127.0.0.1:8080"
)

$ErrorActionPreference = "Stop"

function Test-ApiIsRunning {
    param([Parameter(Mandatory = $true)][string]$Url)

    try {
        $uri = [Uri]$Url
        $client = [System.Net.Sockets.TcpClient]::new()
        $connection = $client.BeginConnect($uri.Host, $uri.Port, $null, $null)
        if (-not $connection.AsyncWaitHandle.WaitOne(2000)) {
            $client.Close()
            return $false
        }

        $client.EndConnect($connection)
        $client.Close()
        return $true
    } catch {
        return $false
    }
}

if (-not (Test-ApiIsRunning $BaseUrl)) {
    Write-Host "app musts be running at $BaseUrl before executing the API test matrix." -ForegroundColor Red
    exit 2
}

function ConvertTo-CanonicalJson {
    param([Parameter(Mandatory = $true)][string]$Json)

    return ($Json | ConvertFrom-Json | ConvertTo-Json -Compress -Depth 20)
}

function Invoke-MatrixRequest {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][int]$UserId,
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][int]$ExpectedStatus,
        [Parameter(Mandatory = $true)][string]$ExpectedBody
    )

    $uri = "$($BaseUrl.TrimEnd('/'))$Path"
    # curl.exe is used instead of Invoke-WebRequest so this works in both
    # Windows PowerShell 5.1 and PowerShell 7 when the API returns HTTP 4xx.
    $output = @(& curl.exe --silent --show-error --request GET --header "X-User-Id: $UserId" --write-out "`n%{http_code}" $uri)
    if ($LASTEXITCODE -ne 0) {
        Write-Host "FAILED - $Name - request error: $($output -join ' ')" -ForegroundColor Red
        return $false
    }

    if ($output.Count -eq 0) {
        Write-Host "FAILED - $Name - request returned no response" -ForegroundColor Red
        return $false
    }

    $actualStatus = [int]$output[-1]
    $actualBody = if ($output.Count -gt 1) { $output[0..($output.Count - 2)] -join "`n" } else { "" }

    if ($actualStatus -ne $ExpectedStatus) {
        Write-Host "FAILED - $Name - expected HTTP $ExpectedStatus, received HTTP $actualStatus" -ForegroundColor Red
        Write-Host "  EXPECTED JSON: $ExpectedBody" -ForegroundColor DarkYellow
        Write-Host "  ACTUAL JSON:   $actualBody" -ForegroundColor DarkYellow
        return $false
    }

    try {
        $normalizedExpected = ConvertTo-CanonicalJson $ExpectedBody
        $normalizedActual = ConvertTo-CanonicalJson $actualBody
    } catch {
        Write-Host "FAILED - $Name - response is not valid JSON: $actualBody" -ForegroundColor Red
        return $false
    }

    if ($normalizedActual -ne $normalizedExpected) {
        Write-Host "FAILED - $Name - JSON differs. Expected: $normalizedExpected; received: $normalizedActual" -ForegroundColor Red
        return $false
    }

    Write-Host "PASSED - $Name - HTTP $actualStatus" -ForegroundColor Green
    Write-Host "  EXPECTED JSON: $normalizedExpected"
    Write-Host "  ACTUAL JSON:   $normalizedActual"
    return $true
}

$notFoundLog = '{"status":"error","message":"Sleep log not found"}'
$notFoundHistory = '{"status":"error","message":"Sleep history not found"}'

$scenarioNames = @{
    5 = "Symmetric times across midnight"
    6 = "Immediately around midnight"
    7 = "Different durations across month boundary"
    8 = "Cross-midnight times across year boundary"
    9 = "Wide range across leap day"
    10 = "Circular time-gap tie prefers midnight"
    11 = "Timestamp seconds are rounded up"
    12 = "Fractional minute across midnight"
    13 = "Mostly late nights with early outlier"
    14 = "Mostly early nights with late outlier"
    15 = "Complete 365-day API-window coverage"
}

$annualHistory = @{
    6 = '{"userId":6,"dateRangeStart":"Sep 5th","dateRangeEnd":"Sep 5th","averageDuration":"08:00","averageStart":"11:57 pm","averageEnd":"07:57 am","userFeelTotals":{"BAD":10,"OK":11,"GOOD":10}}'
    7 = '{"userId":7,"dateRangeStart":"Sep 5th","dateRangeEnd":"Sep 5th","averageDuration":"07:46","averageStart":"10:15 pm","averageEnd":"06:00 am","userFeelTotals":{"BAD":10,"OK":11,"GOOD":10}}'
    8 = '{"userId":8,"dateRangeStart":"Sep 5th","dateRangeEnd":"Sep 5th","averageDuration":"08:00","averageStart":"10:00 pm","averageEnd":"06:00 am","userFeelTotals":{"BAD":0,"OK":1,"GOOD":0}}'
    9 = '{"userId":9,"dateRangeStart":"Sep 5th","dateRangeEnd":"Sep 5th","averageDuration":"08:00","averageStart":"10:00 pm","averageEnd":"06:00 am","userFeelTotals":{"BAD":0,"OK":1,"GOOD":0}}'
    10 = '{"userId":10,"dateRangeStart":"Sep 5th","dateRangeEnd":"Sep 5th","averageDuration":"03:20","averageStart":"11:20 pm","averageEnd":"02:40 am","userFeelTotals":{"BAD":1,"OK":2,"GOOD":0}}'
    11 = '{"userId":11,"dateRangeStart":"Sep 5th","dateRangeEnd":"Sep 5th","averageDuration":"10:40","averageStart":"11:21 pm","averageEnd":"02:01 am","userFeelTotals":{"BAD":1,"OK":2,"GOOD":0}}'
    12 = '{"userId":12,"dateRangeStart":"Sep 5th","dateRangeEnd":"Sep 5th","averageDuration":"07:16","averageStart":"11:30 pm","averageEnd":"06:45 am","userFeelTotals":{"BAD":1,"OK":2,"GOOD":1}}'
    13 = '{"userId":13,"dateRangeStart":"Sep 5th","dateRangeEnd":"Sep 5th","averageDuration":"08:00","averageStart":"11:02 pm","averageEnd":"07:02 am","userFeelTotals":{"BAD":0,"OK":31,"GOOD":0}}'
    14 = '{"userId":14,"dateRangeStart":"Sep 5th","dateRangeEnd":"Sep 5th","averageDuration":"08:00","averageStart":"12:51 am","averageEnd":"08:51 am","userFeelTotals":{"BAD":0,"OK":31,"GOOD":0}}'
    15 = '{"userId":15,"dateRangeStart":"Sep 5th","dateRangeEnd":"Sep 5th","averageDuration":"08:00","averageStart":"10:00 pm","averageEnd":"06:00 am","userFeelTotals":{"BAD":0,"OK":365,"GOOD":0}}'
}

function New-HistoryBody {
    param([int]$UserId, [string]$RangeStart, [int]$OkCount)
    return ('{{"userId":{0},"dateRangeStart":"{1}","dateRangeEnd":"Sep 5th","averageDuration":"08:00","averageStart":"10:00 pm","averageEnd":"06:00 am","userFeelTotals":{{"BAD":0,"OK":{2},"GOOD":0}}}}' -f $UserId, $RangeStart, $OkCount)
}

$tests = @()
foreach ($userId in 5..15) {
    $summary = $scenarioNames[$userId]
    if ($userId -eq 5) {
        $tests += @{ Name = "User $userId - $summary - last night"; UserId = $userId; Path = "/sleep-logs"; ExpectedStatus = 404; ExpectedBody = $notFoundLog }
        $tests += @{ Name = "User $userId - $summary - 1-day history"; UserId = $userId; Path = "/sleep-logs/history?historyDays=1"; ExpectedStatus = 404; ExpectedBody = $notFoundHistory }
        $tests += @{ Name = "User $userId - $summary - 7-day history"; UserId = $userId; Path = "/sleep-logs/history?historyDays=7"; ExpectedStatus = 404; ExpectedBody = $notFoundHistory }
        $tests += @{ Name = "User $userId - $summary - default 30-day history"; UserId = $userId; Path = "/sleep-logs/history"; ExpectedStatus = 404; ExpectedBody = $notFoundHistory }
        $tests += @{ Name = "User $userId - $summary - 365-day history"; UserId = $userId; Path = "/sleep-logs/history?historyDays=365"; ExpectedStatus = 404; ExpectedBody = $notFoundHistory }
        continue
    }

    $lastNightId = 219 + $userId
    if ($userId -eq 15) { $lastNightId = 594 }
    if ($userId -eq 15) { $lastNightId = 598 }
    $lastNightBody = ('{{"id":{0},"targetDate":"September 4th","sleepDuration":"08:00","startSleep":"10:00 pm","endSleep":"06:00 am","userFeel":"OK"}}' -f $lastNightId)
    $tests += @{ Name = "User $userId - $summary - last night"; UserId = $userId; Path = "/sleep-logs"; ExpectedStatus = 200; ExpectedBody = $lastNightBody }
    $tests += @{ Name = "User $userId - $summary - 1-day history"; UserId = $userId; Path = "/sleep-logs/history?historyDays=1"; ExpectedStatus = 200; ExpectedBody = (New-HistoryBody $userId "Sep 4th" 1) }
    $recentHistoryCount = if ($userId -eq 15) { 7 } else { 1 }
    $defaultHistoryCount = if ($userId -eq 15) { 30 } else { 1 }
    $tests += @{ Name = "User $userId - $summary - 7-day history"; UserId = $userId; Path = "/sleep-logs/history?historyDays=7"; ExpectedStatus = 200; ExpectedBody = (New-HistoryBody $userId "Aug 29th" $recentHistoryCount) }
    $tests += @{ Name = "User $userId - $summary - default 30-day history"; UserId = $userId; Path = "/sleep-logs/history"; ExpectedStatus = 200; ExpectedBody = (New-HistoryBody $userId "Aug 6th" $defaultHistoryCount) }
    $tests += @{ Name = "User $userId - $summary - 365-day history"; UserId = $userId; Path = "/sleep-logs/history?historyDays=365"; ExpectedStatus = 200; ExpectedBody = $annualHistory[$userId] }
}

$passed = 0
$failed = 0
Write-Host "Running $($tests.Count) API matrix tests against $BaseUrl" -ForegroundColor Cyan

foreach ($test in $tests) {
    if (Invoke-MatrixRequest @test) { $passed++ } else { $failed++ }
}

Write-Host "`nSummary: $passed passed, $failed failed, $($tests.Count) total." -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
if ($failed -gt 0) { exit 1 }
