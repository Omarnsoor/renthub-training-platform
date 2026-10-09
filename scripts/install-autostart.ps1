$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$startup = [Environment]::GetFolderPath('Startup')
$launcher = Join-Path $startup 'RentHub-Local.cmd'
$script = Join-Path $root 'scripts\start-renthub.ps1'
$content = '@echo off' + "`r`n" + 'start "" powershell.exe -NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File "' + $script + '" -NoBrowser' + "`r`n"
Set-Content -Path $launcher -Value $content -Encoding ASCII
Write-Host 'RentHub will now start automatically when you sign in to Windows.'
Write-Host "Startup entry: $launcher"
