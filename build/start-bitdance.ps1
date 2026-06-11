[CmdletBinding()]
param(
    [string]$DbHost = "98.142.241.155",
    [int]$DbPort = 5432,
    [string]$DbName = "bitdance",
    [string]$DbUser = "BitDance",
    [string]$DbSchema = "bitdance",
    [int]$BackendPort = 8080,
    [int]$FrontendPort = 5173,
    [switch]$SkipBackendBuild,
    [switch]$OpenBrowser
)

$ErrorActionPreference = "Stop"
$buildDir = $PSScriptRoot
$backendDir = Join-Path $buildDir "backend"
$frontendDir = Join-Path $buildDir "frontend"
$backendJar = Join-Path $backendDir "target\bitdance-backend.jar"
$backendHealth = "http://127.0.0.1:$BackendPort/api/actuator/health"
$frontendUrl = "http://127.0.0.1:$FrontendPort/"

function Test-PortListening([int]$Port) {
    return [bool](Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
}

function Wait-Http([string]$Url, [int]$Attempts = 30) {
    for ($i = 1; $i -le $Attempts; $i++) {
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 3
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 400) {
                return $true
            }
        } catch {
            Start-Sleep -Seconds 2
        }
    }
    return $false
}

function Get-DatabasePassword {
    if ($env:SPRING_DATASOURCE_PASSWORD) {
        return $env:SPRING_DATASOURCE_PASSWORD
    }

    $secure = Read-Host "PostgreSQL password for $DbUser@$DbHost" -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    }
}

Write-Host "Starting BitDance..." -ForegroundColor Cyan

if (-not (Test-Path $backendDir) -or -not (Test-Path $frontendDir)) {
    throw "Run this script from the BitDance build directory. backend/ or frontend/ is missing."
}

if (-not (Test-PortListening $BackendPort)) {
    if (-not $SkipBackendBuild -or -not (Test-Path $backendJar)) {
        Write-Host "Building backend..." -ForegroundColor Yellow
        Push-Location $backendDir
        try {
            & mvn.cmd -q -DskipTests package
            if ($LASTEXITCODE -ne 0) {
                throw "Backend build failed."
            }
        } finally {
            Pop-Location
        }
    }

    $env:SPRING_DATASOURCE_URL = "jdbc:postgresql://${DbHost}:${DbPort}/${DbName}?currentSchema=${DbSchema}"
    $env:SPRING_DATASOURCE_USERNAME = $DbUser
    $env:SPRING_DATASOURCE_PASSWORD = Get-DatabasePassword
    $env:SERVER_PORT = "$BackendPort"
    $env:BITDANCE_CACHE_TYPE = "simple"

    $backendOut = Join-Path $backendDir "bitdance-backend.out.log"
    $backendErr = Join-Path $backendDir "bitdance-backend.err.log"
    $backendProcess = Start-Process `
        -FilePath "java" `
        -ArgumentList @("-jar", "`"$backendJar`"") `
        -WorkingDirectory $backendDir `
        -RedirectStandardOutput $backendOut `
        -RedirectStandardError $backendErr `
        -PassThru `
        -WindowStyle Hidden
    Set-Content -LiteralPath (Join-Path $backendDir "bitdance-backend.pid") -Value $backendProcess.Id -Encoding ASCII

    Write-Host "Waiting for backend..." -ForegroundColor Yellow
    if (-not (Wait-Http $backendHealth 35)) {
        throw "Backend did not become healthy. Check $backendOut and $backendErr"
    }
    Write-Host "Backend ready: $backendHealth" -ForegroundColor Green
} elseif (Wait-Http $backendHealth 1) {
    Write-Host "Backend already running: $backendHealth" -ForegroundColor Green
} else {
    throw "Port $BackendPort is occupied, but BitDance backend health check failed."
}

if (-not (Test-PortListening $FrontendPort)) {
    if (-not (Test-Path (Join-Path $frontendDir "node_modules"))) {
        Write-Host "Installing frontend dependencies..." -ForegroundColor Yellow
        Push-Location $frontendDir
        try {
            & npm.cmd install
            if ($LASTEXITCODE -ne 0) {
                throw "Frontend dependency installation failed."
            }
        } finally {
            Pop-Location
        }
    }

    $frontendOut = Join-Path $frontendDir "bitdance-frontend.out.log"
    $frontendErr = Join-Path $frontendDir "bitdance-frontend.err.log"
    $frontendProcess = Start-Process `
        -FilePath "npm.cmd" `
        -ArgumentList @("run", "dev", "--", "--host", "127.0.0.1", "--port", "$FrontendPort") `
        -WorkingDirectory $frontendDir `
        -RedirectStandardOutput $frontendOut `
        -RedirectStandardError $frontendErr `
        -PassThru `
        -WindowStyle Hidden
    Set-Content -LiteralPath (Join-Path $frontendDir "bitdance-frontend.pid") -Value $frontendProcess.Id -Encoding ASCII

    Write-Host "Waiting for frontend..." -ForegroundColor Yellow
    if (-not (Wait-Http $frontendUrl 20)) {
        throw "Frontend did not become ready. Check $frontendOut and $frontendErr"
    }
    Write-Host "Frontend ready: $frontendUrl" -ForegroundColor Green
} elseif (Wait-Http $frontendUrl 1) {
    Write-Host "Frontend already running: $frontendUrl" -ForegroundColor Green
} else {
    throw "Port $FrontendPort is occupied, but BitDance frontend check failed."
}

Write-Host ""
Write-Host "BitDance is ready." -ForegroundColor Cyan
Write-Host "Frontend: $frontendUrl"
Write-Host "Backend:  http://127.0.0.1:$BackendPort/api"
Write-Host "Swagger:  http://127.0.0.1:$BackendPort/api/swagger-ui.html"
Write-Host "Test password / SMS code: 123456"

if ($OpenBrowser) {
    Start-Process "$frontendUrl#/login"
}
