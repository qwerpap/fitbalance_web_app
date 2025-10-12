

### 1️⃣ Запустить Docker (PostgreSQL + Redis + RabbitMQ)
```bash
docker compose up -d
```

### 2️⃣ Запустить сервер
```bash
./start.sh
```

### 3️⃣ Проверить сервисы
```bash
# Проверить сервер
http://localhost:8080/

# Проверить Docker контейнеры
# docker ps

# Проверить кэш Redis
# redis-cli DBSIZE
# redis-cli KEYS *

# Проверить очереди RabbitMQ
# http://localhost:8080/queue/stats
```

### 4️⃣ Мониторинг в реальном времени
```bash
# Мониторинг Redis кэша
./docker-monitor.sh

# Мониторинг RabbitMQ очередей
./queue-monitor.sh
```

### 5️⃣ Открыть в браузере
```
http://localhost:8080/auth/test
http://localhost:8080/static/dashboard.html
http://localhost:8080/static/google-auth-test.html

http://localhost:8080/static/queue-demo.html  - 🆕 Демо очередей!

http://localhost:15672 - RabbitMQ Management UI
```

### 6️⃣ Демонстрация очередей
```bash
# Автоматическое тестирование всех очередей
./test-queue.sh

# Откройте веб-интерфейс для визуальной демонстрации
http://localhost:8080/static/queue-demo.html
```

---



















## 🛑 Остановить все
```bash
# Остановить Docker
docker compose down
```

## 📦 Архитектура

### Стек технологий
- **Backend**: Kotlin + Ktor
- **Database**: PostgreSQL
- **Cache**: Redis (in-memory кэш)
- **Queue**: RabbitMQ (асинхронная обработка)
- **Auth**: Google OAuth 2.0 + JWT
- **Build**: Gradle

### Компоненты системы
```
[Client] → [Ktor Server] → [Redis Cache] → [PostgreSQL]
                ↓              ↓
            [JWT Auth]   [RabbitMQ Queue]
                ↓              ↓
          [Google OAuth] [Async Workers]
```

## 🔐 API Endpoints

### Публичные
- `GET /` - главная страница
- `POST /login` - логин
- `POST /register` - регистрация
- `GET /auth/google/url` - получить URL для OAuth
- `GET /auth/google/callback` - OAuth callback
- `GET /auth/test` - тест авторизации

### Защищенные (требуют JWT токен)
- `GET /calculator` - фитнес калькулятор
- `POST /calculate` - расчет с сохранением
- `GET /user/info` - информация о пользователе
- `GET /calculations/{id}` - получить расчет
- `GET /calculations/user/{userId}` - расчеты пользователя

### Admin только
- `GET /admin/users` - список всех пользователей
- `PUT /admin/users/{userId}/role` - изменить роль пользователя
- `POST /admin/change-role` - изменить роль

### Очереди (Queue API)
- `POST /queue/calculation` - отправить расчет в очередь
- `POST /queue/notification` - отправить уведомление в очередь
- `POST /queue/email` - отправить email в очередь
- `GET /queue/stats` - статистика очередей
- `DELETE /queue/{queueName}` - очистить очередь

## 🗄️ База данных (PostgreSQL)

```sql
 public | users              | table | danil  -- Пользователи
 public | user_info          | table | danil  -- Доп. инфо
 public | calculation_result | table | danil  -- Результаты расчетов
```

## ⚡ Кэширование (Redis)

### Что кэшируется:
- ✅ Пользователи (по ID, email, googleId, login)
- ✅ OAuth коды (защита от replay атак)
- ✅ Результаты расчетов калькулятора
- ✅ JWT сессии (готово к использованию)

### Проверка Redis:
```bash
redis-cli ping  # Должно вывести: PONG
redis-cli KEYS *  # Все ключи в кэше
```

## 🐰 Очереди сообщений (RabbitMQ)

### Зачем нужны очереди:
- **Асинхронная обработка**: тяжелые расчеты не блокируют основной поток
- **Масштабируемость**: можно запустить несколько обработчиков
- **Надежность**: сообщения не теряются при сбоях
- **Разделение задач**: расчеты, уведомления, email - каждое в своей очереди

### Доступные очереди:
1. **calculations** - расчеты калькулятора
2. **notifications** - уведомления пользователям
3. **emails** - отправка писем

### Пример использования:
```bash
# Отправить расчет в очередь
curl -X POST http://localhost:8080/queue/calculation \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "123",
    "weight": 70,
    "height": 175,
    "age": 25,
    "gender": "male",
    "activityLevel": "moderate",
    "goal": "maintain"
  }'

# Получить статистику очередей
curl http://localhost:8080/queue/stats

# Очистить очередь (для тестирования)
curl -X DELETE http://localhost:8080/queue/calculations
```

### RabbitMQ Management UI:
- **URL**: http://localhost:15672
- **Логин**: fitbalance
- **Пароль**: fitbalance

### Проверка RabbitMQ:
```bash
# Мониторинг очередей
./queue-monitor.sh

# Через Docker
docker exec fitbalance-rabbitmq rabbitmqctl list_queues

# Проверка статуса
docker exec fitbalance-rabbitmq rabbitmqctl status
```

## 🧪 Как тестировать очереди

### Способ 1: Веб-интерфейс (самый простой) 🌐

Откройте в браузере:
```
http://localhost:8080/static/queue-demo.html
```

**Что делать:**
1. Нажимайте кнопки "Отправить расчет", "Отправить уведомление", "Отправить Email"
2. Наблюдайте как счетчики увеличиваются, а затем обнуляются (задачи обработаны!)
3. Попробуйте "Нагрузочный тест" - 10 задач обработаются за секунды
4. Смотрите лог активности в реальном времени

### Способ 2: RabbitMQ Management UI (детальный) 🐰

1. **Откройте:** http://localhost:15672
   - Логин: `fitbalance`
   - Пароль: `fitbalance`

2. **Вкладка "Queues"** - посмотрите очереди:
   - `calculations` - расчеты калькулятора
   - `notifications` - уведомления
   - `emails` - отправка писем

3. **Отправить сообщение вручную:**
   - Нажмите на нужную очередь (например, `calculations`)
   - Перейдите в раздел "Publish message"
   - В поле "Payload" вставьте JSON:
   ```json
   {
     "userId": "test-123",
     "weight": 75,
     "height": 180,
     "age": 28,
     "gender": "male",
     "activityLevel": "moderate",
     "goal": "lose"
   }
   ```
   - Нажмите "Publish message"
   - Через секунду вернитесь на вкладку "Overview" - увидите что сообщение обработано!

4. **Просмотр сообщений в очереди:**
   - Выберите очередь
   - Раздел "Get messages"
   - Нажмите "Get Message(s)" - увидите содержимое

5. **Графики:**
   - Вкладка "Overview" → Message rates
   - Видны графики отправки и обработки сообщений в реальном времени

### Способ 3: Автоматический тест-скрипт 🤖

```bash
./test-queue.sh
```
Отправляет 20 тестовых сообщений и показывает статистику.

### Способ 4: Curl команды (для быстрой проверки) ⚡

```bash
# Отправить расчет
curl -X POST http://localhost:8080/queue/calculation \
  -H "Content-Type: application/json" \
  -d '{"userId":"test","weight":70,"height":175,"age":25,"gender":"male","activityLevel":"moderate","goal":"lose"}'

# Проверить статистику
curl http://localhost:8080/queue/stats

# Отправить 5 расчетов сразу
for i in {1..5}; do
  curl -s -X POST http://localhost:8080/queue/calculation \
    -H "Content-Type: application/json" \
    -d "{\"userId\":\"user-$i\",\"weight\":75,\"height\":180,\"age\":25,\"gender\":\"male\",\"activityLevel\":\"moderate\",\"goal\":\"maintain\"}"
done
```

### Способ 5: Мониторинг в реальном времени 📊

```bash
./queue-monitor.sh
```
Показывает количество сообщений в очередях каждые 2 секунды.

---

## 🔧 Переменные окружения (.env)

```bash
# Database (Docker)
DB_PASSWORD=fitbalance_password

# Google OAuth (ваши ключи)
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret

# RabbitMQ (Docker)
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=fitbalance
RABBITMQ_PASSWORD=fitbalance
```

## 🧪 Тестирование

```bash
# Запустить тесты
./gradlew test

# Проверить линтер
./gradlew check
```

## 🛠️ Troubleshooting

### Redis не подключается
```bash
# Проверить что запущен
redis-cli ping

# Запустить заново
docker compose restart redis
```

### PostgreSQL не подключается
```bash
# Проверить подключение
psql -h localhost -U danil -d fitbalance

# Применить миграции
./apply_migration.sh

# Перезапустить
docker compose restart postgres
```

### RabbitMQ не подключается
```bash
# Проверить статус
docker exec fitbalance-rabbitmq rabbitmqctl status

# Перезапустить
docker compose restart rabbitmq

# Проверить логи
docker logs fitbalance-rabbitmq
```

### Приложение не стартует
```bash
# Очистить build
./gradlew clean

# Пересобрать
./gradlew build

# Запустить
./gradlew run
```

### Очереди не обрабатываются
```bash
# Проверить consumers
docker exec fitbalance-rabbitmq rabbitmqctl list_consumers

# Проверить статистику
curl http://localhost:8080/queue/stats

# Очистить очередь и попробовать снова
curl -X DELETE http://localhost:8080/queue/calculations
```

---

## 🎯 Архитектура системы очередей

### Как это работает:

1. **Producer** (отправитель):
   - Клиент отправляет запрос на `/queue/calculation`
   - Сервер добавляет сообщение в RabbitMQ
   - Возвращает ответ `202 Accepted` (задача принята)

2. **Queue** (очередь):
   - RabbitMQ хранит сообщения
   - Гарантирует доставку
   - Обрабатывает в порядке поступления

3. **Consumer** (обработчик):
   - Берет сообщение из очереди
   - Выполняет расчет
   - Отправляет уведомление о результате
   - Подтверждает обработку (ACK)

### Преимущества:
- ⚡ **Быстрый ответ клиенту** - не ждем завершения расчета
- 🔄 **Retry при ошибках** - сообщение вернется в очередь
- 📈 **Масштабирование** - можно запустить больше обработчиков
- 🛡️ **Надежность** - сообщения не теряются

---

**Готово!** 🎉 Приложение работает с кэшированием (Redis) и очередями (RabbitMQ)!