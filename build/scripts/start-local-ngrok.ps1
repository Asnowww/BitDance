param(
    [string]$Domain = $env:NGROK_DOMAIN,
    [string]$NgrokPath = $env:NGROK_PATH,
    [switch]$SkipBackend,
    [switch]$SkipFrontend,
    [switch]$SkipNgrok
)

$ErrorActionPreference = 'Stop'

$BuildRoot = Split-Path -Parent $PSScriptRoot
$BackendDir = Join-Path $BuildRoot 'backend'
$FrontendDir = Join-Path $BuildRoot 'frontend'
$RunDir = Join-Path $BuildRoot '.local\run'
$EnvFile = Join-Path $BuildRoot '.env.local'

function Import-DotEnv {
    param([string]$Path)
    if (-not (Test-Path -LiteralPath $Path)) {
        return
    }
    Get-Content -LiteralPath $Path | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith('#') -or -not $line.Contains('=')) {
            return
        }
        $parts = $line.Split('=', 2)
        $name = $parts[0].Trim()
        $value = $parts[1].Trim().Trim('"').Trim("'")
        if ($name) {
            Set-Item -Path "Env:$name" -Value $value
        }
    }
}

function Set-DefaultEnv {
    param(
        [string]$Name,
        [string]$Value
    )
    if (-not (Get-Item -Path "Env:$Name" -ErrorAction SilentlyContinue)) {
        Set-Item -Path "Env:$Name" -Value $Value
    }
}

function Start-LoggedProcess {
    param(
        [string]$Name,
        [string]$FilePath,
        [string]$WorkingDirectory,
        [string[]]$ArgumentList
    )
    $stdout = Join-Path $RunDir "$Name.out"
    $stderr = Join-Path $RunDir "$Name.err"
    $process = Start-Process `
        -FilePath $FilePath `
        -ArgumentList $ArgumentList `
        -WorkingDirectory $WorkingDirectory `
        -RedirectStandardOutput $stdout `
        -RedirectStandardError $stderr `
        -WindowStyle Hidden `
        -PassThru
    Set-Content -LiteralPath (Join-Path $RunDir "$Name.pid") -Value $process.Id
    Write-Host "$Name started: pid=$($process.Id), logs=$stdout / $stderr"
}

Import-DotEnv $EnvFile

if (-not $Domain -and $env:NGROK_DOMAIN) {
    $Domain = $env:NGROK_DOMAIN
}

if (-not $Domain) {
    $Domain = 'tapioca-roman-dole.ngrok-free.dev'
}

New-Item -ItemType Directory -Force -Path $RunDir | Out-Null

Set-DefaultEnv 'JAVA_HOME' 'D:\DevTools\JetBrains\apps\PyCharm\jbr'
Set-DefaultEnv 'MAVEN_HOME' 'D:\DevTools\apache-maven-3.9.9'
Set-DefaultEnv 'SPRING_DATASOURCE_URL' 'jdbc:postgresql://98.142.241.155:5432/bitdance?currentSchema=bitdance'
Set-DefaultEnv 'SPRING_DATASOURCE_USERNAME' 'BitDance'
Set-DefaultEnv 'SPRING_DATASOURCE_PASSWORD' 'shixun123'
Set-DefaultEnv 'SPRING_JPA_HIBERNATE_DDL_AUTO' 'none'
Set-DefaultEnv 'MANAGEMENT_HEALTH_REDIS_ENABLED' 'false'
Set-DefaultEnv 'BITDANCE_CACHE_TYPE' 'simple'
Set-DefaultEnv 'BITDANCE_SMS_MOCK' 'false'
Set-DefaultEnv 'BITDANCE_SMS_PROVIDER' 'aliyun-pnvs'
Set-DefaultEnv 'BITDANCE_SMS_STORAGE' 'memory'
Set-DefaultEnv 'BITDANCE_WECHAT_REDIRECT_URI' "https://$Domain/api/auth/wechat/callback"
Set-DefaultEnv 'BITDANCE_WECHAT_FRONTEND_CALLBACK_URI' "https://$Domain/#/login"
Set-DefaultEnv 'BITDANCE_WECHAT_AUTO_CREATE_USER' 'false'
Set-DefaultEnv 'VITE_API_BASE' '/api'

$pathParts = @()
if ($env:JAVA_HOME) {
    $javaBin = Join-Path $env:JAVA_HOME 'bin'
    if (Test-Path -LiteralPath $javaBin) {
        $pathParts += $javaBin
    }
}
if ($env:MAVEN_HOME) {
    $mavenBin = Join-Path $env:MAVEN_HOME 'bin'
    if (Test-Path -LiteralPath $mavenBin) {
        $pathParts += $mavenBin
    }
}
if ($pathParts.Count -gt 0) {
    $env:PATH = ($pathParts -join ';') + ';' + $env:PATH
}

$MvnCommand = 'mvn'
if ($env:MAVEN_HOME) {
    $mvnCandidate = Join-Path $env:MAVEN_HOME 'bin\mvn.cmd'
    if (Test-Path -LiteralPath $mvnCandidate) {
        $MvnCommand = $mvnCandidate
    }
}

if (-not $SkipBackend) {
    Start-LoggedProcess `
        -Name 'backend' `
        -FilePath $MvnCommand `
        -WorkingDirectory $BackendDir `
        -ArgumentList @('spring-boot:run')
}

if (-not $SkipFrontend) {
    Start-LoggedProcess `
        -Name 'frontend' `
        -FilePath 'npm' `
        -WorkingDirectory $FrontendDir `
        -ArgumentList @('run', 'dev', '--', '--host', '0.0.0.0', '--port', '5173')
}

if (-not $SkipNgrok) {
    if (-not $NgrokPath) {
        $command = Get-Command ngrok -ErrorAction SilentlyContinue
        if ($command) {
            $NgrokPath = $command.Source
        } else {
            $NgrokPath = Join-Path $BuildRoot '.local\ngrok\ngrok.exe'
        }
    }

    if (-not (Test-Path -LiteralPath $NgrokPath)) {
        throw "ngrok executable not found: $NgrokPath"
    }

    if ($env:NGROK_AUTHTOKEN) {
        & $NgrokPath config add-authtoken $env:NGROK_AUTHTOKEN | Out-Null
    }

    $proxyEnvNames = @('HTTP_PROXY', 'HTTPS_PROXY', 'ALL_PROXY', 'http_proxy', 'https_proxy', 'all_proxy')
    foreach ($proxyEnvName in $proxyEnvNames) {
        Remove-Item -Path "Env:$proxyEnvName" -ErrorAction SilentlyContinue
    }

    Start-LoggedProcess `
        -Name 'ngrok' `
        -FilePath $NgrokPath `
        -WorkingDirectory $BuildRoot `
        -ArgumentList @('http', '--url', $Domain, '5173')
}

Write-Host "Public URL: https://$Domain"
Write-Host "Wechat redirect URI: https://$Domain/api/auth/wechat/callback"
