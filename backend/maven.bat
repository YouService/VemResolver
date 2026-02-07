@echo off
echo Verificando Maven...

where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Maven encontrado no PATH!
    mvn %*
) else (
    echo Maven nao encontrado. Usando Maven Wrapper...
    if exist mvnw.cmd (
        call mvnw.cmd %*
    ) else (
        echo ERRO: Maven nao instalado e mvnw.cmd nao encontrado!
        echo.
        echo Solucoes:
        echo 1. Instale Maven: winget install Apache.Maven
        echo 2. Ou baixe em: https://maven.apache.org/download.cgi
        echo 3. Ou use Docker: docker-compose up
        exit /b 1
    )
)
