-- Script SQL para crear la base de datos de transacciones
-- Base de datos: PostgreSQL

-- Crear tabla de transacciones
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(10) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    supplier_id VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    ticket TEXT,
    error_message TEXT,
    user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_transactions_phone_number ON transactions(phone_number);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_transactions_supplier_id ON transactions(supplier_id);
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);

-- Comentarios de la tabla
COMMENT ON TABLE transactions IS 'Tabla que almacena todas las transacciones de recargas móviles';
COMMENT ON COLUMN transactions.id IS 'Identificador único de la transacción';
COMMENT ON COLUMN transactions.phone_number IS 'Número de teléfono móvil (10 dígitos, inicia con 3)';
COMMENT ON COLUMN transactions.amount IS 'Monto de la recarga (entre 1,000 y 100,000)';
COMMENT ON COLUMN transactions.supplier_id IS 'ID del proveedor de la recarga';
COMMENT ON COLUMN transactions.supplier_name IS 'Nombre del proveedor (ej: Claro, Movistar)';
COMMENT ON COLUMN transactions.status IS 'Estado de la transacción: PENDING, COMPLETED, FAILED';
COMMENT ON COLUMN transactions.ticket IS 'Ticket retornado por Puntored en transacciones exitosas';
COMMENT ON COLUMN transactions.error_message IS 'Mensaje de error en transacciones fallidas';
COMMENT ON COLUMN transactions.user_id IS 'ID del usuario de Supabase (email o UUID) que realizó la transacción';
COMMENT ON COLUMN transactions.created_at IS 'Fecha y hora de creación de la transacción';
COMMENT ON COLUMN transactions.updated_at IS 'Fecha y hora de última actualización';

