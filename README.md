# Inventario — Sistema de Gestión de Inventario para Taller Mecánico

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![Build](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL_15-4169E1?logo=postgresql&logoColor=white)

**Equipo:** 4 integrantes  
**Arquitectura:** Monolito modular  
**Frontend del proyecto:** https://github.com/BeckanG728/inventario-front
---

## Descripción

Inventario es un backend de gestión de inventario para talleres mecánicos, construido como un **monolito modular** sobre
una única base de datos PostgreSQL. Cubre el ciclo completo de inventario: catálogo de productos, categorías y
proveedores, registro de entradas, consumos y ajustes de stock, configuración de stock mínimo, alertas de bajo stock,
reportes, exportación a PDF/Excel, dashboard y estadísticas de movimientos.

Permite a un usuario autenticado gestionar productos y proveedores, registrar entradas y consumos de forma atómica,
consultar existencias, recibir alertas de bajo stock y generar reportes de solo lectura.

---

## Módulos

| Módulo                 | Código | Responsabilidad                                                | Depende de            |
|------------------------|--------|----------------------------------------------------------------|-----------------------|
| Seguridad              | SEC    | Registro, login, emisión y validación de JWT, roles y permisos | —                     |
| Gestión Productos      | M100   | Catálogo de productos, categorías y proveedores                | —                     |
| Gestión Inventario     | M200   | Entradas, consumos atómicos, ajustes y consulta de existencias | M100                  |
| Control Abastecimiento | M300   | Stock mínimo y alertas de bajo stock                           | M100                  |
| Consultas Reportes     | M400   | Reportes, exportación, dashboard y estadísticas                | M100, M200, M300, SEC |

Una única instancia del filtro `JwtAuthFilter` intercepta todas las peticiones no públicas para autenticar y autorizar.

---

## Arquitectura

```
Cliente
  │
  ▼
Aplicación Spring Boot (puerto ${SERVER_PORT})
  │  Prefijo de API: /api (sin versionado)
  │  Filtro JWT (JwtAuthFilter) en peticiones no públicas
  │  Validación fail-fast de variables de entorno al arrancar
  │
  ├── SEC        Seguridad, JWT y roles (ADMIN, JEFE_TALLER, MECANICO)
  ├── M100       Productos, categorías y proveedores
  ├── M200       Entradas, consumos y ajustes de stock
  ├── M300       Stock mínimo y alertas de bajo stock
  └── M400       Reportes, dashboard y estadísticas
```

Los módulos ejecutan en el mismo proceso y comparten la misma base de datos `inventario_taller`. La comunicación entre
módulos es mediante llamadas directas a servicios, sin llamadas HTTP entre ellos.

---

## Stack tecnológico

| Capa           | Tecnología                                                                   |
|----------------|------------------------------------------------------------------------------|
| Lenguaje       | Java 21                                                                      |
| Framework      | Spring Boot 4.1.2 (WebMVC, Data JPA, Security, Actuator, Validation, Flyway) |
| Seguridad      | Spring Security 6 + JWT (HS256)                                              |
| Persistencia   | Spring Data JPA + PostgreSQL 15                                              |
| Migraciones    | Flyway (`ddl-auto: validate`)                                                |
| Acceso a datos | HikariCP, `open-in-view: false`                                              |
| Build          | Maven + Maven Wrapper                                                        |
| Documentación  | Swagger UI / OpenAPI 3.0 (`/swagger-ui.html`)                                |
| Exportación    | Apache POI, OpenPDF                                                          |
| Testing        | JUnit 5 + Mockito                                                            |
| Mapeo          | MapStruct + Lombok                                                           |

---

## Variables de entorno

Copiar `.env.example` a `.env` y completar los valores antes de levantar la aplicación.

```bash
cp .env.example .env
```

| Variable                  | Descripción                                                      | Obligatoria |
|---------------------------|------------------------------------------------------------------|:-----------:|
| `POSTGRES_URL`            | URL de conexión a PostgreSQL                                     |     Sí      |
| `POSTGRES_USER`           | Usuario de PostgreSQL                                            |     Sí      |
| `POSTGRES_PASSWORD`       | Contraseña de PostgreSQL                                         |     Sí      |
| `SERVER_PORT`             | Puerto HTTP de la aplicación                                     |     Sí      |
| `JWT_SECRET`              | Clave secreta para firmar y validar JWT (HS256). Mínimo 32 bytes |     Sí      |
| `JWT_EXPIRACION_SEGUNDOS` | Validez del token en segundos                                    |     Sí      |
| `ADMIN_USERNAME`          | Usuario del semilla ADMIN                                        |     Sí      |
| `ADMIN_PASSWORD`          | Contraseña del semilla ADMIN                                     |     Sí      |
| `ALERTAS_CRON`            | Expresión cron de la revisión programada de bajo stock           |     Sí      |
| `SPRING_PROFILES_ACTIVE`  | Perfil activo de Spring                                          |     No      |
| `SEED_DEMO_ENABLED`       | Habilitar datos de prueba (perfil dev)                           |     No      |
| `SEED_DEMO_*`             | Cantidades y parámetros del seed de demostración                 |     No      |

> **Generar `JWT_SECRET`:** `openssl rand -base64 32`  
> El archivo `.env` está en `.gitignore` y nunca debe subirse al repositorio. En producción las variables se definen en
> el entorno de despliegue.

---

## Puesta en marcha

### Requisitos previos

- Java 21 (JDK)
- Maven (o el wrapper `./mvnw`)
- PostgreSQL 15 con una base de datos llamada `inventario_taller`
- Archivo `.env` configurado (ver sección anterior)

### Levantar la aplicación

```bash
./mvnw spring-boot:run
```

O, si se tiene Maven instalado:

```bash
mvn spring-boot:run
```

### Ejecutar pruebas

```bash
./mvnw test
```

---

## Endpoints disponibles

Todas las rutas usan el prefijo `/api`. Las rutas protegidas (`🔒`) requieren la cabecera
`Authorization: Bearer <token>`.

### Roles

| Rol           | Permisos                                                                                          |
|---------------|---------------------------------------------------------------------------------------------------|
| `ADMIN`       | Puede registrar usuarios (`POST /api/auth/register`). Rol semilla, no se asigna por registro.     |
| `JEFE_TALLER` | Gestiona productos, categorías y proveedores; registra entradas y ajustes; configura stock mínimo |
| `MECANICO`    | Registra consumos. No puede modificar los catálogos de productos, categorías ni proveedores.      |

### Autenticación — SEC

| Método | Ruta                 | Acceso  | Descripción                                        |
|--------|----------------------|---------|----------------------------------------------------|
| POST   | `/api/auth/register` | ADMIN   | Registra un usuario con rol MECANICO o JEFE_TALLER |
| POST   | `/api/auth/login`    | Público | Login, devuelve un JWT firmado con HS256           |

### Productos `🔒` — M100

| Método | Ruta                             | Acceso      | Descripción                             |
|--------|----------------------------------|-------------|-----------------------------------------|
| GET    | `/api/productos`                 | Autenticado | Lista paginada de productos             |
| POST   | `/api/productos`                 | JEFE_TALLER | Crea un producto (stock inicial 0)      |
| PUT    | `/api/productos/{id}`            | JEFE_TALLER | Actualiza un producto existente         |
| PATCH  | `/api/productos/{id}/desactivar` | JEFE_TALLER | Desactivación lógica (`activo = false`) |

### Categorías y Proveedores `🔒` — M100

| Método | Ruta               | Acceso      | Descripción        |
|--------|--------------------|-------------|--------------------|
| POST   | `/api/categorias`  | JEFE_TALLER | Crea una categoría |
| POST   | `/api/proveedores` | JEFE_TALLER | Crea un proveedor  |

### Movimientos de Inventario `🔒` — M200

| Método | Ruta                       | Acceso                | Descripción                              |
|--------|----------------------------|-----------------------|------------------------------------------|
| POST   | `/api/movimientos/entrada` | JEFE_TALLER           | Registra una entrada de stock            |
| POST   | `/api/consumos`            | MECANICO, JEFE_TALLER | Registra un consumo atómico de stock     |
| POST   | `/api/movimientos/ajuste`  | JEFE_TALLER           | Registra un ajuste de inventario         |
| GET    | `/api/existencias`         | Autenticado           | Consulta existencias por IDs de producto |

### Stock Mínimo y Alertas `🔒` — M300

| Método | Ruta                               | Acceso      | Descripción                      |
|--------|------------------------------------|-------------|----------------------------------|
| PUT    | `/api/productos/{id}/stock-minimo` | JEFE_TALLER | Configura el stock mínimo        |
| GET    | `/api/alertas`                     | Autenticado | Lista de productos en bajo stock |

### Reportes y Dashboard `🔒` — M400

| Método | Ruta                             | Acceso                | Descripción                                  |
|--------|----------------------------------|-----------------------|----------------------------------------------|
| GET    | `/api/reportes/movimientos`      | Autenticado           | Reporte de movimientos por periodo           |
| GET    | `/api/reportes/consumo`          | Autenticado           | Reporte de consumo por periodo               |
| GET    | `/api/reportes/bajo-stock`       | Autenticado           | Reporte de bajo stock agrupado por proveedor |
| GET    | `/api/reportes/valor-inventario` | Autenticado           | Valor estimado del inventario                |
| GET    | `/api/dashboard`                 | JEFE_TALLER, MECANICO | Resumen ejecutivo y KPIs                     |
| GET    | `/api/estadisticas`              | Autenticado           | Series temporales de movimientos             |

### Documentación interactiva (Swagger UI)

La aplicación publica Swagger UI con todos sus endpoints, ejemplos de petición/respuesta y los códigos de error
posibles:

- UI interactiva: `http://localhost:${SERVER_PORT}/swagger-ui.html`

Los endpoints protegidos aparecen con el candado `🔒`. Para probarlos desde la propia UI:

1. Obtener un token con `POST /api/auth/login` (o registrarse si se tiene rol ADMIN en `POST /api/auth/register`).
2. Pulsar **Authorize** y pegar el token (Swagger UI añade el prefijo `Bearer ` automáticamente).
3. Ejecutar la operación: cada una documenta sus parámetros de ejemplo y las respuestas posibles (`200`, `400`, `401`,
   `403`, `404`, `409`, `500`).

---

## Seguridad y comunicación entre módulos

### Autenticación

| Tipo                 | Mecanismo                                                                                                                                                                                                 |
|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Peticiones externas  | `Authorization: Bearer <token>`. El filtro `JwtAuthFilter` valida la firma HS256 con `JWT_SECRET`, lee el claim `sub`, carga al usuario desde la base de datos y usa su rol actual para decidir el acceso |
| Endpoints públicos   | `POST /api/auth/login`. En este endpoint un token inválido o ausente no bloquea la petición                                                                                                               |
| Registro de usuarios | `POST /api/auth/register` requiere un token de un usuario con rol `ADMIN`                                                                                                                                 |

### Roles y permisos

- `ADMIN`: único rol que puede invocar `POST /api/auth/register`. El rol ADMIN no se asigna por registro; se inserta
  como usuario seed al arrancar.
- `JEFE_TALLER`: administra productos, categorías y proveedores; registra entradas y ajustes; configura el stock mínimo.
- `MECANICO`: registra consumos. No puede modificar los catálogos (endpoints `POST /api/productos`,
  `POST /api/categorias`, `POST /api/proveedores` devuelven `403 FORBIDDEN`).

### Comunicación entre módulos

Los módulos se ejecutan en el mismo proceso y se comunican mediante llamadas directas a sus servicios, sin llamadas HTTP
entre ellos.

| Módulo que llama | Módulos a los que llama | Propósito                                                                  |
|------------------|-------------------------|----------------------------------------------------------------------------|
| M400             | M100, M200, M300, SEC   | Reportes, dashboard y estadísticas                                         |
| M300             | M100                    | Leer productos activos y su proveedor principal                            |
| M200             | M100                    | Validar productos y proveedores; actualizar stock y costo de última compra |
| SEC, M100        | —                       | —                                                                          |

---

## Contrato de errores

Todos los endpoints de la API (`/api`) devuelven los errores con la siguiente estructura:

```json
{
  "error": "VALIDATION_ERROR",
  "message": "La petición contiene campos no válidos",
  "details": [
    {
      "field": "year",
      "reason": "debe estar entre 1990 y 2027"
    }
  ],
  "endpoint": "/api/movimientos/entrada",
  "timestamp": "2026-10-05T10:30:00Z"
}
```

`details` solo aparece cuando hay detalles por campo de validación.

| HTTP | Código                                                                                      | Cuándo                                                                                            |
|------|---------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| 400  | `VALIDATION_ERROR`, `INVALID_ROLE`                                                          | Algún campo no cumple las validaciones; rol inexistente o intento de asignar `ADMIN` por registro |
| 401  | `UNAUTHORIZED`, `TOKEN_INVALID`, `TOKEN_EXPIRED`, `ACCOUNT_DISABLED`, `INVALID_CREDENTIALS` | Falta el token, no es válido, ha caducado, cuenta desactivada o credenciales incorrectas          |
| 403  | `FORBIDDEN`                                                                                 | El rol del usuario no tiene permiso                                                               |
| 404  | `—`                                                                                         | El recurso no existe                                                                              |
| 409  | `USER_ALREADY_EXISTS`, `STOCK_INSUFICIENTE`                                                 | Conflicto con el estado actual (usuario duplicado, stock insuficiente)                            |
| 500  | `—`                                                                                         | Error interno                                                                                     |

---

<div align="center">

![Backend Inventario](https://img.shields.io/badge/Inventario-Taller_Mecánico-4A90D9?style=for-the-badge)

</div>
