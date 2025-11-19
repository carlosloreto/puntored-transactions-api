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
| `SPRING_PROFILES_ACTIVE` | Perfil activo de Spring Boot | Sí (para prod) | prod |
| `PORT` | Puerto del servidor (Cloud Run lo proporciona automáticamente) | No (default: 8080) | prod |
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
| `SUPABASE_JWT_SECRET` | JWT Secret de Supabase | Sí | prod |
| `SUPABASE_JWT_ISSUER` | JWT Issuer de Supabase | Sí | prod |

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

#### Opción 4: Google Cloud Run
La aplicación está configurada para usar automáticamente el puerto proporcionado por Cloud Run.

**Pasos para desplegar:**

1. **Conectar el repositorio a Cloud Run:**
   - En Google Cloud Console, ve a Cloud Run
   - Crea un nuevo servicio
   - Selecciona "Deploy from source repository"
   - Conecta tu repositorio de GitHub

2. **Configurar variables de entorno en Cloud Run:**
   En la sección "Variables y secretos", agrega todas las siguientes variables:

   | Variable | Valor |
   |----------|-------|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DB_URL` | `jdbc:postgresql://[host]:5432/[database]` |
   | `DB_USERNAME` | `[usuario]` |
   | `DB_PASSWORD` | `[contraseña]` |
   | `PUNTORED_BASE_URL` | `https://[url-api-puntored]` |
   | `PUNTORED_API_KEY` | `[tu-api-key]` |
   | `PUNTORED_USER` | `[usuario]` |
   | `PUNTORED_PASSWORD` | `[contraseña]` |
   | `ALLOWED_ORIGINS` | `https://tu-frontend.com` |
   | `SUPABASE_JWT_SECRET` | `[jwt-secret]` |
   | `SUPABASE_JWT_ISSUER` | `https://[proyecto].supabase.co/auth/v1` |

3. **Configuración del servicio:**
   - **Región:** Selecciona la región deseada (ej: `southamerica-east1`)
   - **Autenticación:** Permite solicitudes no autenticadas (si es necesario)
   - **Puerto:** La aplicación usa automáticamente la variable `PORT` (no es necesario configurarlo)
   - **Timeout:** Aumenta a 300 segundos si es necesario
   - **Memoria:** Mínimo 512 MiB recomendado

4. **Desplegar:**
   - Haz clic en "Deploy"
   - Cloud Run construirá la imagen automáticamente usando buildpacks
   - El servicio estará disponible en la URL proporcionada

**Nota importante:** La aplicación está configurada para usar `server.port=${PORT:8080}` en el perfil de producción, lo que permite que Cloud Run asigne el puerto automáticamente.

### 🔧 Troubleshooting: Error "container failed to start" en Cloud Run

Si ves el error `The user-provided container failed to start and listen on the port`, sigue estos pasos:

#### 1. Verificar Variables de Entorno
Asegúrate de que **TODAS** estas variables estén configuradas en Cloud Run:

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://[host]:5432/[database]
DB_USERNAME=[usuario]
DB_PASSWORD=[contraseña]
PUNTORED_BASE_URL=https://[url-api-puntored]
PUNTORED_API_KEY=[tu-api-key]
PUNTORED_USER=[usuario]
PUNTORED_PASSWORD=[contraseña]
ALLOWED_ORIGINS=https://tu-frontend.com
SUPABASE_JWT_SECRET=[jwt-secret]
SUPABASE_JWT_ISSUER=https://[proyecto].supabase.co/auth/v1
```

**⚠️ CRÍTICO:** Si falta `SPRING_PROFILES_ACTIVE=prod`, la aplicación usará el perfil `dev` por defecto, que requiere `application-dev.yml` (que no debe estar en producción).

#### 2. Revisar Logs de Cloud Run
1. Ve a [Google Cloud Console > Cloud Run](https://console.cloud.google.com/run)
2. Selecciona tu servicio `puntored-transactions-api`
3. Haz clic en "Logs" para ver los logs de la aplicación
4. Busca errores como:
   - `Failed to bind properties`
   - `Could not resolve placeholder`
   - `Connection refused` (base de datos)
   - `Application run failed`

#### 3. Verificar Conexión a Base de Datos
- Asegúrate de que la URL de la base de datos sea correcta
- Verifica que la base de datos permita conexiones desde Cloud Run (IPs de Google Cloud)
- Si usas Supabase, verifica que el pooler esté habilitado

#### 4. Verificar Timeout y Memoria
En la configuración del servicio:
- **Timeout:** Aumenta a 300 segundos (5 minutos)
- **Memoria:** Mínimo 512 MiB (recomendado 1 GiB para aplicaciones Spring Boot)
- **CPU:** Al menos 1 CPU

#### 5. Probar Localmente con Variables de Entorno
Antes de desplegar, prueba localmente con las mismas variables:

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL="jdbc:postgresql://..."
export DB_USERNAME="..."
# ... (todas las variables)

./mvnw spring-boot:run
```

Si funciona localmente pero no en Cloud Run, el problema es la configuración de Cloud Run.

#### 6. Verificar que el Build se Complete Correctamente
En los logs de Cloud Build, verifica que:
- El buildpack detecte correctamente la aplicación Java
- La compilación Maven se complete sin errores
- La imagen se construya correctamente

#### 7. Comandos Útiles para Diagnóstico
```bash
# Ver logs en tiempo real
gcloud run services logs read puntored-transactions-api --region=southamerica-east1 --limit=50

# Ver detalles del servicio
gcloud run services describe puntored-transactions-api --region=southamerica-east1

# Ver revisiones fallidas
gcloud run revisions list --service=puntored-transactions-api --region=southamerica-east1
```

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

