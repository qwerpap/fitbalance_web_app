#!/bin/bash

# Скрипт для демонстрации работы RabbitMQ очередей

echo "=========================================="
echo "🧪 Тестирование RabbitMQ очередей"
echo "=========================================="
echo ""

# Цвета для вывода
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Проверка что сервер запущен
echo "🔍 Проверка сервера..."
if ! curl -s http://localhost:8080/queue/stats > /dev/null; then
    echo "❌ Сервер не запущен! Запустите ./start.sh"
    exit 1
fi
echo -e "${GREEN}✅ Сервер работает${NC}"
echo ""

# Показываем начальную статистику
echo "📊 Начальная статистика очередей:"
curl -s http://localhost:8080/queue/stats | python3 -m json.tool
echo ""

# Функция для красивого вывода
send_message() {
    local type=$1
    local endpoint=$2
    local data=$3
    local desc=$4
    
    echo -e "${BLUE}➤ Отправка: $desc${NC}"
    response=$(curl -s -X POST "http://localhost:8080/queue/$endpoint" \
        -H "Content-Type: application/json" \
        -d "$data")
    echo "   Ответ: $response"
    echo ""
}

# Демо 1: Отправляем несколько расчетов
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${YELLOW}📦 Демо 1: Отправка 5 расчетов в очередь${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

for i in {1..5}; do
    weight=$((70 + i * 2))
    age=$((25 + i))
    
    send_message "calculation" "calculation" \
        "{\"userId\":\"demo-user-$i\",\"weight\":$weight,\"height\":180,\"age\":$age,\"gender\":\"male\",\"activityLevel\":\"moderate\",\"goal\":\"lose\"}" \
        "Расчет #$i (вес: $weight кг, возраст: $age)"
    
    sleep 0.5
done

echo "⏳ Ждем 2 секунды для обработки..."
sleep 2

echo "📊 Статистика после расчетов:"
curl -s http://localhost:8080/queue/stats | python3 -m json.tool
echo ""

# Демо 2: Отправляем уведомления
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${YELLOW}🔔 Демо 2: Отправка 3 уведомлений${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

types=("success" "info" "warning")
titles=("Успех!" "Информация" "Внимание!")
messages=("Ваш расчет готов" "Новое обновление" "Проверьте данные")

for i in {0..2}; do
    send_message "notification" "notification" \
        "{\"userId\":\"demo-user-$i\",\"title\":\"${titles[$i]}\",\"message\":\"${messages[$i]}\",\"type\":\"${types[$i]}\"}" \
        "Уведомление: ${titles[$i]}"
    
    sleep 0.3
done

echo "⏳ Ждем 2 секунды для обработки..."
sleep 2

echo "📊 Статистика после уведомлений:"
curl -s http://localhost:8080/queue/stats | python3 -m json.tool
echo ""

# Демо 3: Отправляем emails
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${YELLOW}✉️  Демо 3: Отправка 2 писем${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

send_message "email" "email" \
    "{\"to\":\"user1@example.com\",\"subject\":\"Добро пожаловать\",\"body\":\"Спасибо за регистрацию!\"}" \
    "Email для user1@example.com"

send_message "email" "email" \
    "{\"to\":\"user2@example.com\",\"subject\":\"Ваши результаты\",\"body\":\"BMI: 23.4, Калории: 2100\"}" \
    "Email для user2@example.com"

echo "⏳ Ждем 2 секунды для обработки..."
sleep 2

echo "📊 Финальная статистика:"
curl -s http://localhost:8080/queue/stats | python3 -m json.tool
echo ""

# Демо 4: Массовая нагрузка
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${YELLOW}🚀 Демо 4: Массовая отправка (10 задач)${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "Отправка 10 расчетов одновременно..."

for i in {1..10}; do
    curl -s -X POST http://localhost:8080/queue/calculation \
        -H "Content-Type: application/json" \
        -d "{\"userId\":\"stress-test-$i\",\"weight\":75,\"height\":180,\"age\":30,\"gender\":\"male\",\"activityLevel\":\"moderate\",\"goal\":\"maintain\"}" > /dev/null &
done

echo "⏳ Ждем завершения отправки..."
wait

echo ""
echo "📊 Статистика сразу после отправки:"
curl -s http://localhost:8080/queue/stats | python3 -m json.tool
echo ""

echo "⏳ Ждем 3 секунды для обработки..."
sleep 3

echo "📊 Статистика после обработки:"
curl -s http://localhost:8080/queue/stats | python3 -m json.tool
echo ""

# Показываем статистику RabbitMQ
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${YELLOW}📈 Статистика RabbitMQ (из Docker)${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
docker exec fitbalance-rabbitmq rabbitmqctl list_queues name messages consumers
echo ""

echo "=========================================="
echo -e "${GREEN}✅ Тестирование завершено!${NC}"
echo "=========================================="
echo ""
echo "📝 Итого отправлено:"
echo "   • 15 расчетов"
echo "   • 3 уведомления"
echo "   • 2 email"
echo "   • Всего: 20 сообщений"
echo ""
echo "🌐 Откройте RabbitMQ Management UI:"
echo "   http://localhost:15672"
echo "   Логин: fitbalance"
echo "   Пароль: fitbalance"
echo ""

