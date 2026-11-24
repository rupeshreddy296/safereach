@echo off
title SafeReach Backend Server
echo ===================================================
echo       Starting SafeReach Backend & Database        
echo ===================================================

:: 1. Start MySQL if not running
netstat -ano | findstr :3306 >nul
if %ERRORLEVEL% neq 0 (
    echo [*] Starting MySQL Database Server...
    if exist "C:\xampp\htdocs\mysql_start.bat" (
        start "" /min "C:\xampp\htdocs\mysql_start.bat"
    )
) else (
    echo [v] MySQL is already active on port 3306.
)

:: 2. Start Apache if not running
netstat -ano | findstr :80 >nul
if %ERRORLEVEL% neq 0 (
    echo [*] Starting Apache Web Server...
    if exist "C:\xampp\htdocs\apache_start.bat" (
        start "" /min "C:\xampp\htdocs\apache_start.bat"
    )
) else (
    echo [v] Apache is already active on port 80.
)

echo.
echo ===================================================
echo   Backend APIs are ready and accessible at:
echo   http://localhost/safereach/api/
echo ===================================================
echo.
pause

