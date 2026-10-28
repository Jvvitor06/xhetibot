@echo off
setlocal

rem Sempre trabalha a partir da pasta onde este arquivo esta (a raiz do projeto)
cd /d "%~dp0"

rem Garante os programas basicos do Windows no PATH (alguns computadores estao sem)
set "PATH=%SystemRoot%\System32;%SystemRoot%;%SystemRoot%\System32\WindowsPowerShell\v1.0;%PATH%"

rem Se o JAVA_HOME estiver definido, usa o JDK dele
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jpackage.exe" set "PATH=%JAVA_HOME%\bin;%PATH%"

rem =====================================================================
rem  Gera o Chat IA para Windows com o jpackage.
rem
rem  Uso:  build-installer.bat             -> instalador .exe (precisa do WiX 3.x)
rem        build-installer.bat appimage    -> pasta portatil + .zip (NAO precisa do WiX)
rem        acrescente  debug  para abrir o app com um console e ver os erros
rem =====================================================================

set APP_NAME=Chat IA
set APP_VERSION=1.0.0
set MAIN_JAR=chat-ia-%APP_VERSION%.jar
set MAIN_CLASS=br.com.chatia.Launcher
set INPUT=target\installer\input
set OUTPUT=target\installer\output
set UPGRADE_UUID=9f70039a-f10d-4d77-9dce-e4d4121f8f1e

set TIPO=exe
set CONSOLE_ARG=
for %%A in (%*) do (
    if /I "%%A"=="debug" set CONSOLE_ARG=--win-console
    if /I "%%A"=="appimage" set TIPO=app-image
)

echo.
echo [1/5] Verificando ferramentas... modo: %TIPO%
where jpackage >nul 2>nul
if errorlevel 1 (
    echo ERRO: jpackage nao encontrado. Confira o JAVA_HOME e o PATH.
    exit /b 1
)
jpackage --version

if /I "%TIPO%"=="exe" (
    where candle >nul 2>nul
    if errorlevel 1 (
        echo ERRO: WiX Toolset 3.x nao encontrado no PATH.
        echo Sem o WiX, gere a versao portatil:  build-installer.bat appimage
        exit /b 1
    )
)

rem Modulos do runtime embutido. O jdk.crypto.ec so existe ate o JDK 21;
rem a partir do JDK 22 ele ja faz parte do java.base.
set MODULES=java.base,java.desktop,java.logging,java.management,java.naming,java.net.http,java.prefs,java.security.sasl,java.sql,java.xml,jdk.unsupported,jdk.xml.dom,jdk.naming.dns
set JMAJOR=0
for /f "tokens=1 delims=." %%v in ('jpackage --version') do set JMAJOR=%%v
if %JMAJOR% LSS 22 set MODULES=%MODULES%,jdk.crypto.ec
echo Modulos do runtime: %MODULES%

echo.
echo [2/5] Compilando o projeto...
where mvn >nul 2>nul
if errorlevel 1 (
    echo Maven fora do PATH: usando o que ja existe em target. Rode clean e package no IntelliJ antes.
) else (
    call mvn -q clean package -DskipTests
    if errorlevel 1 (
        echo ERRO: falha no Maven.
        exit /b 1
    )
)

if not exist "target\%MAIN_JAR%" (
    echo ERRO: target\%MAIN_JAR% nao existe. Rode clean e package no IntelliJ e tente de novo.
    exit /b 1
)

echo.
echo [3/5] Montando a pasta de entrada...
if not exist "%INPUT%" mkdir "%INPUT%"
copy /Y "target\%MAIN_JAR%" "%INPUT%\" >nul

echo.
echo [4/5] Gerando o pacote...
if exist "%OUTPUT%" rmdir /S /Q "%OUTPUT%"

rem Procura um .ico na raiz, em installer e em src\main\resources\images (o ultimo encontrado vale)
set ICON_FILE=
for %%I in (*.ico installer\*.ico src\main\resources\images\*.ico) do set ICON_FILE=%%I
set ICON_ARG=
if defined ICON_FILE (
    set ICON_ARG=--icon "%ICON_FILE%"
    echo Usando o icone: %ICON_FILE%
) else (
    echo AVISO: nenhum arquivo .ico encontrado na raiz do projeto nem em installer. Sera usado o icone padrao do Java.
)

rem Opcoes de atalho/menu so valem para o instalador
set WIN_ARGS=
if /I "%TIPO%"=="exe" set WIN_ARGS=--win-menu --win-shortcut --win-dir-chooser --win-menu-group "Chat IA" --win-per-user-install --win-upgrade-uuid %UPGRADE_UUID%

jpackage ^
  --type %TIPO% ^
  --name "%APP_NAME%" ^
  --app-version %APP_VERSION% ^
  --vendor "Chat IA" ^
  --description "Chat desktop com Inteligencia Artificial" ^
  --input "%INPUT%" ^
  --main-jar %MAIN_JAR% ^
  --main-class %MAIN_CLASS% ^
  --dest "%OUTPUT%" ^
  %ICON_ARG% ^
  --add-modules %MODULES% ^
  --java-options "-Dfile.encoding=UTF-8" ^
  %WIN_ARGS% ^
  %CONSOLE_ARG%

if errorlevel 1 (
    echo ERRO: o jpackage falhou. Leia a mensagem acima.
    exit /b 1
)

echo.
echo [5/5] Finalizando...
if /I "%TIPO%"=="exe" goto :finalizar_exe
goto :finalizar_portatil

:finalizar_exe
ren "%OUTPUT%\%APP_NAME%-%APP_VERSION%.exe" "ChatIA-Setup.exe"
echo Pronto! Instalador em: %OUTPUT%\ChatIA-Setup.exe
goto :fim

:finalizar_portatil
if exist installer\LEIA-ME.txt copy /Y installer\LEIA-ME.txt "%OUTPUT%\%APP_NAME%\" >nul
if exist installer\Configurar.bat copy /Y installer\Configurar.bat "%OUTPUT%\%APP_NAME%\" >nul
if exist installer\Configurar.ps1 copy /Y installer\Configurar.ps1 "%OUTPUT%\%APP_NAME%\" >nul
set TENTATIVAS=0

:zip_tentar
set /a TENTATIVAS+=1
if exist "%OUTPUT%\ChatIA-portatil.zip" del /Q "%OUTPUT%\ChatIA-portatil.zip"
powershell -NoProfile -Command "Compress-Archive -ErrorAction Stop -Path '%OUTPUT%\%APP_NAME%' -DestinationPath '%OUTPUT%\ChatIA-portatil.zip' -Force"
if not errorlevel 1 goto :zip_ok
if %TENTATIVAS% GEQ 3 goto :zip_falhou
echo O Windows ainda esta usando os arquivos, provavelmente o antivirus. Tentando de novo em 5 segundos...
timeout /t 5 /nobreak >nul
goto :zip_tentar

:zip_ok
echo Pronto! App portatil em: %OUTPUT%\%APP_NAME%\%APP_NAME%.exe
echo Zip para levar: %OUTPUT%\ChatIA-portatil.zip
goto :fim

:zip_falhou
if exist "%OUTPUT%\ChatIA-portatil.zip" del /Q "%OUTPUT%\ChatIA-portatil.zip"
echo O app portatil foi gerado, mas nao foi possivel criar o .zip agora.
echo Use a pasta direto: %OUTPUT%\%APP_NAME%
echo Ou compacte depois, quando o antivirus terminar de escanear.

:fim
endlocal