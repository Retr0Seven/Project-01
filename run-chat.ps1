param(
    [int]$Clients = 2,
    [switch]$ReadOnlyClient,
    [switch]$Build,
    [string]$HostName = "localhost",
    [int]$Port = 0
)

$ErrorActionPreference = "Stop"

$Root = $PSScriptRoot
$ServerJar = Join-Path $Root "TCPServer\target\TCPServer.jar"
$ClientJar = Join-Path $Root "TCPClient\target\TCPClient.jar"
$ServerPom = Join-Path $Root "TCPServer\pom.xml"
$ClientPom = Join-Path $Root "TCPClient\pom.xml"
$MavenWrapper = Join-Path $Root "mvnw.cmd"
$ServerProperties = Join-Path $Root "TCPServer\server.properties"

function Get-ConfiguredPort {
    if ($Port -gt 0) {
        return $Port
    }

    if (Test-Path -LiteralPath $ServerProperties) {
        $PortLine = Get-Content -LiteralPath $ServerProperties |
            Where-Object { $_ -match '^\s*server\.port\s*=' } |
            Select-Object -First 1

        if ($PortLine -and $PortLine.Split("=", 2)[1].Trim() -match '^\d+$') {
            return [int]$PortLine.Split("=", 2)[1].Trim()
        }
    }

    return 3000
}

function Invoke-Build {
    Write-Host "Building TCPServer..."
    & $MavenWrapper -f $ServerPom clean package
    if ($LASTEXITCODE -ne 0) {
        throw "TCPServer build failed."
    }

    Write-Host "Building TCPClient..."
    & $MavenWrapper -f $ClientPom clean package
    if ($LASTEXITCODE -ne 0) {
        throw "TCPClient build failed."
    }
}

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw "Java is not available on PATH."
}

if ($Build -or -not (Test-Path -LiteralPath $ServerJar) -or -not (Test-Path -LiteralPath $ClientJar)) {
    Invoke-Build
}

$ResolvedPort = Get-ConfiguredPort

Write-Host "Starting TCPServer..."
$ServerProcess = Start-Process -FilePath "java" `
    -ArgumentList @("-jar", $ServerJar) `
    -WorkingDirectory $Root `
    -WindowStyle Normal `
    -PassThru

Start-Sleep -Seconds 3

if ($ServerProcess.HasExited) {
    throw "TCPServer exited early with code $($ServerProcess.ExitCode). Check whether port $ResolvedPort is already in use."
}

Write-Host "TCPServer PID: $($ServerProcess.Id)"

for ($Index = 1; $Index -le $Clients; $Index++) {
    Write-Host "Starting TCPClient $Index..."
    $ClientProcess = Start-Process -FilePath "java" `
        -ArgumentList @("-jar", $ClientJar, $HostName, "$ResolvedPort") `
        -WorkingDirectory $Root `
        -WindowStyle Normal `
        -PassThru

    Start-Sleep -Milliseconds 650

    if ($ClientProcess.HasExited) {
        Write-Warning "TCPClient $Index exited early with code $($ClientProcess.ExitCode)."
    } else {
        Write-Host "TCPClient $Index PID: $($ClientProcess.Id)"
    }
}

if ($ReadOnlyClient) {
    Write-Host "Starting read-only test client. Leave username blank and click Connect."
    $ReadOnlyProcess = Start-Process -FilePath "java" `
        -ArgumentList @("-jar", $ClientJar, $HostName, "$ResolvedPort") `
        -WorkingDirectory $Root `
        -WindowStyle Normal `
        -PassThru

    Start-Sleep -Milliseconds 650

    if ($ReadOnlyProcess.HasExited) {
        Write-Warning "Read-only test client exited early with code $($ReadOnlyProcess.ExitCode)."
    } else {
        Write-Host "Read-only test client PID: $($ReadOnlyProcess.Id)"
    }
}

Write-Host ""
Write-Host "Ready. Connect clients with usernames, or leave one blank for read-only mode."
Write-Host "Use allUsers to list active users, and bye or end to disconnect."
