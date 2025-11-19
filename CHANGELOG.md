# Changelog

Todos los cambios notables de este proyecto se documentarán en este archivo.

---

## [1.0.0] - 2025-11-19

### ✨ Características Implementadas

#### Nivel 0 - Integración con API Puntored
- ✅ Autenticación con Puntored (`POST /api/auth`)
- ✅ Obtener lista de proveedores (`GET /api/suppliers`)
- ✅ Realizar compra de recarga (`POST /api/recharges`)
- ✅ Cliente HTTP reactivo con WebClient
- ✅ Manejo de token cacheado thread-safe con `AtomicReference`

#### Nivel 1 - Persistencia de Datos
- ✅ Almacenamiento de transacciones en PostgreSQL (Supabase)
- ✅ Historial de transacciones (`GET /api/transactions`)
- ✅ Consulta de transacción por ID (`GET /api/transactions/{id}`)
- ✅ Filtrado de transacciones por teléfono y usuario
- ✅ Índices de base de datos para performance

#### Nivel 2 - API REST Completa
- ✅ Arquitectura Hexagonal (Puertos y Adaptadores)
- ✅ Validaciones de reglas de negocio (Value Objects)
- ✅ Manejo de errores centralizado con `@RestControllerAdvice`
- ✅ Documentación con Swagger/OpenAPI
- ✅ Tests unitarios para Value Objects
- ✅ CORS configurado por perfil (dev/prod)

### 🔒 Seguridad

#### Autenticación JWT con Supabase
- ✅ Validación de tokens JWT de Supabase
- ✅ Verificación de issuer (previene tokens falsificados)
- ✅ Extracción automática de userId del token
- ✅ Protección de endpoints sensibles con JWT obligatorio

#### Control de Acceso
- ✅ Usuarios solo pueden ver sus propias transacciones
- ✅ Validación de ownership en endpoints de consulta
- ✅ Logs de auditoría para intentos no autorizados
- ✅ Exception personalizada `UnauthorizedAccessException` (403 Forbidden)

#### Gestión de Credenciales
- ✅ Separación de configuración por perfiles (dev/prod)
- ✅ Variables de entorno para producción
- ✅ `.gitignore` actualizado para prevenir leak de secrets
- ✅ Archivos `-example` con placeholders
- ✅ Documentación de seguridad en `SECURITY.md`

### 🚀 Mejoras de Performance y Confiabilidad

#### Auto-Recuperación de Errores
- ✅ Refresh automático de token de Puntored en errores 401
- ✅ Retry logic en `getSuppliers()` tras expiración de token
- ✅ Invalidación de cache en errores de autenticación

#### Logging Optimizado
- ✅ Logs de nivel apropiado (debug/info/warn/error)
- ✅ Logs menos verbosos en producción
- ✅ Información de auditoría para seguridad

### 🗃️ Base de Datos

#### Modelo de Datos
- ✅ Tabla `transactions` con todos los campos necesarios
- ✅ Campo `user_id` NOT NULL para trazabilidad
- ✅ Estados de transacción: PENDING, COMPLETED, FAILED
- ✅ Timestamps automáticos (`created_at`, `updated_at`)

#### Scripts SQL
- ✅ `schema.sql` - Creación inicial de tablas
- ✅ `migration_add_user_id.sql` - Migración para agregar user_id
- ✅ `migration_make_user_id_required.sql` - Hacer user_id obligatorio
- ✅ Índices para mejorar performance

### 📚 Documentación

#### Archivos Creados
- ✅ `README.md` - Documentación principal del proyecto
- ✅ `SECURITY.md` - Guía de seguridad y manejo de credenciales
- ✅ `FRONTEND_JWT_INSTRUCTIONS.md` - Guía para integración con frontend
- ✅ `.env.example` - Template de variables de entorno
- ✅ `CHANGELOG.md` - Este archivo

#### Documentación de API
- ✅ Swagger UI disponible en `/swagger-ui.html`
- ✅ OpenAPI JSON en `/v3/api-docs`
- ✅ Anotaciones detalladas en controllers

### 🔧 Configuración

#### Perfiles de Spring
- ✅ `dev` - Desarrollo local (hardcoded credentials)
- ✅ `prod` - Producción (variables de entorno)

#### CORS
- ✅ Configuración específica por perfil
- ✅ Orígenes permitidos configurables
- ✅ Headers necesarios para JWT

---

## Correcciones Críticas de Seguridad

### [1.0.1] - 2025-11-19

#### 🔒 Seguridad
- 🔧 Token de Puntored con refresh automático en 401
- 🔧 Validación de issuer en JWT de Supabase
- 🔧 Logging optimizado (menos verboso en producción)

#### 🛡️ Protección de Endpoints
- 🔧 `GET /api/transactions/{id}` ahora valida JWT y ownership
- 🔧 `GET /api/transactions/phone/{phoneNumber}` ahora requiere JWT
- 🔧 Nueva excepción `UnauthorizedAccessException` para 403

#### 🧹 Limpieza de Código
- 🗑️ Eliminado `UserIdValidationService` (obsoleto tras JWT)
- 🗑️ Removido header `X-User-Id` de CORS (reemplazado por JWT)
- 🔧 Campo `user_id` ahora es NOT NULL en base de datos

---

## Pendiente para Próximas Versiones

### [1.1.0] - Por Implementar

#### 🚀 Performance
- [ ] Rate limiting en `/api/auth` (protección contra brute force)
- [ ] Paginación en `/api/transactions`
- [ ] Cache de proveedores (reduce llamadas a Puntored)
- [ ] Timeouts globales configurados en WebClient

#### 📊 Monitoreo
- [ ] Health check endpoint (`/actuator/health`)
- [ ] Métricas con Micrometer
- [ ] Logs estructurados (JSON)

#### 🔄 Resiliencia
- [ ] Retry con backoff exponencial
- [ ] Circuit breaker para llamadas a Puntored
- [ ] Fallback responses

---

## Notas de Versión

### Compatibilidad
- Java 17+
- Spring Boot 3.3.5
- PostgreSQL 12+

### Dependencias Principales
- Spring Web
- Spring Data JPA
- Spring WebFlux (WebClient)
- JJWT 0.12.3
- SpringDoc OpenAPI 2.6.0
- Lombok

---

**Formato basado en [Keep a Changelog](https://keepachangelog.com/)**

