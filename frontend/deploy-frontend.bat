@echo off
echo ============================
echo Desplegando Manitas Frontend
echo ============================

echo.
echo [1/4] Deteniendo contenedor...
docker stop manitas-frontend 2>nul

echo.
echo [2/4] Eliminando contenedor...
docker rm manitas-frontend 2>nul

echo.
echo [3/4] Construyendo imagen...
docker build --build-arg VITE_API_URL=http://192.168.0.16:8080 -t manitas-frontend .

if errorlevel 1 (
    echo ERROR: Fallo al construir la imagen.
    pause
    exit /b 1
)

echo.
echo [4/4] Iniciando contenedor...
docker run -d --name manitas-frontend -p 3000:80 manitas-frontend

if errorlevel 1 (
    echo ERROR: Fallo al arrancar el contenedor.
    pause
    exit /b 1
)

echo.
echo ============================
echo Despliegue completado
echo ============================
echo Frontend disponible en:
echo http://192.168.0.16:3000
echo.

docker ps
