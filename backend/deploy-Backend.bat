@echo off
echo ==========================
echo Desplegando manitas-backend
echo ==========================

docker stop manitas-backend >nul 2>&1
docker rm manitas-backend >nul 2>&1

docker build -t manitas-backend .
if errorlevel 1 (
    echo ERROR: Fallo al construir la imagen.
    pause
    exit /b 1
)

docker run -d --name manitas-backend --env-file .env -p 8080:8080 manitas-backend
if errorlevel 1 (
    echo ERROR: Fallo al arrancar el contenedor.
    pause
    exit /b 1
)

echo.
echo Backend desplegado correctamente.
docker ps
