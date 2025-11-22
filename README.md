# Puntored Transactions API - Backend

API REST para el portal transaccional de recargas móviles de Puntored. Implementa arquitectura hexagonal con Spring Boot, autenticación JWT y persistencia en PostgreSQL.

## 📋 Tabla de Contenido

- [🚀 Características](#-características)
- [📋 Requisitos](#-requisitos)
- [🔧 Instalación](#-instalación)
- [🏗️ Build para Producción](#️-build-para-producción)
- [📁 Estructura del Proyecto](#-estructura-del-proyecto)
- [🔐 Autenticación](#-autenticación)
- [🛠️ Tecnologías](#️-tecnologías)
- [🔒 Seguridad](#-seguridad)
- [🐛 Scripts Disponibles](#-scripts-disponibles)
- [🧪 Pruebas Automatizadas](#-pruebas-automatizadas)
- [📝 Notas Importantes](#-notas-importantes)
- [🚀 Despliegue](#-despliegue)
- [📚 Documentación API](#-documentación-api)

## 🚀 Características

- ✅ Integración completa con API de Puntored (auth, getSuppliers, buy)
- ✅ Persistencia de transacciones en PostgreSQL (Supabase)
- ✅ Autenticación JWT con Supabase
- ✅ Arquitectura hexagonal (Puertos y Adaptadores)
- ✅ Validaciones de negocio con Value Objects
- ✅ Manejo de errores centralizado
- ✅ Documentación interactiva con Swagger/OpenAPI
- ✅ 45 tests automatizados (100% pasando)
- ✅ Logging estructurado en JSON para Cloud Logging
- ✅ Retry automático en llamadas externas

## 📋 Requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL (o cuenta de Supabase)
- Credenciales de API de Puntored

## 🔧 Instalación

**Clonar el repositorio:**
```bash
git clone https://github.com/carlosloreto/puntored-transactions-api.git
cd puntored-transactions-api
```

**Configurar variables de entorno:**

Copia el archivo `.env.example` a `.env.local`:
```bash
cp .env.example .env.local
```

Edita `.env.local` con tus credenciales:
```properties
# Base de datos
DB_URL=jdbc:postgresql://...
DB_USERNAME=postgres
DB_PASSWORD=tu-password

# API Puntored
PUNTORED_BASE_URL=https://api.puntored.com
PUNTORED_API_KEY=tu-api-key
PUNTORED_USER=tu-usuario
PUNTORED_PASSWORD=tu-password

# Supabase JWT
SUPABASE_JWT_SECRET=tu-jwt-secret
SUPABASE_JWT_ISSUER=https://tu-proyecto.supabase.co/auth/v1
```

**Iniciar la aplicación:**
```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`

## 🏗️ Build para Producción

**Generar JAR ejecutable:**
```bash
./mvnw clean package
```

El JAR se generará en `target/transactions-api-0.0.1-SNAPSHOT.jar`

**Ejecutar el JAR:**
```bash
java -jar target/transactions-api-0.0.1-SNAPSHOT.jar
```

## 📁 Estructura del Proyecto

```
src/main/java/com/puntored/transactions_api/
├── domain/                    # Capa de Dominio (núcleo del negocio)
│   ├── model/                 # Entidades y Value Objects
│   ├── port/                  # Interfaces (Puertos)
│   ├── service/               # Servicios de Dominio
│   └── exception/             # Excepciones de Dominio
├── application/               # Casos de Uso
│   ├── usecase/               # Lógica de aplicación
│   └── dto/                   # DTOs de aplicación
├── adapter/                   # Adaptadores (entrada/salida)
│   └── input/                 # Controllers REST
│       ├── dto/               # DTOs de API
│       └── exception/         # Manejo de errores
└── infrastructure/            # Infraestructura
    ├── config/                # Configuración (CORS, WebClient, etc.)
    ├── external/              # Cliente HTTP Puntored
    ├── persistence/           # Repositorios JPA
    ├── security/              # JWT Validation
    └── logging/               # Logging estructurado
```

## 🔐 Autenticación

El proyecto utiliza **Supabase JWT** para autenticación:

- **JWT Validation:** Todos los endpoints protegidos validan el token JWT
- **Issuer Verification:** Previene tokens falsificados
- **User Ownership:** Los usuarios solo ven sus propias transacciones

**Flujo de Autenticación:**
```
Frontend → Supabase Login → JWT Token → Backend API (Bearer Token)
```

## 🛠️ Tecnologías

- **Spring Boot 3.3.5** - Framework principal
- **Java 17** - Lenguaje
- **PostgreSQL** - Base de datos (Supabase)
- **WebClient** - Cliente HTTP reactivo
- **JJWT 0.12.3** - Validación de JWT
- **JUnit 5 + Mockito** - Testing
- **Logstash Logback Encoder** - Logging JSON
- **SpringDoc OpenAPI** - Documentación API

## 🔒 Seguridad

- ✅ Autenticación JWT con validación de issuer
- ✅ Variables de entorno para credenciales
- ✅ Endpoints de desarrollo aislados con `@Profile("dev")`
- ✅ CORS configurado por perfil
- ✅ Validación de ownership en transacciones
- ✅ Logging de eventos de seguridad

## 🐛 Scripts Disponibles

```bash
./mvnw spring-boot:run    # Inicia servidor de desarrollo
./mvnw clean package      # Genera JAR de producción
./mvnw test               # Ejecuta todas las pruebas
./mvnw clean verify       # Build completo + tests
```

## 🧪 Pruebas Automatizadas

El proyecto cuenta con **45 tests automatizados** (100% pasando):

**Ejecutar pruebas:**
```bash
./mvnw test
```

**Cobertura:**
- **Unit Tests (31):** Value Objects, Services, Use Cases
- **Integration Tests (14):** Controllers con MockMvc
- **Context Test (1):** Verificación de contexto Spring

**Tipos de pruebas:**
- ✅ Validaciones de negocio (PhoneNumber, Amount)
- ✅ Lógica de casos de uso
- ✅ Endpoints REST con autenticación
- ✅ Manejo de errores y excepciones

## 📝 Notas Importantes

**Perfiles de Spring:**
- `dev` - Desarrollo local (usa `application-dev.yml`)
- `prod` - Producción (usa `application-prod.yml` + env vars)
- `test` - Tests (usa `application-test.yml`)

**Variables de Entorno:**
- `.env.local` - Desarrollo (NO subir a git)
- `.env.example` - Plantilla (SÍ subir a git)
- Producción: Configurar en Cloud Run o servidor

**Logging:**
- Desarrollo: Logs en consola con formato legible
- Producción: JSON estructurado para Cloud Logging

## 🚀 Despliegue

### Google Cloud Run (Recomendado) ⭐

Este proyecto está optimizado para Google Cloud Run con buildpacks.

**Deploy con gcloud:**
```bash
gcloud run deploy puntored-transactions-api \
  --source . \
  --region southamerica-east1 \
  --allow-unauthenticated \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod" \
  --set-secrets="DB_URL=db-url:latest,DB_USERNAME=db-username:latest,..."
```

**Variables de entorno requeridas:**
- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `PUNTORED_BASE_URL`, `PUNTORED_API_KEY`, `PUNTORED_USER`, `PUNTORED_PASSWORD`
- `SUPABASE_JWT_SECRET`, `SUPABASE_JWT_ISSUER`
- `ALLOWED_ORIGINS` (URLs del frontend)

**Deploy automático:**
Configura un trigger en Cloud Build para deploy automático en cada push a `main`.

### Otras Plataformas

**Heroku:**
```bash
heroku create puntored-api
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set DB_URL=...
git push heroku main
```

**Docker:**
```bash
docker build -t puntored-api .
docker run -p 8080:8080 --env-file .env.local puntored-api
```

## 📚 Documentación API

### Swagger UI (Interactivo)

Una vez ejecutada la aplicación, accede a:
```
http://localhost:8080/swagger-ui.html
```

### Endpoints Principales

**Autenticación:**
- `POST /api/auth` - Autenticación con Puntored (deprecated, usar Supabase)

**Proveedores:**
- `GET /api/suppliers` - Listar proveedores disponibles (requiere JWT)

**Recargas:**
- `POST /api/recharges` - Crear nueva recarga (requiere JWT)

**Transacciones:**
- `GET /api/transactions` - Historial de transacciones (requiere JWT)
- `GET /api/transactions?phoneNumber={phone}` - Filtrar por teléfono
- `GET /api/transactions?userId={userId}` - Filtrar por usuario

**Desarrollo (solo perfil `dev`):**
- `POST /api/dev/generate-jwt` - Generar JWT de prueba

### Ejemplo de Uso

**Crear una recarga:**
```bash
curl -X POST http://localhost:8080/api/recharges \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "3001234567",
    "amount": 10000,
    "supplierId": "8753",
    "userId": "user@example.com"
  }'
```

## 🔍 Características Técnicas Destacadas

- ✅ **Arquitectura Hexagonal:** Separación clara de dominio e infraestructura
- ✅ **Value Objects:** Validaciones encapsuladas en `PhoneNumber`, `Amount`
- ✅ **Retry Logic:** Reintentos automáticos en caso de token expirado (401)
- ✅ **Thread-Safe Token Caching:** `AtomicReference` para cache de tokens
- ✅ **Timeouts Configurables:** Diferentes timeouts para auth/suppliers (10s) y buy (30s)
- ✅ **Logging Estructurado:** Metadata contextual en todos los logs
- ✅ **Profile-Based Security:** Endpoints de desarrollo aislados con `@Profile("dev")`

## 📄 Licencia

Este proyecto es propiedad de Puntored.

## 📧 Contacto

Para soporte o consultas, contacta al equipo de desarrollo de Puntored.

---

Desarrollado con ❤️ por el equipo de Puntored
