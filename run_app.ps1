# CONFIGURATION
$javafxPath = "C:\Users\nezihat\javafx\javafx-sdk-21.0.9\lib"
$mysqlJar = "libs/mysql-connector-j-8.2.0.jar"

# MODULES
$modules = "javafx.controls,javafx.fxml,javafx.graphics,javafx.base,java.sql"
$src = "src\main\java"
$resources = "src\main\resources"

Write-Host "Compiling..."
if (!(Test-Path "bin")) { New-Item -ItemType Directory -Force -Path "bin" | Out-Null }

# Check dependencies
if (!(Test-Path $javafxPath)) {
    Write-Warning "JavaFX SDK not found at $javafxPath. Please edit run_app.ps1 and set `$javafxPath to your JavaFX 'lib' folder."
    # Try to find it in libs if user put it there?
    if (Test-Path "libs/javafx.controls.jar") {
        $javafxPath = "libs"
        Write-Host "Found JavaFX in libs folder."
    }
}

javac -d bin --module-path $javafxPath --add-modules $modules -cp $mysqlJar (Get-ChildItem -Recurse $src -Filter *.java).FullName

if ($LASTEXITCODE -eq 0) {
    Write-Host "Running..."
    # Copy resources to bin
    Copy-Item "src\main\resources\*" -Destination "bin" -Recurse -Force
    
    java --module-path $javafxPath --add-modules $modules -cp "bin;$mysqlJar" com.group18.greengrocer.main.Main
} else {
    Write-Error "Compilation failed."
}
