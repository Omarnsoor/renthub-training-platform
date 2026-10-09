$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$runtime = Join-Path $root '.runtime'
foreach($name in @('frontend','backend')){
  $pidFile = Join-Path $runtime "$name.pid"
  if(Test-Path $pidFile){
    $id = Get-Content $pidFile | Select-Object -First 1
    if($id){ cmd /c "taskkill /PID $id /T /F" | Out-Null }
    Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
  }
}
Write-Host 'RentHub local processes stopped.'
