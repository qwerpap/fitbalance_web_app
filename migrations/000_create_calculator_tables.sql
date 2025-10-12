-- Создание таблиц для калькулятора

-- Таблица с информацией о пользователях (для калькулятора)
CREATE TABLE IF NOT EXISTS user_info (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    age INTEGER NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    height INTEGER NOT NULL,
    gender VARCHAR(10) NOT NULL,
    activity_level DOUBLE PRECISION NOT NULL,
    goal VARCHAR(50) NOT NULL,
    pregnancy BOOLEAN DEFAULT FALSE,
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    updated_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- Таблица с результатами расчетов калорий
CREATE TABLE IF NOT EXISTS calculation_result (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50),
    tdee DOUBLE PRECISION NOT NULL,
    protein DOUBLE PRECISION NOT NULL,
    fat DOUBLE PRECISION NOT NULL,
    carbs DOUBLE PRECISION NOT NULL,
    recommended_calories DOUBLE PRECISION NOT NULL,
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- Индексы для user_info
CREATE INDEX IF NOT EXISTS idx_user_info_user_id ON user_info(user_id);
CREATE INDEX IF NOT EXISTS idx_user_info_created_at ON user_info(created_at DESC);

-- Индексы для calculation_result
CREATE INDEX IF NOT EXISTS idx_calculation_result_user_id ON calculation_result(user_id);
CREATE INDEX IF NOT EXISTS idx_calculation_result_created_at ON calculation_result(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_calculation_result_user_id_created_at ON calculation_result(user_id, created_at DESC);

-- Комментарии к таблицам
COMMENT ON TABLE user_info IS 'Информация о пользователях для калькулятора';
COMMENT ON TABLE calculation_result IS 'История расчетов калорий пользователей';

