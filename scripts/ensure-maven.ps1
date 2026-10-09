$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$version = '3.9.11'
$toolRoot = Join-Path $root '.tools'
$mavenHome = Join-Path $toolRoot "apache-maven-$version"
$mavenCmd = Join-Path $mavenHome 'bin\mvn.cmd'

if (Test-Path $mavenCmd) { Write-Output $mavenCmd; exit 0 }

New-Item -ItemType Directory -Force -Path $toolRoot | Out-Null
$zip = Join-Path $toolRoot "apache-maven-$version-bin.zip"
$url = "https://archive.apache.org/dist/maven/maven-3/$version/binaries/apache-maven-$version-bin.zip"
Write-Host "Maven was not found. Downloading portable Maven $version once..."
Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing
Expand-Archive -Path $zip -DestinationPath $toolRoot -Force
Remove-Item $zip -Force
if (!(Test-Path $mavenCmd)) { throw 'Portable Maven setup failed.' }
Write-Output $mavenCmd
