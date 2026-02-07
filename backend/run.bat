@echo off
chcp 65001 >nul
cls
echo ========================================
echo   MARKETPLACE DE SERVICOS - BACKEND
echo ========================================
echo.

where mvn >nul 2>nul
if errorlevel 1 (
    echo [ERRO] Maven nao encontrado!
    echo.
    echo Instale o Maven primeiro:
    echo   1. Execute: ..\baixar-maven.bat
    echo   2. Ou veja: ..\INSTALAR-MAVEN-MANUAL.md
    echo   3. Ou use Docker: docker-compose up
    echo.
    pause
    exit /b 1
)

echo [OK] Maven encontrado
echo.

where java >nul 2>nul
if errorlevel 1 (
    echo [ERRO] Java nao encontrado!
    echo Instale Java 21 LTS
    echo.
    pause
    exit /b 1
)

echo [OK] Java encontrado
java -version
echo.
echo Iniciando backend...
echo.

mvn spring-boot:run
