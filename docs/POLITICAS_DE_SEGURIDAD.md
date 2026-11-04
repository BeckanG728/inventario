# Políticas de seguridad — Sistema de Gestión de Inventario

Este documento distingue las **decisiones de política** (responsabilidades del negocio, autenticación, autorización,
roles, permisos y credenciales) de los **mecanismos técnicos** que las implementan. Los detalles de configuración
técnica se completan en [CONFIGURACION_TECNICA.md](CONFIGURACION_TECNICA.md); los contratos de acceso de cada endpoint
se mantienen en [CATALOGO_ENDPOINTS.md](CATALOGO_ENDPOINTS.md).

## 1. Roles y responsabilidades

| Rol | Responsabilidad | Restricciones |
|-----|-----------------|---------------|
| `ADMIN` | Usuario semilla del sistema. Registra usuarios. | No accede a endpoints de negocio (catálogo, productos, inventario, alertas, dashboard, reportes ni estadísticas). No puede asignarse mediante el registro. |
| `JEFE_TALLER` | Administra el catálogo de productos, categorías y proveedores; registra entradas y ajustes; también puede registrar consumos. | No puede acceder a operaciones de otros roles que no le correspondan; su acceso se limita a lo definido en la matriz. |
| `MECANICO` | Registra consumos y consulta productos, existencias y alertas. | No modifica el catálogo ni el stock directamente; no accede a dashboard, reportes ni estadísticas. |

La responsabilidad operativa de `RN107` (solo el Jefe de Taller administra el catálogo) pertenece al dominio y se
documenta en [REGLAS_DE_NEGOCIO.md](REGLAS_DE_NEGOCIO.md). Las reglas `RN201`, `RN204` y `RN209` también definen
responsabilidades operativas dentro de sus módulos.

## 2. Autenticación

### 2.1 Inicio de sesión

- El usuario se autentica con `username` y `password` en `POST /api/auth/login`.
- Si las credenciales son incorrectas, la respuesta es `401 INVALID_CREDENTIALS`.
- La autenticación exitosa emite un token JWT y devuelve el tipo `Bearer`, el tiempo de expiración en segundos, el
  identificador del usuario y su rol.
- Las sesiones son sin estado: no se mantiene sesión de servidor entre peticiones.

### 2.2 Uso del token

- Las peticiones a endpoints protegidos deben incluir la cabecera `Authorization: Bearer <token>`.
- La ausencia de token en un endpoint protegido produce `401 UNAUTHORIZED`.
- Un token expirado produce `401 TOKEN_EXPIRED`.
- Un token malformado, con firma inválida o cuyo usuario no existe produce `401 TOKEN_INVALID`.
- Un token válido asociado a un usuario inactivo produce `401 ACCOUNT_DISABLED`.
- El rol efectivo se obtiene de la base de datos a partir del usuario del token; el valor del claim `role` no se usa
  como autoridad para las decisiones de acceso.

## 3. Autorización

### 3.1 Decisiones de política

- Solo `ADMIN` puede registrar usuarios (`RN110`).
- El rol asignado en el registro es obligatorio y solo puede ser `JEFE_TALLER` o `MECANICO`; `ADMIN` es un usuario
  semilla y no se asigna por registro (`RN111`).
- Solo `JEFE_TALLER` administra productos, categorías y proveedores (`RN107`).
- Solo `JEFE_TALLER` registra entradas y ajustes de inventario (`RN201`, `RN209`).
- `JEFE_TALLER` y `MECANICO` pueden registrar consumos (`RN204`).
- `ADMIN` solo registra usuarios y no accede a endpoints de negocio (`RN117`).
- `GET /api/productos`, `GET /api/existencias` y `GET /api/alertas` están disponibles para `JEFE_TALLER` y
  `MECANICO`; `ADMIN` no accede.
- `GET /api/dashboard`, `GET /api/reportes/**` (incluidas las exportaciones) y `GET /api/estadisticas` están
  disponibles solo para `JEFE_TALLER`; `ADMIN` y `MECANICO` no acceden.
- Toda petición a un endpoint protegido sin autenticación o con un rol sin permiso se rechaza con `401` o
  `403 FORBIDDEN`, respectivamente.

### 3.2 Matriz de acceso por grupo de endpoints

| Grupo | Endpoints | `ADMIN` | `JEFE_TALLER` | `MECANICO` | Público |
|-------|-----------|:-------:|:-------------:|:---------:|:-------:|
| Registro de usuarios | `POST /api/auth/register` | ✅ | ❌ | ❌ | — |
| Inicio de sesión | `POST /api/auth/login` | — | — | — | ✅ |
| Catálogo | `POST /api/categorias`, `POST /api/proveedores` | ❌ | ✅ | ❌ | — |
| Productos (escritura) | `POST /api/productos`, `PUT /api/productos/**`, `PATCH /api/productos/**` | ❌ | ✅ | ❌ | — |
| Productos (consulta) | `GET /api/productos` | ❌ | ✅ | ✅ | — |
| Existencias | `GET /api/existencias` | ❌ | ✅ | ✅ | — |
| Entradas y ajustes | `POST /api/movimientos/entrada`, `POST /api/movimientos/ajuste` | ❌ | ✅ | ❌ | — |
| Consumos | `POST /api/consumos` | ❌ | ✅ | ✅ | — |
| Alertas | `GET /api/alertas` | ❌ | ✅ | ✅ | — |
| Dashboard | `GET /api/dashboard` | ❌ | ✅ | ❌ | — |
| Reportes y exportaciones | `GET /api/reportes/**` | ❌ | ✅ | ❌ | — |
| Estadísticas | `GET /api/estadisticas` | ❌ | ✅ | ❌ | — |
| Infraestructura | `GET /actuator/health`, `GET /actuator/info` | — | — | — | ✅ |

La matriz completa por endpoint, con los códigos de respuesta y ejemplos, está en
[CATALOGO_ENDPOINTS.md](CATALOGO_ENDPOINTS.md). Cualquier diferencia entre esta matriz y el contrato de endpoints se
resuelve a favor del contrato.

## 4. Gestión de usuarios y credenciales

- `ADMIN` es un usuario semilla creado al arrancar la aplicación a partir de las variables de entorno
  `ADMIN_USERNAME` y `ADMIN_PASSWORD`; no se crea mediante el endpoint de registro.
- El registro de usuarios está reservado a `ADMIN`.
- El `username` es único (`RN112`).
- La contraseña debe tener al menos 8 caracteres, una mayúscula y un número (`RN113`). Se almacena con un algoritmo
  de hash, nunca en texto plano.
- El rol debe ser `JEFE_TALLER` o `MECANICO` (`RN111`).
- Las restricciones de formato, longitud y unicidad se detallan en [VALIDACIONES.md](VALIDACIONES.md).

## 5. Endpoints públicos

Los únicos endpoints que no exigen autenticación son:

- `POST /api/auth/login`
- `GET /actuator/health`
- `GET /actuator/info`
- `/error`

El resto de endpoints exige autenticación y, cuando corresponde, un rol determinado.

## 6. Mecanismos técnicos de implementación

Esta sección conserva la trazabilidad con el código; no introduce nuevas políticas.

| Mecanismo | Implementación | Propósito |
|-----------|----------------|-----------|
| Cadena de filtros y reglas de autorización | `SecurityConfig` | Define endpoints públicos, reglas por método y rol, sesiones sin estado, CSRF deshabilitado, CORS y manejadores de error 401/403. |
| Autorización a nivel de método | `@PreAuthorize` en controladores de movimientos y consumos | Refuerza las reglas de `JEFE_TALLER` para entradas/ajustes y de `JEFE_TALLER`/`MECANICO` para consumos. |
| Autenticación de usuarios | `DaoAuthenticationProvider`, `UsuarioDetailsService`, `BCryptPasswordEncoder` | Valida credenciales y aplica hash de contraseña. |
| Emisión y validación de tokens | `JwtUtil`, `JwtAuthFilter`, `TokenAuthenticationEntryPoint`, `ErrorResponseWriter` | Genera el JWT, valida firma y expiración, carga el usuario desde la base de datos y escribe los errores de seguridad. |
| Carga de autoridades | `Usuario.getAuthorities()` a partir del rol almacenado | Garantiza que el rol efectivo sea el de la base de datos. |
| Usuario semilla | `AdminSeeder` | Crea el usuario `ADMIN` si no existe. |

La configuración del algoritmo, los claims, el secreto, la expiración y el arranque se documentan en
[CONFIGURACION_TECNICA.md](CONFIGURACION_TECNICA.md).

## 7. Verificación

Las políticas se contrastaron estáticamente con `SecurityConfig`, `JwtAuthFilter`, `JwtUtil`, `AuthServiceImpl`,
`AdminSeeder` y los controladores con `@PreAuthorize`. No se ejecutaron pruebas de runtime.
