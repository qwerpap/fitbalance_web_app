#!/bin/bash

# Скрипт для мониторинга RabbitMQ очередей в реальном времени

echo "=========================================="
echo "🐰 RabbitMQ Queue Monitor"
echo "=========================================="
echo ""
echo "Мониторинг очередей FitBalance:"
echo "- calculations (расчеты калькулятора)"
echo "- notifications (уведомления)"
echo "- emails (письма)"
echo ""
echo "Нажмите Ctrl+C для выхода"
echo "=========================================="
echo ""

# Функция для получения статистики очереди
get_queue_stats() {
    local queue_name=$1
    docker exec fitbalance-rabbitmq rabbitmqctl list_queues name messages consumers 2>/dev/null | grep "^$queue_name" || echo "$queue_name 0 0"
}

# Функция для вывода статистики с цветами
print_stats() {
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    
    echo "─────────────────────────────────────────"
    echo "📅 Время: $timestamp"
    echo "─────────────────────────────────────────"
    
    # Проверка подключения к RabbitMQ
    if ! docker exec fitbalance-rabbitmq rabbitmqctl status &>/dev/null; then
        echo "❌ RabbitMQ не доступен"
        return
    fi
    
    echo "✅ RabbitMQ Status: Running"
    echo ""
    
    # Получаем статистику всех очередей
    local calc_stats=$(get_queue_stats "calculations")
    local notif_stats=$(get_queue_stats "notifications")
    local email_stats=$(get_queue_stats "emails")
    
    # Парсим результаты
    local calc_messages=$(echo "$calc_stats" | awk '{print $2}')
    local calc_consumers=$(echo "$calc_stats" | awk '{print $3}')
    
    local notif_messages=$(echo "$notif_stats" | awk '{print $2}')
    local notif_consumers=$(echo "$notif_stats" | awk '{print $3}')
    
    local email_messages=$(echo "$email_stats" | awk '{print $2}')
    local email_consumers=$(echo "$email_stats" | awk '{print $3}')
    
    # Выводим статистику
    echo "📊 Очереди:"
    echo ""
    echo "  🧮 calculations:"
    echo "     ├─ Сообщений в очереди: $calc_messages"
    echo "     └─ Активных consumers: $calc_consumers"
    echo ""
    echo "  🔔 notifications:"
    echo "     ├─ Сообщений в очереди: $notif_messages"
    echo "     └─ Активных consumers: $notif_consumers"
    echo ""
    echo "  ✉️  emails:"
    echo "     ├─ Сообщений в очереди: $email_messages"
    echo "     └─ Активных consumers: $email_consumers"
    echo ""
    
    # Общая статистика
    local total_messages=$((calc_messages + notif_messages + email_messages))
    local total_consumers=$((calc_consumers + notif_consumers + email_consumers))
    
    echo "📈 Итого:"
    echo "   ├─ Всего сообщений: $total_messages"
    echo "   └─ Всего consumers: $total_consumers"
    echo ""
    
    # Management UI ссылка
    echo "🌐 Management UI: http://localhost:15672"
    echo "   (логин: fitbalance / пароль: fitbalance_queue_password)"
}

# Основной цикл мониторинга
while true; do
    clear
    echo "=========================================="
    echo "🐰 RabbitMQ Queue Monitor"
    echo "=========================================="
    echo ""
    
    print_stats
    
    echo ""
    echo "🔄 Обновление через 2 секунды..."
    sleep 2
done

