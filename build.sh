#!/bin/bash
# Script de compilación para Linux/macOS

echo ""
echo "========================================"
echo "Sistema de Inventario de Equipos"
echo "Build Script - Linux/macOS"
echo "========================================"
echo ""

# Verificar si Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "[ERROR] Maven no está instalado"
    echo "Instalar con: brew install maven (macOS) o apt-get install maven (Linux)"
    exit 1
fi

# Limpiar y construir
echo "[1/3] Limpiando proyecto previo..."
mvn clean

echo ""
echo "[2/3] Instalando dependencias..."
mvn install -DskipTests

echo ""
echo "[3/3] Compilando proyecto..."
mvn compile

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "[OK] Compilación exitosa!"
    echo "========================================"
    echo ""
    echo "Para ejecutar: mvn spring-boot:run"
    echo ""
else
    echo ""
    echo "========================================"
    echo "[ERROR] La compilación falló"
    echo "========================================"
    exit 1
fi
