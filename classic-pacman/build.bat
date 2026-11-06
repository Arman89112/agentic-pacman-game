@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
echo ==> Compiling Classic Pacman...
javac --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.fxml -d out src\com\pacman\*.java
echo Build OK -> out
pause
