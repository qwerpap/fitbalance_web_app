#!/bin/bash

echo "🐳 Мониторинг Redis в Docker..."
echo ""

# Проверяем установлен ли Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker не установлен!"
    echo ""
    echo "Установите Docker Desktop:"
    echo "👉 brew install --cask docker"
    echo "👉 https://www.docker.com/products/docker-desktop"
    echo ""
    echo "Или используйте локальный Redis:"
    echo "👉 ./test_cache.sh"
    exit 1
fi

# Проверяем запущен ли Docker
if ! docker info &> /dev/null; then
    echo "❌ Docker не запущен!"
    echo ""
    echo "Запустите Docker Desktop из Applications"
    echo ""
    echo "Или используйте локальный Redis:"
    echo "👉 ./test_cache.sh"
    exit 1
fi

# Проверяем запущен ли контейнер Redis
if ! docker ps | grep -q fitbalance-redis; then
    echo "⚠️  Redis контейнер не запущен"
    echo ""
    echo "Запустить Redis в Docker:"
    echo "👉 docker compose up -d redis"
    echo ""
    read -p "Запустить сейчас? (y/n): " answer
    if [ "$answer" = "y" ]; then
        echo ""
        echo "Запускаю Redis..."
        docker compose up -d redis
        sleep 2
    else
        echo ""
        echo "Используйте локальный Redis:"
        echo "👉 ./test_cache.sh"
        exit 0
    fi
fi

echo "1️⃣ Проверка Docker Redis:"
docker exec -it fitbalance-redis redis-cli ping
echo ""

echo "2️⃣ Количество ключей:"
docker exec -it fitbalance-redis redis-cli DBSIZE
echo ""

echo "3️⃣ Все ключи в кэше:"
docker exec -it fitbalance-redis redis-cli KEYS '*'
echo ""

echo "4️⃣ Мониторинг в реальном времени (Ctrl+C для выхода):"
echo "---"
docker exec -it fitbalance-redis redis-cli MONITOR

