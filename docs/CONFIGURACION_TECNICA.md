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
| `RATE_LIMIT_LOGIN_IP` | Número máximo de intentos de login por IP y ventana | Sí |
| `RATE_LIMIT_LOGIN_USERNAME` | Número máximo de intentos de login por username y ventana | Sí |
| `RATE_LIMIT_USUARIO` | Número máximo de peticiones autenticadas por usuario y ventana | Sí |
| `RATE_LIMIT_SWAGGER` | Límite holgado para rutas de Swagger | Sí |
| `RATE_LIMIT_VENTANA_LOGIN_SEGUNDOS` | Duración de la ventana de login, en segundos | Sí |
| `RATE_LIMIT_VENTANA_USUARIO_SEGUNDOS` | Duración de la ventana del resto de endpoints, en segundos | Sí |

### 2.1 Validación de arranque (fail-fast)

La aplicación no arranca si:

- `JWT_SECRET` está vacío o tiene menos de 32 bytes.
- `JWT_EXPIRACION_SEGUNDOS` es nulo o menor o igual que 0.
- Cualquiera de las variables `RATE_LIMIT_*` es nula o menor que 1 (`@Validated` en `RateLimitProperties`).

En esos casos se lanza una `IllegalStateException` con un mensaje que comienza por `FATAL`. La validación se ejecuta
al inicializar la configuración (`SeguridadProperties`, `@PostConstruct`). Origen: RN116.

### 2.2 Usuario semilla

Al arrancar, `AdminSeeder` crea el usuario `ADMIN` si no existe, usando `ADMIN_USERNAME` y `ADMIN_PASSWORD` y el rol
`ADMIN` definido en la migración inicial. Si el rol `ADMIN` no existe, el arranque falla con un mensaje `FATAL`.
Origen: RN111.

### 2.3 Proxy y límite de inicio de sesión

El backend limita por la IP del par TCP y por nombre de usuario. Detrás de un proxy, las solicitudes comparten el
límite por IP del proxy. Si se requiere limitar por IP de cliente, aplica ese límite en el proxy de confianza.
Los contadores del backend se mantienen en memoria por instancia.

### 2.4 Reintentos de movimientos

Los clientes pueden enviar un UUID en `Idempotency-Key` al registrar consumos, entradas o ajustes. Reenvía el
mismo UUID y cuerpo cuando se desconozca si la solicitud se confirmó. El backend guarda la respuesta junto con el
movimiento en la misma transacción. Reutilizar el UUID con otro cuerpo en la misma operación y cuenta devuelve 409.

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
| Bloqueo de fila | `findAllByIdInAndActivoTrueForUpdate` con `LockModeType.PESSIMISTIC_WRITE` y `order by p.id` adquiere todas las filas de un consumo en orden ascendente de id antes de descontar; `findByIdAndActivoTrueForUpdate` sigue usándose en entradas y ajustes | RN207, RN209 |
| Agrupación de ítems repetidos | Las cantidades del mismo producto se suman antes de validar disponibilidad | RN206, RN207 |
| Desbordamiento | `Math.addExact` / `Math.subtractExact` detectan el desbordamiento del entero y la operación se rechaza | RN211 |
| No negatividad | `Producto.descontarStock` verifica que el stock resultante no sea negativo | RN206 |

La validación de disponibilidad se hace contra el estado devuelto por la consulta bloqueada, no contra una entidad
precargada sin bloqueo en el `PersistenceContext`. Todas las filas se bloquean en orden ascendente de id en una sola
sentencia, de modo que dos consumos concurrentes con los mismos productos en orden inverso no forman un ciclo de
espera. La transacción garantiza que no se produzcan descuentos parciales.

## 6. Persistencia y modelo de datos

| Elemento | Decisión | Origen |
|----------|----------|--------|
| Motor | PostgreSQL | Arquitectura |
| Migraciones | Flyway habilitado, con migraciones versionadas `V1` a `V5` | Arquitectura |
| Esquema | `spring.jpa.hibernate.ddl-auto: validate`; el esquema se define en las migraciones | Arquitectura |
| Acceso a datos | Spring Data JPA; `open-in-view: false` | Arquitectura |
| Stock | `stockActual` y `stockMinimo` como enteros; el stock inicial es 0 | RN106 |
| Costo | `costoUltimaCompra` con precisión 12 y escala 2 | RN203 |
| Movimiento | Conserva tipo, producto, cantidad/variación, stock anterior/posterior, fecha, usuario, costo unitario y motivo. El costo es real en entradas y snapshot de `costoUltimaCompra` en consumos y ajustes | RN208 |
| Código de producto | Generado por el backend con `PRD-nnnn` (correlativo global) y restricción única en base de datos | RN101 |
| SKU | `AAA-ccc-nnnn`: siglas del nombre, id de categoría (`%03d`) y correlativo (`%04d`) dentro de ese prefijo. Se calcula antes de persistir, tanto en creación real como en datos demo | RN106 |
| Consumo | Conserva fecha, finalidad, referencia opcional y usuario; se relaciona con los movimientos generados | RN205, RN208 |

El correlativo se obtiene del mayor SKU existente para ese prefijo (`findMaxSkuByPrefijo`) en creación real, y de un
contador en memoria durante la generación demo. La restricción `UNIQUE` de la columna resuelve la ventana de
concurrencia entre la comprobación y el guardado. El patrón del SKU es una decisión de implementación; no forma parte
de la regla de negocio del stock inicial, y no se regenera si cambian categoría o proveedor.

## 7. Arquitectura y decisiones de diseño

| Decisión | Descripción |
|----------|-------------|
| Organización por dominio | Paquetes `catalogo`, `inventario`, `monitoreo`, `reportes`, `seguridad` y `auditoria`, más `compartido`, `configuracion` e `inicializacion`. |
| Organización interna | Cada dominio agrupa sus capacidades (por ejemplo `seguridad/autenticacion`, `seguridad/usuario`, `reportes/consulta/controller` (único controller para los 5 endpoints de `/api/reportes`), `reportes/exportacion` (exporters y servicio), `monitoreo/controller` (único controller para alertas y existencias), `monitoreo/alerta`, `monitoreo/existencia`, `catalogo/stockminimo`) y dentro de ellas separa controlador, servicio, DTO, entidad y repositorio según corresponda. |
| Lógica pura | `ValorInventarioCalculator`, `EstadisticasBucketing` y `EstadoStock` no dependen de Spring ni de la base de datos. |
| Mapeo | `ProductoMapper` (MapStruct) construye las respuestas de producto, incluido `stockCritico`. |
| Seguridad sin estado | Filtro JWT antes del filtro de autenticación de Spring; CSRF deshabilitado; sesiones `STATELESS`. |
| Rate limiting | `LoginRateLimitFilter` antes de `JwtAuthFilter` (IP y username) y `UsuarioRateLimitFilter` después (usuario de la BD). Estado en memoria detrás de `RateLimitStore`. |
| Manejo de errores | `ErrorCode` y `compartido/error` unifican el contrato para `GlobalExceptionHandler`, `ErrorResponseWriter`, seguridad, rate limiting y auditoría; ver [TABLA_ERRORES.md](TABLA_ERRORES.md). |
| Datos demo | `DemoData` mantiene catálogos relacionales: tipo → categoría, unidades, costos y cantidades por reposición. Unidad, categoría y costo se derivan del tipo, no se eligen al azar. Los consumos se generan desde `SERVICIOS`, excluyendo herramientas. Entradas, consumos y ajustes se planifican como eventos y se procesan en orden cronológico a lo largo de 540 días (apertura concentrada al inicio, reposiciones repartidas), con `costoUnitario` en los tres tipos, para que los reportes de costos por período tengan datos coherentes. |
| Prefijo de API | `/api`; respuestas JSON; fechas `Instant` en UTC. |

## 8. Verificación

La configuración se contrastó estáticamente con `application.yaml`, `SeguridadProperties`, `ClockConfig`, `JacksonConfig`,
`SchedulingConfig`, `AlertaScheduler`, `SecurityConfig`, `JwtUtil`, `JwtAuthFilter`, las entidades y los servicios.
No se ejecutaron compilaciones ni pruebas de runtime.
