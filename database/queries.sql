-- Queries útiles para consultar transacciones

-- Listar todas las transacciones ordenadas por fecha (más recientes primero)
SELECT * FROM transactions ORDER BY created_at DESC;

-- Contar transacciones por estado
SELECT status, COUNT(*) as total
FROM transactions
GROUP BY status;

-- Transacciones por proveedor
SELECT supplier_name, COUNT(*) as total, SUM(amount) as total_amount
FROM transactions
WHERE status = 'COMPLETED'
GROUP BY supplier_name
ORDER BY total DESC;

-- Transacciones de un número de teléfono específico
SELECT * FROM transactions
WHERE phone_number = '3001234567'
ORDER BY created_at DESC;

-- Transacciones del día actual
SELECT * FROM transactions
WHERE DATE(created_at) = CURRENT_DATE
ORDER BY created_at DESC;

-- Transacciones fallidas con mensaje de error
SELECT id, phone_number, amount, supplier_name, error_message, created_at
FROM transactions
WHERE status = 'FAILED'
ORDER BY created_at DESC;

-- Estadísticas generales
SELECT
    COUNT(*) as total_transactions,
    COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed,
    COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed,
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending,
    SUM(CASE WHEN status = 'COMPLETED' THEN amount ELSE 0 END) as total_amount_completed,
    AVG(CASE WHEN status = 'COMPLETED' THEN amount ELSE NULL END) as average_amount
FROM transactions;

-- Top 10 números con más recargas
SELECT phone_number, COUNT(*) as total_recharges, SUM(amount) as total_spent
FROM transactions
WHERE status = 'COMPLETED'
GROUP BY phone_number
ORDER BY total_recharges DESC
LIMIT 10;

-- Transacciones de la última semana
SELECT DATE(created_at) as date, COUNT(*) as transactions, SUM(amount) as total_amount
FROM transactions
WHERE created_at >= CURRENT_DATE - INTERVAL '7 days'
  AND status = 'COMPLETED'
GROUP BY DATE(created_at)
ORDER BY date DESC;

