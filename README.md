# FitBalance Web App

Веб-приложение для фитнес-калькуляций с авторизацией через Google OAuth 2.0.

## 🚀 Возможности

- ✅ **Google OAuth 2.0** - авторизация через Google аккаунт
- ✅ **JWT Authentication** - безопасная аутентификация
- ✅ **Система ролей** - USER и ADMIN роли
- ✅ **Admin панель** - управление пользователями
- ✅ **Фитнес калькулятор** - расчет калорий и макронутриентов
- ✅ **PostgreSQL база данных**

## ⚙️ Начальная настройка

### 1. Настройка переменных окружения

Скопируйте файл с примером и заполните своими данными:

```bash
cp .env.example .env
```

Отредактируйте `.env` и добавьте свои значения:

```bash
# Google OAuth (получите на https://console.cloud.google.com)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8080/auth/google/callback

# JWT Secret (сгенерируйте случайную строку)
JWT_SECRET=your-secure-random-string-here

# Database
DB_USER=your-db-username
DB_PASSWORD=your-db-password
```

### 2. Настройка Google OAuth

1. Перейдите в [Google Cloud Console](https://console.cloud.google.com)
2. Создайте новый проект или выберите существующий
3. Включите **Google+ API**
4. Создайте **OAuth 2.0 Client ID** в разделе "Credentials"
5. Добавьте authorized redirect URI: `http://localhost:8080/auth/google/callback`
6. Скопируйте Client ID и Client Secret в `.env` файл

### 3. Настройка PostgreSQL

```bash
# Создайте базу данных
createdb fitbalance

# Выполните миграции
psql -d fitbalance -f migrations/001_add_google_auth.sql
```

## 🏃 Запуск приложения

### Быстрый старт

```bash
./start.sh
```

Скрипт `start.sh` автоматически:
- Загружает переменные окружения из `.env`
- Настраивает Java PATH
- Запускает сервер через Gradle

### Альтернативные команды

| Команда                       | Описание                                                            |
| ------------------------------|---------------------------------------------------------------------|
| `./gradlew run`               | Запустить сервер (без загрузки .env)                               |
| `./gradlew test`              | Запустить тесты                                                     |
| `./gradlew build`             | Собрать проект                                                      |
| `./gradlew buildFatJar`       | Собрать JAR с зависимостями                                         |

## 📍 Endpoints

### Публичные
- `GET /` - главная страница
- `POST /login` - логин
- `POST /register` - регистрация
- `GET /auth/google/login` - начало OAuth процесса
- `GET /auth/google/callback` - OAuth callback

### Защищенные (требуют JWT токен)
- `GET /calculator` - фитнес калькулятор
- `GET /user/info` - информация о пользователе

### Admin только
- `GET /admin/users` - список всех пользователей
- `PUT /admin/users/{id}/role` - изменить роль пользователя

## 🔒 Безопасность

⚠️ **ВАЖНО:** Файл `.env` содержит секретные данные и **не должен** коммититься в git!

- `.env` - добавлен в `.gitignore`
- `.env.example` - шаблон для создания собственного `.env`

## 📱 Тестирование

Для тестирования Google OAuth откройте:
```
http://localhost:8080/google-auth-test.html
```

## 🛠 Технологии

- **Ktor** - веб-фреймворк
- **Kotlin** - язык программирования
- **PostgreSQL** - база данных
- **Exposed** - ORM
- **JWT** - аутентификация
- **Google OAuth 2.0** - авторизация

---

Если сервер запустился успешно, вы увидите:

```
🚀 Запускаем FitBalance сервер...
📊 Google Client ID: your-client-id
🔐 JWT Secret: your-jwt-secret...
💾 Database: fitbalance (user: your-user)

Application started in 0.303 seconds.
Responding at http://0.0.0.0:8080
```

