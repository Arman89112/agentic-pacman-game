@echo off
setlocal
cd /d "%~dp0"
if not exist out (
    call build.bat
)
echo ==> Running Agentic Pacman...
java --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -cp out com.pacman.PacmanApp
pause
