-- ============================================
-- Migración: Hacer user_id NOT NULL
-- ============================================
-- IMPORTANTE: Este script debe ejecutarse DESPUÉS de migration_add_user_id.sql
-- y DESPUÉS de asignar userId a todas las transacciones existentes

-- Paso 1: Verificar que NO haya transacciones sin user_id
DO $$
DECLARE
    null_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO null_count
    FROM transactions
    WHERE user_id IS NULL;
    
    IF null_count > 0 THEN
        RAISE EXCEPTION 'ERROR: Existen % transacciones sin user_id. Debes asignar user_id a todas las transacciones antes de continuar.', null_count;
    END IF;
    
    RAISE NOTICE 'OK: Todas las transacciones tienen user_id asignado';
END $$;

-- Paso 2: Hacer la columna NOT NULL
ALTER TABLE transactions ALTER COLUMN user_id SET NOT NULL;

-- Paso 3: Verificar que se aplicó correctamente
SELECT 
    column_name, 
    data_type, 
    character_maximum_length,
    is_nullable 
FROM information_schema.columns 
WHERE table_name = 'transactions' 
  AND column_name = 'user_id';

-- Resultado esperado: is_nullable = 'NO'

RAISE NOTICE 'Migración completada: user_id ahora es NOT NULL';

