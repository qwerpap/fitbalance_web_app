-- Добавление временной метки к таблице calculation_result
-- Проверяем, существует ли колонка created_at, и добавляем только если её нет
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'calculation_result' 
        AND column_name = 'created_at'
    ) THEN
        ALTER TABLE calculation_result 
        ADD COLUMN created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT;
    END IF;
END $$;

-- Обновление существующих записей, если created_at NULL или 0
UPDATE calculation_result 
SET created_at = (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT 
WHERE created_at IS NULL OR created_at = 0;

-- Создание индекса для ускорения сортировки по времени
CREATE INDEX IF NOT EXISTS idx_calculation_result_created_at ON calculation_result(created_at DESC);

-- Создание индекса для ускорения поиска по user_id и времени
CREATE INDEX IF NOT EXISTS idx_calculation_result_user_id_created_at ON calculation_result(user_id, created_at DESC);

