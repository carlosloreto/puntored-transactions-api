# Puntored Transactions API

API REST para el portal transaccional de recargas móviles de Puntored. Implementa una arquitectura hexagonal con Spring Boot 3.5.7 y Java 17.

## 📋 Tabla de Contenidos

- [Características](#características)
- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Requisitos](#requisitos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Despliegue en Google Cloud Run](#despliegue-en-google-cloud-run)
- [Endpoints](#endpoints)
- [Reglas de Negocio](#reglas-de-negocio)
- [Tests](#tests)
- [Documentación API](#documentación-api)

## ✨ Características

- ✅ **Nivel 0**: Integración completa con API de Puntored (auth, getSuppliers, buy)
- ✅ **Nivel 1**: Persistencia de transacciones en PostgreSQL (Supabase)
- ✅ **Nivel 2**: API REST lista para consumo por frontend React
- ✅ Arquitectura Hexagonal por capas
- ✅ Validaciones de reglas de negocio
- ✅ Manejo de errores centralizado
- ✅ Documentación con Swagger/OpenAPI
- ✅ Tests unitarios
- ✅ Cliente HTTP reactivo con WebClient

## 🏗️ Arquitectura

Arquitectura Hexagonal (Puertos y Adaptadores) organizada por capas:

```
com.puntored.transactions_api/
├── domain/              # Capa de Dominio (Núcleo del negocio)
│   ├── model/           # Entidades y Value Objects
│   │   ├── Transaction.java
│   │   ├── PhoneNumber.java (VO)
│   │   ├── Amount.java (VO)
│   │   └── Supplier.java (VO)
│   ├── port/            # Interfaces (Puertos)
│   │   ├── PuntoredClientPort.java
│   │   └── TransactionRepositoryPort.java
│   ├── service/         # Servicios de Dominio
│   │   └── TransactionValidationService.java
│   └── exception/       # Excepciones de Dominio
│
├── application/         # Capa de Aplicación (Casos de Uso)
│   ├── usecase/
│   │   ├── AuthenticateUseCase.java
│   │   ├── GetSuppliersUseCase.java
│   │   ├── CreateRechargeUseCase.java
│   │   └── GetTransactionHistoryUseCase.java
│   └── dto/             # DTOs de aplicación
│
├── infrastructure/      # Capa de Infraestructura
│   ├── external/        # Cliente HTTP Puntored
│   │   └── PuntoredClient.java
│   ├── persistence/     # Repositorios JPA
│   │   ├── TransactionJpaRepository.java
│   │   └── TransactionRepositoryAdapter.java
│   └── config/          # Configuraciones
│
└── adapter/             # Capa de Adaptadores
    └── input/           # Controladores REST
        ├── AuthController.java
        ├── SupplierController.java
        ├── RechargeController.java
        ├── TransactionController.java
        └── dto/         # DTOs de API
```

### Flujo de una Recarga

```
Frontend → RechargeController → CreateRechargeUseCase → TransactionValidationService
                                         ↓
                                 PuntoredClient (WebClient)
                                         ↓
                                 TransactionRepository
                                         ↓
                                    PostgreSQL
```

## 🚀 Tecnologías

- **Java 17**
- **Spring Boot 3.5.7**
  - Spring Web
  - Spring Data JPA
  - Spring WebFlux (WebClient)
  - Spring Validation
- **PostgreSQL** (Supabase)
- **Lombok**
- **SpringDoc OpenAPI** (Swagger)
- **Maven**

## 📦 Requisitos

- Java 17 o superior
- Maven 3.9+
- PostgreSQL (o cuenta de Supabase)

## 🔧 Instalación

1. Clonar el repositorio:
```bash
git clone <repository-url>
cd transactions-api
```

2. Configurar la base de datos en `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://[host]:5432/[database]
    username: [usuario]
    password: [contraseña]
```

3. Compilar el proyecto:
```bash
./mvnw clean install
```

## ⚙️ Configuración

La aplicación soporta **dos perfiles** para separar configuraciones de desarrollo y producción:

### 🔧 Perfil de Desarrollo (`dev`)

**Uso:** Para desarrollo local con credenciales hardcodeadas.

Las credenciales ya están configuradas en `application-dev.yml` (NO commitear con credenciales reales).

CORS permite: `http://localhost:3000`, `http://localhost:5173`

### 🚀 Perfil de Producción (`prod`)

**Uso:** Para ambientes productivos con variables de entorno.

1. Copiar el archivo de ejemplo:
```bash
cp .env.example .env
```

2. Configurar las variables de entorno en `.env`:
```bash
# Base de datos
DB_URL=jdbc:postgresql://[host]:5432/[database]
DB_USERNAME=[usuario]
DB_PASSWORD=[contraseña]

# API de Puntored
PUNTORED_BASE_URL=https://[url-api-puntored]
PUNTORED_API_KEY=[tu-api-key]
PUNTORED_USER=[usuario]
PUNTORED_PASSWORD=[contraseña]

# CORS (separar múltiples dominios con comas)
ALLOWED_ORIGINS=https://tu-frontend.com
```

3. En servicios cloud (Railway, Render, Heroku, AWS, GCP):
   - Configurar las variables de entorno en el panel del servicio
   - **NO subir el archivo `.env` al repositorio**

### Variables de Entorno Disponibles

| Variable | Descripción | Requerido | Perfil |
|----------|-------------|-----------|--------|
| `DB_URL` | URL conexión PostgreSQL | Sí | prod |
| `DB_USERNAME` | Usuario base de datos | Sí | prod |
| `DB_PASSWORD` | Contraseña base de datos | Sí | prod |
| `DB_POOL_SIZE` | Tamaño pool conexiones | No (default: 20) | prod |
| `DB_MIN_IDLE` | Conexiones idle mínimas | No (default: 10) | prod |
| `PUNTORED_BASE_URL` | URL API Puntored | Sí | prod |
| `PUNTORED_API_KEY` | API Key de Puntored | Sí | prod |
| `PUNTORED_USER` | Usuario autenticación | Sí | prod |
| `PUNTORED_PASSWORD` | Contraseña autenticación | Sí | prod |
| `ALLOWED_ORIGINS` | Orígenes CORS permitidos | Sí | prod |

## ▶️ Ejecución

### Desarrollo Local (perfil `dev`)
```bash
# Con Maven Wrapper
./mvnw spring-boot:run

# O especificando el perfil explícitamente
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

La aplicación se ejecutará en `http://localhost:8080`

### Producción (perfil `prod`)

#### Opción 1: Con variables de entorno en el sistema
```bash
# 1. Exportar variables
export DB_URL="jdbc:postgresql://..."
export DB_USERNAME="..."
# ... (todas las variables)

# 2. Compilar
./mvnw clean package

# 3. Ejecutar con perfil prod
java -jar target/transactions-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

#### Opción 2: Con archivo .env (usando herramientas como `dotenv`)
```bash
# 1. Compilar
./mvnw clean package

# 2. Cargar .env y ejecutar
source .env && java -jar target/transactions-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

#### Opción 3: Docker
```bash
# Construir imagen
docker build -t puntored-api .

# Ejecutar con variables de entorno
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL="jdbc:postgresql://..." \
  -e DB_USERNAME="..." \
  -e DB_PASSWORD="..." \
  -e PUNTORED_BASE_URL="..." \
  -e PUNTORED_API_KEY="..." \
  -e PUNTORED_USER="..." \
  -e PUNTORED_PASSWORD="..." \
  -e ALLOWED_ORIGINS="https://tu-frontend.com" \
  puntored-api
```

## ☁️ Despliegue en Google Cloud Run

Esta API está preparada para desplegarse en Google Cloud Run usando **buildpacks de Java** (sin necesidad de Dockerfile).

### Prerrequisitos

1. **Cuenta de Google Cloud** con proyecto creado
2. **Repositorio** conectado a Google Cloud (GitHub, GitLab, Bitbucket o Cloud Source Repositories)
3. **APIs habilitadas**:
   - Cloud Run API
   - Cloud Build API
   - Secret Manager API (recomendado)

### Configuración Pre-Despliegue

#### 1. Habilitar APIs en Google Cloud Console

1. Ir a **APIs & Services > Library**
2. Habilitar:
   - Cloud Run API
   - Cloud Build API
   - Secret Manager API (recomendado para secretos)

#### 2. Configurar Secret Manager (Recomendado)

Para gestionar credenciales de forma segura:

1. Ir a **Secret Manager** en Google Cloud Console
2. Crear secretos para valores sensibles:
   - `db-password`
   - `puntored-api-key`
   - `puntored-password`
   - `supabase-jwt-secret`

#### 3. Verificar Configuración del Proyecto

El proyecto ya está configurado con:
- ✅ Puerto dinámico: `server.port: ${PORT:8080}` en `application-prod.yml`
- ✅ Perfil de producción: Usa variables de entorno
- ✅ Archivo `.gcloudignore`: Excluye archivos innecesarios del build

### Despliegue desde Cloud Run Console

#### Paso 1: Crear Nuevo Servicio

1. Ir a **Cloud Run > Create Service**
2. **Service name**: `transactions-api` o `puntored-transactions-api`
3. **Region**: Seleccionar región cercana (ej: `us-central1`, `us-east1`)

#### Paso 2: Configurar Despliegue

**Pestaña "Container"**:
- Seleccionar **"Deploy from source code"**
- **Repository**: Conectar tu repositorio (GitHub, GitLab, Bitbucket)
- **Branch**: `main` o `master`
- **Build Type**: **Buildpacks** (no Dockerfile)
- **Runtime**: **Java**
- **Java version**: **17**

**Build Configuration**:
- **Buildpack builder**: Dejar por defecto (Google Cloud detectará automáticamente Maven)
- **Build command**: (vacío, Maven se ejecuta automáticamente)
- **Output directory**: `target/` (donde Maven genera el JAR)

#### Paso 3: Configurar Variables de Entorno

**Pestaña "Variables & Secrets"**:

**Variables de entorno estándar**:
```
SPRING_PROFILES_ACTIVE=prod
PORT=8080
```

**Variables de entorno de la aplicación** (configurar todas las requeridas):
```
DB_URL=jdbc:postgresql://[HOST]:5432/postgres
DB_USERNAME=postgres.[PROJECT_REF]
DB_PASSWORD=[SECRET_VALUE o referencia a Secret Manager]
DB_POOL_SIZE=20
DB_MIN_IDLE=10
PUNTORED_BASE_URL=https://us-central1-puntored-dev.cloudfunctions.net/technicalTest-developer/api
PUNTORED_API_KEY=[SECRET_VALUE o referencia a Secret Manager]
PUNTORED_USER=[USER]
PUNTORED_PASSWORD=[SECRET_VALUE o referencia a Secret Manager]
ALLOWED_ORIGINS=https://tu-frontend.com
SUPABASE_JWT_SECRET=[SECRET_VALUE o referencia a Secret Manager]
SUPABASE_JWT_ISSUER=https://[PROJECT_REF].supabase.co/auth/v1
```

**Usar Secret Manager** (recomendado):
- Para valores sensibles, usar la opción **"Reference a secret"**
- Seleccionar el secreto creado en Secret Manager
- Cloud Run inyectará el valor automáticamente

#### Paso 4: Configurar Recursos y Escalado

**Pestaña "Container, Networking, Security"**:

**Container**:
- **CPU**: 1 (mínimo recomendado para Spring Boot)
- **Memory**: 512Mi (mínimo) o 1Gi (recomendado)
- **Timeout**: 300s (5 minutos)
- **Concurrency**: 80 (default, ajustar según carga)
- **Max instances**: 10 (ajustar según necesidades)
- **Min instances**: 0 (para ahorrar costos) o 1 (para evitar cold starts)

**Networking**:
- **Port**: 8080 (Cloud Run inyectará PORT automáticamente)
- **Allow unauthenticated invocations**: SÍ (si la API es pública) o NO (si requiere autenticación)

#### Paso 5: Configurar Conexión a Base de Datos

**Si la BD está en Supabase (externo)**:
- No requiere configuración adicional en Cloud Run
- Asegurar que las IPs de Cloud Run estén permitidas en Supabase (si hay restricciones de IP)

**Si la BD está en Cloud SQL**:
- **Pestaña "Connections"**: Conectar a instancia de Cloud SQL
- Cloud SQL Proxy se configurará automáticamente

#### Paso 6: Desplegar

1. Revisar todas las configuraciones
2. Click en **"Deploy"** o **"Create"**
3. Cloud Build iniciará automáticamente:
   - Clonará el repositorio
   - Detectará que es un proyecto Java/Maven
   - Ejecutará el build con buildpacks
   - Construirá la imagen
   - Desplegará en Cloud Run

### Monitorear el Despliegue

1. **Cloud Build > History**: Ver logs del build en tiempo real
2. Verificar que:
   - Maven compile correctamente
   - Buildpack detecte Java 17
   - Se genere el JAR ejecutable
   - La imagen se construya exitosamente

### Verificar el Despliegue

1. En **Cloud Run**, verificar que el servicio esté **Active**
2. Obtener la URL del servicio (formato: `https://transactions-api-[hash]-[region].a.run.app`)
3. Probar endpoint: `GET https://[URL]/api/auth`
4. Verificar logs en **Cloud Run > Logs**

### Actualizar el Servicio

Para desplegar una nueva versión:

1. Hacer push de cambios al repositorio
2. En **Cloud Run**, click en **"Edit & Deploy New Revision"**
3. Seleccionar el nuevo commit/branch
4. Click en **"Deploy"**
5. Cloud Build construirá y desplegará automáticamente

### Rollback

Si es necesario volver a una versión anterior:

1. En **Cloud Run > Revisions**
2. Seleccionar revisión anterior
3. Click en **"Manage Traffic"**
4. Asignar 100% del tráfico a la revisión anterior

### Configuración Post-Despliegue

#### Dominio Personalizado (Opcional)

1. **Cloud Run > Manage Custom Domains**
2. Mapear dominio a la URL del servicio
3. SSL se configura automáticamente

#### Monitoreo

1. **Cloud Monitoring**: Habilitar métricas automáticas
2. **Cloud Logging**: Verificar que los logs se estén generando
3. Configurar alertas para errores críticos

### Notas Importantes

- ✅ Cloud Run usa buildpacks automáticamente cuando detecta `pom.xml` (Maven)
- ✅ El puerto se inyecta automáticamente via variable `PORT`
- ✅ Los secretos deben gestionarse via Secret Manager para producción
- ✅ El perfil `prod` debe activarse con `SPRING_PROFILES_ACTIVE=prod`
- ✅ Cloud Run escala a cero cuando no hay tráfico (ahorro de costos)
- ⚠️ Los cold starts pueden tomar 5-10 segundos (considerar `min instances = 1`)

### Checklist Pre-Despliegue

- [ ] Variables de entorno configuradas (o secretos en Secret Manager)
- [ ] Base de datos accesible desde Cloud Run (IPs permitidas en Supabase)
- [ ] Repositorio conectado a Google Cloud
- [ ] APIs habilitadas (Cloud Run, Cloud Build)
- [ ] Credenciales de Puntored API verificadas
- [ ] CORS configurado con dominios correctos
- [ ] JWT secret de Supabase configurado

## 🌐 Endpoints

### Autenticación
```http
POST /api/auth
```
Obtiene un token Bearer para autenticación con Puntored.

**Response:**
```json
{
  "token": "Bearer e8797850-95bb-4ca1-ac52-c99dd3c3cbad"
}
```

### Proveedores
```http
GET /api/suppliers
Authorization: Bearer {token}
```
Lista los proveedores de recargas disponibles.

**Response:**
```json
[
  { "id": "8753", "name": "Claro" },
  { "id": "9773", "name": "Movistar" },
  { "id": "3398", "name": "Tigo" },
  { "id": "4689", "name": "WOM" }
]
```

### Crear Recarga
```http
POST /api/recharges
Authorization: Bearer {token}
X-User-Id: user@example.com
Content-Type: application/json

{
  "phoneNumber": "3001234567",
  "amount": 10000,
  "supplierId": "8753"
}
```

**Response (201 Created):**
```json
{
  "transactionId": 1,
  "phoneNumber": "3001234567",
  "amount": 10000,
  "supplierId": "8753",
  "supplierName": "Claro",
  "status": "COMPLETED",
  "ticket": "TKT-ABC123...",
  "createdAt": "2024-11-18T17:30:00"
}
```

### Historial de Transacciones
```http
GET /api/transactions
X-User-Id: user@example.com (opcional)
```
Lista transacciones. Si se proporciona `X-User-Id`, filtra por usuario.

```http
GET /api/transactions/{id}
```
Obtiene una transacción por ID.

```http
GET /api/transactions/phone/{phoneNumber}
```
Obtiene transacciones por número de teléfono.

```http
GET /api/transactions/user/{userId}
```
Obtiene transacciones de un usuario específico.

**Response:**
```json
[
  {
    "id": 1,
    "phoneNumber": "3001234567",
    "amount": 10000,
    "supplierId": "8753",
    "supplierName": "Claro",
    "status": "COMPLETED",
    "ticket": "TKT-ABC123...",
    "errorMessage": null,
    "createdAt": "2024-11-18T17:30:00",
    "updatedAt": "2024-11-18T17:30:05"
  }
]
```

## 📏 Reglas de Negocio

### Número de Teléfono
- ✅ Debe iniciar con **3**
- ✅ Longitud exacta de **10 caracteres**
- ✅ Solo valores **numéricos**

**Ejemplos válidos:** `3001234567`, `3101234567`, `3501234567`

**Ejemplos inválidos:** `2001234567` (no inicia con 3), `300123456` (9 dígitos), `300123456a` (contiene letra)

### Monto
- ✅ Mínimo: **1,000**
- ✅ Máximo: **100,000**

**Ejemplos válidos:** `1000`, `50000`, `100000`

**Ejemplos inválidos:** `999` (menor al mínimo), `100001` (mayor al máximo)

### Estados de Transacción
- **PENDING**: Transacción creada pero no procesada
- **COMPLETED**: Transacción exitosa
- **FAILED**: Transacción fallida

## 🧪 Tests

Ejecutar tests unitarios:
```bash
./mvnw test
```

Tests incluidos:
- ✅ `PhoneNumberTest` - Validación de números de teléfono
- ✅ `AmountTest` - Validación de montos
- ✅ `SupplierTest` - Validación de proveedores
- ✅ `TransactionValidationServiceTest` - Servicio de validación

## 📚 Documentación API

### Swagger UI
Accede a la documentación interactiva en:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON
Documentación en formato JSON:
```
http://localhost:8080/api-docs
```

## 🗄️ Base de Datos

### Tabla: transactions

```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(10) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    supplier_id VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    ticket TEXT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## 🔐 Seguridad

### Gestión de Credenciales
- ✅ **Perfiles separados**: Credenciales en `application-dev.yml` (dev) y variables de entorno (prod)
- ✅ **Variables de entorno**: En producción NO se hardcodean credenciales
- ✅ **Archivo .env protegido**: Incluido en `.gitignore` para evitar commits accidentales
- ✅ **Sin logs sensibles**: No se registran API keys, tokens o passwords en logs

### CORS
- ✅ **Orígenes específicos por perfil**:
  - **Dev**: `http://localhost:3000`, `http://localhost:5173`
  - **Prod**: Solo dominios configurados en `ALLOWED_ORIGINS`
- ✅ **Sin wildcard (`*`)** en producción

### Token Caching
- ✅ **Thread-safe**: Token cacheado con `AtomicReference` para concurrencia segura
- ✅ **Renovación automática**: Se renueva cuando expira o está vacío

### Validaciones
- ✅ **Múltiples capas**:
  - Bean Validation (`@Valid`, `@NotNull`, etc.)
  - Domain Validation (Value Objects)
  - Business Rules (TransactionValidationService)

### Recomendaciones Adicionales para Producción
- 🔒 Usar HTTPS en todos los endpoints
- 🔒 Implementar rate limiting con Spring Cloud Gateway o Resilience4j
- 🔒 Habilitar actuadores de Spring Boot con autenticación
- 🔒 Usar secretos gestionados (AWS Secrets Manager, GCP Secret Manager)
- 🔒 Monitoreo y alertas con herramientas como Prometheus + Grafana

## 📝 Notas para el Frontend

### CORS
La API está configurada con CORS específico por perfil. Los orígenes permitidos son:
- **Desarrollo**: `http://localhost:3000`, `http://localhost:5173`
- **Producción**: Configurar en variable `ALLOWED_ORIGINS`

### Flujo de Autenticación
1. Frontend obtiene token: `POST /api/auth`
2. Frontend guarda el token
3. Frontend incluye token en header `Authorization: Bearer {token}` para todas las demás peticiones

### Manejo de Errores
Todos los errores retornan un formato estándar:
```json
{
  "message": "Descripción del error",
  "errors": ["detalle 1", "detalle 2"],
  "status": 400,
  "timestamp": "2024-11-18T17:30:00"
}
```

## 👥 Autor

Puntored Development Team

## 📄 Licencia

Este proyecto es privado y confidencial.

