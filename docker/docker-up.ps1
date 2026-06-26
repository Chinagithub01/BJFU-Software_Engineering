# Windows deploy helper - sync to ASCII path then docker compose up
$ErrorActionPreference = "Stop"

$SourceDir = Split-Path -Parent $PSScriptRoot
$DeployDir = "C:\peerreview-docker"

Write-Host ">> Source: $SourceDir"
Write-Host ">> Deploy: $DeployDir"

New-Item -ItemType Directory -Force -Path $DeployDir | Out-Null
robocopy $SourceDir $DeployDir /MIR /NFL /NDL /NJH /NJS /nc /ns /np /XD target out .idea node_modules dist | Out-Null
if ($LASTEXITCODE -ge 8) {
    throw "robocopy failed with exit code $LASTEXITCODE"
}

Set-Location $DeployDir
$env:COMPOSE_BAKE = "false"

Write-Host ">> Building and starting containers (first run may take 5-15 min)..."
docker compose up -d --build
if ($LASTEXITCODE -ne 0) {
    Write-Host ">> BUILD FAILED - see errors above" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host ">> Container status:"
docker compose ps -a

Write-Host ""
Write-Host ">> Database: PostgreSQL (peerreview)"
Write-Host ">> Storage: MinIO (Console http://localhost:9001)"
Write-Host ">> First migration from SQL Server: docker compose down -v"
Write-Host ">> Open: http://localhost:8080/login"
Write-Host ">> Login: teacher01 / 123456"
Write-Host ">> Logs:  cd C:\peerreview-docker"
Write-Host ">>        docker compose logs -f"
