#!/bin/bash

# Скрипт для применения миграций к базе данных PostgreSQL

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== Применение миграций базы данных ===${NC}\n"

# Параметры подключения к БД (измените на свои)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-fitbalance}"
DB_USER="${DB_USER:-danil}"

echo -e "${YELLOW}Параметры подключения:${NC}"
echo "  Host: $DB_HOST"
echo "  Port: $DB_PORT"
echo "  Database: $DB_NAME"
echo "  User: $DB_USER"
echo ""

# Проверка наличия psql
if ! command -v psql &> /dev/null; then
    echo -e "${RED}Ошибка: psql не найден. Установите PostgreSQL client.${NC}"
    exit 1
fi

# Применение миграций
MIGRATIONS_DIR="./migrations"

if [ ! -d "$MIGRATIONS_DIR" ]; then
    echo -e "${RED}Ошибка: Директория миграций не найдена: $MIGRATIONS_DIR${NC}"
    exit 1
fi

echo -e "${YELLOW}Применение миграций...${NC}\n"

for migration in "$MIGRATIONS_DIR"/*.sql; do
    if [ -f "$migration" ]; then
        echo -e "${GREEN}Применение: $(basename "$migration")${NC}"
        
        # Применяем миграцию
        PGPASSWORD=$DB_PASSWORD psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$migration"
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Успешно применена: $(basename "$migration")${NC}\n"
        else
            echo -e "${RED}✗ Ошибка при применении: $(basename "$migration")${NC}\n"
            exit 1
        fi
    fi
done

echo -e "${GREEN}=== Все миграции успешно применены! ===${NC}"

