@echo off
REM Script de compilación para Windows

echo.
echo ========================================
echo Sistema de Inventario de Equipos
echo Build Script - Windows
echo ========================================
echo.

REM Verificar si Maven está instalado
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven no está instalado o no está en PATH
    echo Descargar desde: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Limpiar y construir
echo [1/3] Limpiando proyecto previo...
call mvn clean

echo.
echo [2/3] Instalando dependencias...
call mvn install -DskipTests

echo.
echo [3/3] Compilando proyecto...
call mvn compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo [OK] Compilación exitosa!
    echo ========================================
    echo.
    echo Para ejecutar: mvn spring-boot:run
    echo.
    pause
) else (
    echo.
    echo ========================================
    echo [ERROR] La compilación falló
    echo ========================================
    pause
    exit /b 1
)
