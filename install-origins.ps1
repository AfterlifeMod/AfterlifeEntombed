# Install Origins dependencies for development
$modsDir = "run/mods"
if (!(Test-Path $modsDir)) {
    New-Item -ItemType Directory -Path $modsDir
}

Write-Host "Downloading Origins (Forge)..." -ForegroundColor Cyan
Invoke-WebRequest -Uri "https://cdn.modrinth.com/data/3BeIrqZR/versions/DerkC4wX/origins-forge-1.10.0.5-forge.jar" -OutFile "$modsDir/origins-forge-1.10.0.5-forge.jar"

Write-Host "Downloading Apoli (Forge)..." -ForegroundColor Cyan
Invoke-WebRequest -Uri "https://cdn.modrinth.com/data/DjLobEOy/versions/jukMOBPr/apoli-forge-1.20.1-2.9.0.5.jar" -OutFile "$modsDir/apoli-forge-1.20.1-2.9.0.5.jar"

Write-Host "Downloading Calio (Forge)..." -ForegroundColor Cyan
Invoke-WebRequest -Uri "https://cdn.modrinth.com/data/oln932m4/versions/T46LT7O0/calio-forge-1.20.1-1.11.0.1.jar" -OutFile "$modsDir/calio-forge-1.20.1-1.11.0.1.jar"

Write-Host "Downloading Caelus (Forge)..." -ForegroundColor Cyan
Invoke-WebRequest -Uri "https://cdn.modrinth.com/data/CAKagPlH/versions/lFLu37mN/caelus-forge-3.2.0%2B1.20.1.jar" -OutFile "$modsDir/caelus-forge-3.2.0+1.20.1.jar"

Write-Host "Origins dependencies installed successfully!" -ForegroundColor Green
Write-Host "You can now run './gradlew runClient'" -ForegroundColor Yellow
