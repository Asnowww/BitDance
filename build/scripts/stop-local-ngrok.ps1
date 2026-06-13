$ErrorActionPreference = 'SilentlyContinue'

$BuildRoot = Split-Path -Parent $PSScriptRoot
$RunDir = Join-Path $BuildRoot '.local\run'
$ids = @()

if (Test-Path -LiteralPath $RunDir) {
    Get-ChildItem -LiteralPath $RunDir -Filter '*.pid' | ForEach-Object {
        $value = Get-Content -LiteralPath $_.FullName
        if ($value -match '^\d+$') {
            $ids += [int]$value
        }
    }
}

Get-NetTCPConnection -LocalPort 8080,5173,4040 -State Listen | ForEach-Object {
    $ids += $_.OwningProcess
}

$ids | Sort-Object -Unique | ForEach-Object {
    Stop-Process -Id $_ -Force
}

Write-Host 'Stopped local BitDance backend/frontend/ngrok processes.'
