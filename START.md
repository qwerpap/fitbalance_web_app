# 🚀 FitBalance Web App - Быстрый старт

## ⚡ Команды для запуска (для демонстрации)

### 1️⃣ Запустить Docker (PostgreSQL + Redis)
```bash
docker compose up -d
```

### 2️⃣ Запустить сервер
```bash
./start.sh
```


```bash
# Проверить сервер
http://localhost:8080/

# Проверить Docker контейнеры
docker ps

# Проверить кэш Redis
redis-cli DBSIZE
redis-cli KEYS *
```

### 4️⃣ Мониторинг кэша в реальном времени
```bash
./docker-monitor.sh
```

### 5️⃣ Открыть в браузере
```
http://localhost:8080/static/dashboard.html
http://localhost:8080/static/google-auth-test.html
http://localhost:8080/auth/test
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
- **Auth**: Google OAuth 2.0 + JWT
- **Build**: Gradle

### Компоненты системы
```
[Client] → [Ktor Server] → [Redis Cache] → [PostgreSQL]
                ↓
            [JWT Auth]
                ↓
          [Google OAuth]
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

## 🔧 Переменные окружения (.env)

```bash
# Database (Docker)
DB_PASSWORD=fitbalance_password

# Google OAuth (ваши ключи)
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret
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
brew services restart redis
# или
docker-compose restart redis
```

### PostgreSQL не подключается
```bash
# Проверить подключение
psql -h localhost -U danil -d fitbalance

# Применить миграции
./apply_migration.sh
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

---

**Готово!** 🎉 Приложение работает с кэшированием через Redis!