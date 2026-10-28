@echo off
set "PATH=%SystemRoot%\System32;%SystemRoot%;%SystemRoot%\System32\WindowsPowerShell\v1.0;%PATH%"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0Configurar.ps1"
echo.
pause