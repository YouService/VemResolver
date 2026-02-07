@echo off
echo ================================================================
echo   BAIXAR E INSTALAR MAVEN AUTOMATICAMENTE
echo ================================================================
echo.
echo Este script vai:
echo   1. Baixar Maven 3.9.9
echo   2. Extrair para C:\Maven
echo   3. Configurar variaveis de ambiente
echo.
echo IMPORTANTE: Execute este script como ADMINISTRADOR!
echo   (Clique com botao direito e "Executar como administrador")
echo.
pause

echo.
echo [1/4] Criando diretorio...
if not exist "C:\Maven" mkdir C:\Maven

echo [2/4] Baixando Maven 3.9.9... (pode demorar)
powershell -Command "& {Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip' -OutFile '%TEMP%\maven.zip'}"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERRO] Falha ao baixar Maven!
    echo Por favor, baixe manualmente em:
    echo https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

echo [3/4] Extraindo arquivo...
powershell -Command "& {Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath 'C:\Maven' -Force}"

echo [4/4] Configurando variaveis de ambiente...
setx MAVEN_HOME "C:\Maven\apache-maven-3.9.9" /M
setx PATH "%PATH%;C:\Maven\apache-maven-3.9.9\bin" /M

echo.
echo [OK] Maven instalado com sucesso!
echo.
echo Local de instalacao: C:\Maven\apache-maven-3.9.9
echo.
echo ================================================================
echo   IMPORTANTE: FECHE ESTE TERMINAL E ABRA UM NOVO!
echo ================================================================
echo.
echo Depois teste: mvn -version
echo.
pause
