-- Migración: Agregar columna user_id a tabla transactions existente
-- Ejecutar este script en Supabase si ya tienes la tabla creada

-- 1. Agregar columna user_id (permite NULL inicialmente)
ALTER TABLE transactions 
ADD COLUMN IF NOT EXISTS user_id VARCHAR(255);

-- 2. Crear índice para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);

-- 3. Agregar comentario
COMMENT ON COLUMN transactions.user_id IS 'ID del usuario de Supabase (email o UUID) que realizó la transacción';

-- 4. (RECOMENDADO) Asignar transacciones existentes a un usuario
-- IMPORTANTE: Ejecutar este paso ANTES de hacer la columna NOT NULL
-- Opción A: Asignar a un usuario específico
-- UPDATE transactions 
-- SET user_id = 'admin@puntored.com' 
-- WHERE user_id IS NULL;

-- Opción B: Eliminar transacciones sin usuario (si son datos de prueba)
-- DELETE FROM transactions WHERE user_id IS NULL;

-- 5. (IMPORTANTE) Hacer la columna NOT NULL después de migrar datos
-- Solo ejecutar después de asignar usuarios a todas las transacciones
ALTER TABLE transactions ALTER COLUMN user_id SET NOT NULL;

-- Verificar que se agregó correctamente
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'transactions' 
ORDER BY ordinal_position;

