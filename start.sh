#!/bin/bash

# Загружаем переменные окружения из .env файла
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | grep -v '^$' | xargs)
else
    echo "❌ Ошибка: файл .env не найден!"
    echo "Скопируйте .env.example в .env и заполните своими значениями"
    exit 1
fi

# Устанавливаем Java PATH
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"

# Запускаем сервер
echo "🚀 Запускаем FitBalance сервер..."
echo "📊 Google Client ID: $GOOGLE_CLIENT_ID"
echo "🔐 JWT Secret: ${JWT_SECRET:0:20}..."
echo "💾 Database: fitbalance (user: $DB_USER)"
echo ""

./gradlew run
