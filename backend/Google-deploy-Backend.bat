@echo off
setlocal

echo ==================================================
echo ARIBA CROCHET - DEPLOY BACKEND - GOOGLE CLOUD RUN
echo ==================================================

set PROJECT_ID=arribacrochet
set REGION=europe-southwest1
set REPOSITORY=backend
set IMAGE=backend
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
gcloud run deploy backend ^
--image=%REGION%-docker.pkg.dev/%PROJECT_ID%/%REPOSITORY%/%IMAGE%:%VERSION% ^
--region=%REGION% ^
--platform=managed ^
--allow-unauthenticated ^
--memory=512Mi ^
--cpu=1 ^
--min-instances=0 ^
--max-instances=3 ^
--env-vars-file=env.yaml ^
--set-secrets=MONGODB_URI=mongodb-uri:latest,EMAIL_PASSWORD=EMAIL_PASSWORD:latest,JWT_SECRET=JWT_SECRET:latest,IMAGEKIT_PUBLIC_KEY=IMAGEKIT_PUBLIC_KEY:latest,IMAGEKIT_PRIVATE_KEY=IMAGEKIT_PRIVATE_KEY:latest

if errorlevel 1 (
    echo ERROR desplegando Cloud Run
    pause
    exit /b 1
)

echo.
echo ======================================
echo DESPLIEGUE COMPLETADO
echo ======================================

pause