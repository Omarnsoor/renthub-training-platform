$startup = [Environment]::GetFolderPath('Startup')
$launcher = Join-Path $startup 'RentHub-Local.cmd'
if(Test-Path $launcher){Remove-Item $launcher -Force}
Write-Host 'RentHub Windows autostart removed.'
