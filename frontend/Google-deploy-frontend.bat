@echo off
setlocal

echo =====================================================
echo MANITAS CROCHET - DEPLOY FRONTEND - GOOGLE CLOUD RUN
echo =====================================================

set PROJECT_ID=arribacrochet
set REGION=europe-southwest1
set REPOSITORY=frontend
set IMAGE=frontend
set VERSION=latest

echo.
echo [1/4] Construyendo imagen...
docker build -t %IMAGE%:%VERSION% .

if errorlevel 1 (
    echo ERROR construyendo la imagen
    pause
    exit /b 1
)

echo.
echo [2/4] Etiquetando imagen...
docker tag %IMAGE%:%VERSION% %REGION%-docker.pkg.dev/%PROJECT_ID%/%REPOSITORY%/%IMAGE%:%VERSION%

if errorlevel 1 (
    echo ERROR etiquetando la imagen
    pause
    exit /b 1
)

echo.
echo [3/4] Subiendo a Artifact Registry...
docker push %REGION%-docker.pkg.dev/%PROJECT_ID%/%REPOSITORY%/%IMAGE%:%VERSION%

if errorlevel 1 (
    echo ERROR subiendo la imagen
    pause
    exit /b 1
)

echo.
echo [4/4] Desplegando Cloud Run...
gcloud run deploy frontend ^
--image=%REGION%-docker.pkg.dev/%PROJECT_ID%/%REPOSITORY%/%IMAGE%:%VERSION% ^
--region=%REGION% ^
--platform=managed ^
--allow-unauthenticated ^
--memory=256Mi ^
--cpu=1 ^
--min-instances=0 ^
--max-instances=3

if errorlevel 1 (
    echo ERROR desplegando Cloud Run
    pause
    exit /b 1
)

echo.
echo ======================================
echo FRONTEND DESPLEGADO CORRECTAMENTE
echo ======================================

pause