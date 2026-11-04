# Configuración y especificaciones técnicas — Sistema de Gestión de Inventario

Este documento reúne configuraciones, decisiones arquitectónicas y mecanismos técnicos identificados en el sistema:
JWT, variables de entorno, zona horaria, tareas programadas, transacciones, concurrencia, persistencia y generación
de identificadores.

Las políticas de seguridad se documentan en [POLITICAS_DE_SEGURIDAD.md](POLITICAS_DE_SEGURIDAD.md); los contratos de
API en [CATALOGO_ENDPOINTS.md](CATALOGO_ENDPOINTS.md); las reglas de negocio en
[REGLAS_DE_NEGOCIO.md](REGLAS_DE_NEGOCIO.md).

## 1. Configuración JWT

| Elemento | Valor / decisión | Origen |
|----------|------------------|--------|
| Algoritmo de firma | HS256 (`Jwts.SIG.HS256`) | RN114 |
| Secreto | Variable de entorno `JWT_SECRET`; mínimo 32 bytes | RN116 |
| Expiración | Variable de entorno `JWT_EXPIRACION_SEGUNDOS`; debe ser mayor que 0 | RN116 |
| Claims | `sub` con el ID del usuario, `username` y `role` | RN114 |
| Autoridad efectiva | El rol se obtiene del usuario cargado desde la base de datos; no se confía en el claim `role` | RN114 |
| Emisión | `JwtUtil.generateToken` | RN114 |
| Validación | `JwtUtil.parse` verifica firma y expiración; `JwtAuthFilter` resuelve el usuario y sus autoridades | RN114 |
| Respuesta de login | `expiresIn` en segundos, tomado de `JWT_EXPIRACION_SEGUNDOS` | RN114 |

El token se envía en la cabecera `Authorization: Bearer <token>`. La aplicación no mantiene sesión de servidor.

## 2. Arranque y variables de entorno

| Variable | Uso | Obligatoria |
|----------|-----|:-----------:|
| `POSTGRES_URL` | URL de conexión a PostgreSQL | Sí |
| `POSTGRES_USER` | Usuario de base de datos | Sí |
| `POSTGRES_PASSWORD` | Contraseña de base de datos | Sí |
| `SERVER_PORT` | Puerto HTTP de la aplicación | Sí |
| `JWT_SECRET` | Secreto de firma JWT; mínimo 32 bytes | Sí |
| `JWT_EXPIRACION_SEGUNDOS` | Expiración del token en segundos; mayor que 0 | Sí |
| `ADMIN_USERNAME` | Username del usuario semilla `ADMIN` | Sí |
| `ADMIN_PASSWORD` | Contraseña del usuario semilla `ADMIN` | Sí |
| `ALERTAS_CRON` | Expresión cron de la revisión programada de stock | Sí |

### 2.1 Validación de arranque (fail-fast)

La aplicación no arranca si:

- `JWT_SECRET` está vacío o tiene menos de 32 bytes.
- `JWT_EXPIRACION_SEGUNDOS` es nulo o menor o igual que 0.

En esos casos se lanza una `IllegalStateException` con un mensaje que comienza por `FATAL`. La validación se ejecuta
al inicializar la configuración (`AppConfig`, `@PostConstruct`). Origen: RN116.

### 2.2 Usuario semilla

Al arrancar, `AdminSeeder` crea el usuario `ADMIN` si no existe, usando `ADMIN_USERNAME` y `ADMIN_PASSWORD` y el rol
`ADMIN` definido en la migración inicial. Si el rol `ADMIN` no existe, el arranque falla con un mensaje `FATAL`.
Origen: RN111.

## 3. Zona horaria y representación de fechas

| Elemento | Decisión | Origen |
|----------|----------|--------|
| Zona de negocio | `America/Lima` (`ClockConfig.ZONA_NEGOCIO`) | RN409 |
| Reloj de la aplicación | `Clock.system(America/Lima)` | RN409 |
| Almacenamiento | UTC (`hibernate.jdbc.time_zone: UTC`) | RN409 |
| Serialización de instantes | UTC con patrón `yyyy-MM-dd'T'HH:mm:ss'Z'` (`JacksonConfig`) | RN409 |
| Fechas de reportes | `LocalDate` con formato `yyyy-MM-dd` | RN405, RN409 |
| Límites de rango | Inicio del día `desde` en `America/Lima`; inicio del día siguiente a `hasta` como límite exclusivo | RN405, RN409 |
| Cortes de estadísticas | Día natural; semana con lunes como inicio; mes con día 1 | RN409, RN410 |

Los rangos de fechas se calculan en la zona de negocio y se convierten a instantes UTC para consultar los datos
almacenados.

## 4. Tareas programadas

| Elemento | Decisión | Origen |
|----------|----------|--------|
| Habilitación | `@EnableScheduling` (`SchedulingConfig`) | RN306 |
| Expresión cron | `${alertas.cron}`, proveniente de `ALERTAS_CRON` | RN306 |
| Zona de ejecución | `America/Lima` | RN306 |
| Acción | Revisa el stock, obtiene las alertas y escribe un resumen y el detalle en el log | RN306 |
| Lista vacía | Registra el mensaje de «sin alertas» | RN306 |

La tarea no persiste las alertas; solo registra el resultado de la revisión. El cálculo de alertas se realiza al
consultar (ver [REQUISITOS_FUNCIONALES.md](REQUISITOS_FUNCIONALES.md)).

## 5. Transacciones y concurrencia

| Elemento | Decisión | Origen |
|----------|----------|--------|
| Operaciones de escritura | Servicios anotados con `@Transactional` | RN203, RN207 |
| Consultas y reportes | Servicios de lectura anotados con `@Transactional(readOnly = true)` | RN408 |
| Atomicidad del consumo | La validación y el descuento de todos los ítems se ejecutan en una misma transacción | RN207 |
| Bloqueo de fila | `findByIdAndActivoTrueForUpdate` con `LockModeType.PESSIMISTIC_WRITE` al descontar stock o ajustar | RN207, RN209 |
| Agrupación de ítems repetidos | Las cantidades del mismo producto se suman antes de validar disponibilidad | RN206, RN207 |
| Desbordamiento | `Math.addExact` / `Math.subtractExact` detectan el desbordamiento del entero y la operación se rechaza | RN211 |
| No negatividad | `Producto.descontarStock` verifica que el stock resultante no sea negativo | RN206 |

El bloqueo de fila y la transacción garantizan que no se produzcan descuentos parciales ni condiciones de carrera
en el consumo concurrente del mismo producto.

## 6. Persistencia y modelo de datos

| Elemento | Decisión | Origen |
|----------|----------|--------|
| Motor | PostgreSQL | Arquitectura |
| Migraciones | Flyway habilitado, con migraciones versionadas `V1` a `V5` | Arquitectura |
| Esquema | `spring.jpa.hibernate.ddl-auto: validate`; el esquema se define en las migraciones | Arquitectura |
| Acceso a datos | Spring Data JPA; `open-in-view: false` | Arquitectura |
| Stock | `stockActual` y `stockMinimo` como enteros; el stock inicial es 0 | RN106 |
| Costo | `costoUltimaCompra` con precisión 12 y escala 2 | RN203 |
| Movimiento | Conserva tipo, producto, cantidad/variación, stock anterior/posterior, fecha, usuario, costo y motivo | RN208 |
| Código de producto | Restricción única en base de datos | RN101 |
| SKU | Se genera como `CP-` seguido de 6 dígitos del identificador (`%06d`) después de guardar el producto | RN106 |
| Consumo | Conserva fecha, finalidad, referencia opcional y usuario; se relaciona con los movimientos generados | RN205, RN208 |

La generación del SKU a partir del identificador es una decisión de implementación; no forma parte de la regla de
negocio del stock inicial.

## 7. Arquitectura y decisiones de diseño

| Decisión | Descripción |
|----------|-------------|
| Organización por dominio | Paquetes `auth`, `catalogo`, `producto`, `movimiento`, `consumo`, `existencia`, `alerta`, `dashboard` y `reporte`. |
| Separación por capas | Controladores, servicios, repositorios, DTO y entidades; los controladores delegan la lógica en servicios. |
| Lógica pura | `ValorInventarioCalculator`, `EstadisticasBucketing` y `BajoStockAgrupador` no dependen de Spring ni de la base de datos. |
| Mapeo | `ProductoMapper` (MapStruct) construye las respuestas de producto, incluido `stockCritico`. |
| Seguridad sin estado | Filtro JWT antes del filtro de autenticación de Spring; CSRF deshabilitado; sesiones `STATELESS`. |
| Manejo de errores | `GlobalExceptionHandler` y `ErrorResponseWriter` unifican el contrato de error; ver [TABLA_ERRORES.md](TABLA_ERRORES.md). |
| Prefijo de API | `/api`; respuestas JSON; fechas `Instant` en UTC. |

## 8. Verificación

La configuración se contrastó estáticamente con `application.yaml`, `AppConfig`, `ClockConfig`, `JacksonConfig`,
`SchedulingConfig`, `AlertaScheduler`, `SecurityConfig`, `JwtUtil`, `JwtAuthFilter`, las entidades y los servicios.
No se ejecutaron compilaciones ni pruebas de runtime.
