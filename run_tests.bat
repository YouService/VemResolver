@echo off
echo ==========================================
echo      Iniciando Testes Automatizados
echo ==========================================
echo.

cd backend

echo Rodando FluxoPrincipalTest...
echo.

call mvn test -Dtest=FluxoPrincipalTest

if %ERRORLEVEL% equ 0 (
    echo.
    echo ==========================================
    echo        TESTES PASSARAM COM SUCESSO!
    echo ==========================================
) else (
    echo.
    echo ==========================================
    echo          ERRO NA EXECUCAO DOS TESTES
    echo ==========================================
)

pause