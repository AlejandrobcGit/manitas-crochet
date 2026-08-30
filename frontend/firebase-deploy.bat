@echo off
cd /d "%~dp0"

echo === Instalando/compilando build ===
call npm run build
if %errorlevel% neq 0 (
    echo [ERROR] Build fallido, abortando deploy.
    pause
    exit /b %errorlevel%
)

echo === Desplegando a Firebase Hosting ===
call firebase deploy --only hosting
if %errorlevel% neq 0 (
    echo [ERROR] Deploy fallido.
    pause
    exit /b %errorlevel%
)

echo === Deploy completado con exito ===
pause