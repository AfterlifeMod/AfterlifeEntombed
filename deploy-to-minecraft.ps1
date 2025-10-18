# Quick deploy script for testing in production Minecraft
# Build the mod and copy it to your Minecraft mods folder

Write-Host "Building mod..." -ForegroundColor Cyan
./gradlew build

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

# Adjust this path to your Minecraft instance
$mcModsFolder = "$env:APPDATA\.minecraft\mods"

# Uncomment and modify if using a different Minecraft launcher
# $mcModsFolder = "C:\Path\To\Your\Minecraft\Instance\mods"

if (!(Test-Path $mcModsFolder)) {
    Write-Host "Minecraft mods folder not found at: $mcModsFolder" -ForegroundColor Yellow
    Write-Host "Please update the path in this script" -ForegroundColor Yellow
    exit 1
}

# Remove old versions
Write-Host "Removing old versions..." -ForegroundColor Cyan
Remove-Item "$mcModsFolder\afterlifeentombed-*.jar" -ErrorAction SilentlyContinue

# Copy new version
Write-Host "Copying new version..." -ForegroundColor Cyan
Copy-Item "build\libs\afterlifeentombed-1.1.7.jar" -Destination $mcModsFolder -Force

Write-Host ""
Write-Host "✓ Mod deployed successfully!" -ForegroundColor Green
Write-Host "Location: $mcModsFolder\afterlifeentombed-1.1.7.jar" -ForegroundColor Gray
Write-Host ""
Write-Host "Make sure you have these mods installed:" -ForegroundColor Yellow
Write-Host "  - Origins (Forge) 1.10.0.5+" -ForegroundColor Gray
Write-Host "  - Apoli (Forge) 1.20.1-2.9.0.5+" -ForegroundColor Gray
Write-Host "  - Calio (Forge) 1.20.1-1.11.0.1+" -ForegroundColor Gray
Write-Host "  - Caelus (Forge) 3.2.0+1.20.1" -ForegroundColor Gray
Write-Host ""
Write-Host "Launch Minecraft to test!" -ForegroundColor Cyan
