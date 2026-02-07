#!/bin/bash

echo "========================================"
echo "  MARKETPLACE DE SERVICOS - BACKEND"
echo "========================================"
echo ""

# Verificar Maven
if ! command -v mvn &> /dev/null; then
    echo "[ERRO] Maven não encontrado!"
    echo ""
    echo "Para executar o backend, instale o Maven:"
    echo "  - Ubuntu/Debian: sudo apt install maven"
    echo "  - macOS: brew install maven"
    echo "  - Ou use Docker: docker-compose up"
    echo ""
    exit 1
fi

echo "[OK] Maven encontrado!"
echo ""

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "[ERRO] Java não encontrado!"
    echo "Instale Java 21 LTS"
    exit 1
fi

echo "[OK] Java encontrado!"
echo ""
java -version
echo ""

echo "Iniciando o backend..."
echo ""
mvn spring-boot:run
