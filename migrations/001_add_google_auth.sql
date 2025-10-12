-- Миграция для добавления Google OAuth авторизации
-- Дата: 2025-01-11

-- Добавляем колонку google_id
ALTER TABLE users ADD COLUMN IF NOT EXISTS google_id VARCHAR(100);

-- Делаем login и password необязательными (для Google OAuth пользователей)
ALTER TABLE users ALTER COLUMN login DROP NOT NULL;
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;

-- Увеличиваем размер email для длинных Google email
ALTER TABLE users ALTER COLUMN email TYPE VARCHAR(100);

-- Делаем google_id уникальным
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_google_id ON users(google_id) WHERE google_id IS NOT NULL;

-- Делаем email уникальным
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Устанавливаем значение по умолчанию для role если NULL
UPDATE users SET role = 'user' WHERE role IS NULL;

-- Комментарии к колонкам
COMMENT ON COLUMN users.google_id IS 'Google OAuth user ID';
COMMENT ON COLUMN users.role IS 'User role: user or admin';

