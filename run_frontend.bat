@echo off
echo ==========================================
echo Starting SafeReach Web Application...
echo ==========================================
cd frontend
echo Installing dependencies (if needed)...
call npm install
echo Starting development server...
call npm run dev
pause
