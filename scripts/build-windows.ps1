Param(
  [string]$AppVersion = "1.0.0"
)

$ErrorActionPreference = "Stop"

$AppName = "BlackJack"
$MainClass = "com.example.blackjack.Main"
$JarName = "blackjack-desktop-$AppVersion.jar"

# Предполагается, что установлены: JDK 17 + jpackage, Maven

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path | Split-Path -Parent
Push-Location $Root

Write-Host "Building jar..."
mvn -q -DskipTests package

New-Item -ItemType Directory -Path dist -Force | Out-Null

Write-Host "Packaging with jpackage..."
& jpackage `
  --type exe `
  --name $AppName `
  --app-version $AppVersion `
  --dest dist `
  --vendor "Example" `
  --input target `
  --main-jar $JarName `
  --main-class $MainClass `
  --add-modules java.base,java.desktop

Write-Host "Done. Check dist/ for installer." 

Pop-Location
