@echo off
echo ========================================
echo   MARKETPLACE - INSTALAR FERRAMENTAS
echo ========================================
echo.
echo Este script vai instalar:
echo   - Apache Maven
echo   - (Java 21 ja deve estar instalado)
echo.
pause

echo.
echo Instalando Apache Maven...
echo.
winget install Apache.Maven -e --accept-source-agreements --accept-package-agreements

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [OK] Maven instalado com sucesso!
    echo.
    echo IMPORTANTE: Feche e abra um novo terminal CMD para usar o Maven.
    echo.
) else (
    echo.
    echo [ERRO] Falha ao instalar Maven.
    echo Por favor, instale manualmente:
    echo https://maven.apache.org/download.cgi
    echo.
)

pause
