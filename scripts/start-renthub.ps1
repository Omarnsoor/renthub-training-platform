param([switch]$NoBrowser)
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$runtime = Join-Path $root '.runtime'
New-Item -ItemType Directory -Force -Path $runtime | Out-Null

function Test-Port([int]$Port) {
  try { return [bool](Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction Stop) } catch { return $false }
}

# Cache real demo photography on first run. Failures are harmless because the UI has online fallbacks.
try { & (Join-Path $PSScriptRoot 'download-media.ps1') } catch { Write-Warning $_.Exception.Message }

# Local DB configuration. Password is stored DPAPI-encrypted for this Windows user only.
$configPath = Join-Path $root '.renthub-local.json'
$secretPath = Join-Path $root '.renthub-db-secret'
if (!(Test-Path $configPath)) {
  @{ dbUrl='jdbc:oracle:thin:@localhost:1521/XEPDB1'; dbUser='RENTHUB' } | ConvertTo-Json | Set-Content $configPath -Encoding UTF8
}
$config = Get-Content $configPath -Raw | ConvertFrom-Json
if (!(Test-Path $secretPath)) {
  Write-Host 'First local launch: enter the RENTHUB Oracle password. It will be encrypted for your Windows account.'
  $secure = Read-Host 'Database password' -AsSecureString
  $secure | ConvertFrom-SecureString | Set-Content $secretPath -Encoding UTF8
}
$securePassword = Get-Content $secretPath -Raw | ConvertTo-SecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try { $plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }

$env:RENTHUB_DB_URL = $config.dbUrl
$env:RENTHUB_DB_USER = $config.dbUser
$env:RENTHUB_DB_PASSWORD = $plainPassword

if (!(Test-Port 8080)) {
  $maven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
  if (!$maven) { $maven = Get-Command mvn -ErrorAction SilentlyContinue }
  if ($maven) { $mavenPath = $maven.Source }
  else { $mavenPath = (& (Join-Path $PSScriptRoot 'ensure-maven.ps1') | Select-Object -Last 1) }

  Write-Host 'Starting RentHub backend on http://localhost:8080 ...'
  $backend = Start-Process -FilePath $mavenPath -ArgumentList 'spring-boot:run' -WorkingDirectory (Join-Path $root 'backend') -RedirectStandardOutput (Join-Path $runtime 'backend.log') -RedirectStandardError (Join-Path $runtime 'backend-error.log') -PassThru -WindowStyle Hidden
  $backend.Id | Set-Content (Join-Path $runtime 'backend.pid')
} else { Write-Host 'Backend already running on 8080.' }

if (!(Get-Command npm.cmd -ErrorAction SilentlyContinue)) { throw 'Node.js/npm is required. Install Node.js LTS once, then run Start-RentHub.cmd again.' }
if (!(Test-Port 5173)) {
  Write-Host 'Starting RentHub frontend on http://localhost:5173 ...'
  $frontend = Start-Process -FilePath 'npm.cmd' -ArgumentList 'run','dev','--','--host','127.0.0.1' -WorkingDirectory (Join-Path $root 'frontend') -RedirectStandardOutput (Join-Path $runtime 'frontend.log') -RedirectStandardError (Join-Path $runtime 'frontend-error.log') -PassThru -WindowStyle Hidden
  $frontend.Id | Set-Content (Join-Path $runtime 'frontend.pid')
} else { Write-Host 'Frontend already running on 5173.' }

$deadline = (Get-Date).AddSeconds(90)
while ((Get-Date) -lt $deadline -and (!(Test-Port 8080) -or !(Test-Port 5173))) { Start-Sleep -Seconds 2 }

if (!(Test-Port 8080)) { Write-Warning "Backend did not become ready. Check $runtime\backend-error.log" }
if (!(Test-Port 5173)) { Write-Warning "Frontend did not become ready. Check $runtime\frontend-error.log" }
if ((Test-Port 8080) -and (Test-Port 5173)) {
  Write-Host 'RentHub is ready: http://localhost:5173'
  if (!$NoBrowser) { Start-Process 'http://localhost:5173' }
}
