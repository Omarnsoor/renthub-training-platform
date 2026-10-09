$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$target = Join-Path $root 'frontend\public\assets\real'
New-Item -ItemType Directory -Force -Path $target | Out-Null

$files = @(
  @{Name='car-camry.jpg'; Source='Toyota Camry XSE AWD (2026) (55213727536).jpg'},
  @{Name='car-bmw-x5.jpg'; Source='BMW G05 IMG 0919.jpg'},
  @{Name='car-mercedes-c200.jpg'; Source='Mercedes-Benz C 200 (W206, 2025) (54740572519).jpg'},
  @{Name='car-kia-sportage.jpg'; Source='Kia Sportage (NQ5) 1758033820001.jpg'},
  @{Name='car-hyundai-tucson.jpg'; Source='Hyundai Tucson (NX4).png'},
  @{Name='car-tesla-model3.jpg'; Source='Tesla Model 3 (Facelift) – f 30082026.jpg'},
  @{Name='car-nissan-sunny.jpg'; Source='Nissan Sunny in Jordan.jpg'},
  @{Name='car-land-cruiser.jpg'; Source='Toyota Land Cruiser J300 3.3 ZX 2024.jpg'},
  @{Name='property-modern-apartment.jpg'; Source='Apartment Modern.jpg'},
  @{Name='property-villa.jpg'; Source='Moderne Villa.jpg'},
  @{Name='property-resort.jpg'; Source='Modern DC Apartment Complex.jpg'},
  @{Name='property-studio.jpg'; Source='Lima Peru city - Modern Apartment - interior.jpg'},
  @{Name='property-penthouse.jpg'; Source='Interior of apartment hotel in Kotka.jpg'},
  @{Name='property-chalet.jpg'; Source='Cozy wooden cabin interior with seating area and bedroom space.jpg'},
  @{Name='property-house.jpg'; Source='Villa moderniste.jpg'},
  @{Name='property-family.jpg'; Source='Modern DC Apartment Complex.jpg'}
)

foreach ($file in $files) {
  $out = Join-Path $target $file.Name
  if (Test-Path $out) { continue }
  $encoded = [uri]::EscapeDataString($file.Source)
  $url = "https://commons.wikimedia.org/wiki/Special:FilePath/$encoded?width=1600"
  Write-Host "Downloading $($file.Name)..."
  try {
    Invoke-WebRequest -Uri $url -OutFile $out -MaximumRedirection 10 -UseBasicParsing
  } catch {
    Write-Warning "Could not cache $($file.Name). The UI will use its online fallback."
    if (Test-Path $out) { Remove-Item $out -Force }
  }
}

Write-Host 'Real-photo cache ready.'
