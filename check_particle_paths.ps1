# Script to check for absolute paths in Effekseer particle files
# This helps identify which .efkefc files have problematic absolute file paths

$effeksDir = "src\main\resources\assets\afterlifeentombed\effeks"
$files = Get-ChildItem -Path $effeksDir -Filter "*.efkefc" -Recurse

Write-Host "Checking Effekseer particle files for absolute paths..." -ForegroundColor Cyan
Write-Host ""

$problematicFiles = @()

foreach ($file in $files) {
    $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
    
    # Check for absolute Windows paths (C:\ or users paths)
    if ($content -match "C:\\|Users\\dtmin|Downloads" -or $content -match "\.\./\.\./\.\./\.\./\.\./\.\./") {
        Write-Host "❌ PROBLEM: $($file.Name)" -ForegroundColor Red
        Write-Host "   Location: $($file.DirectoryName)" -ForegroundColor Gray
        
        # Try to extract the problematic path
        if ($content -match "(?:C:\\|Users\\|Downloads\\)[^`"<>|]*") {
            $matchedPath = $matches[0]
            Write-Host "   Contains: $matchedPath" -ForegroundColor Yellow
        } elseif ($content -match "(\.\./){5,}[^`"]*") {
            $matchedPath = $matches[0]
            Write-Host "   Contains: $matchedPath" -ForegroundColor Yellow
        }
        
        $problematicFiles += $file.Name
        Write-Host ""
    } else {
        Write-Host "✅ OK: $($file.Name)" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Summary:" -ForegroundColor White
Write-Host "Total files checked: $($files.Count)" -ForegroundColor White
Write-Host "Problematic files: $($problematicFiles.Count)" -ForegroundColor $(if ($problematicFiles.Count -gt 0) { "Red" } else { "Green" })

if ($problematicFiles.Count -gt 0) {
    Write-Host ""
    Write-Host "Files that need fixing:" -ForegroundColor Yellow
    foreach ($f in $problematicFiles) {
        Write-Host "  - $f" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "SOLUTION:" -ForegroundColor Cyan
    Write-Host "1. Open each problematic .efkefc file in Effekseer editor" -ForegroundColor White
    Write-Host "2. Ensure all texture files are in a 'Texture' subfolder relative to the .efkefc file" -ForegroundColor White
    Write-Host "3. Update texture paths to be relative (e.g., 'Texture/my_image.png')" -ForegroundColor White
    Write-Host "4. Re-save the particle effect" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "All particle files look good! ✅" -ForegroundColor Green
}
