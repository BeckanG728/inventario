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

Backend de gestión de inventario para talleres mecánicos sobre una única base de datos PostgreSQL. Cubre catálogo de
productos, entradas, consumos y ajustes de stock, alertas de bajo stock, reportes, exportación, dashboard y estadísticas.
Los requisitos y las reglas de negocio se documentan en `docs/`.

---

## Documentación

| Documento | Contenido |
|---|---|
| [REQUISITOS_FUNCIONALES.md](docs/REQUISITOS_FUNCIONALES.md) | Casos de uso y requisitos funcionales |
| [REGLAS_DE_NEGOCIO.md](docs/REGLAS_DE_NEGOCIO.md) | Reglas RN107–RN408 y su trazabilidad |
| [CATALOGO_ENDPOINTS.md](docs/CATALOGO_ENDPOINTS.md) | Endpoints, matriz de acceso, parámetros y contratos request/response |
| [POLITICAS_DE_SEGURIDAD.md](docs/POLITICAS_DE_SEGURIDAD.md) | Roles, autenticación JWT, autorización y limitación de frecuencia |
| [VALIDACIONES.md](docs/VALIDACIONES.md) | Validaciones y restricciones de datos por dominio |
| [TABLA_ERRORES.md](docs/TABLA_ERRORES.md) | Contrato HTTP de error, códigos y resolución de 400/404/409 |
| [CONFIGURACION_TECNICA.md](docs/CONFIGURACION_TECNICA.md) | Variables de entorno, JWT, zona horaria, transacciones, persistencia y arquitectura |

---

## Puesta en marcha

Requisitos: Java 21, Maven (o el wrapper `./mvnw`) y PostgreSQL 15 con una base de datos `inventario_taller`.

```bash
cp .env.example .env
./mvnw spring-boot:run
```

Pruebas:

```bash
./mvnw test
```

El listado de variables, su validación al arranque y el usuario semilla están en
[CONFIGURACION_TECNICA.md §2](docs/CONFIGURACION_TECNICA.md#2-arranque-y-variables-de-entorno). La matriz de acceso de
cada endpoint está en [CATALOGO_ENDPOINTS.md §2](docs/CATALOGO_ENDPOINTS.md#2-resumen-de-endpoints-y-matriz-de-acceso).

La documentación interactiva (Swagger UI) se publica en `http://localhost:${SERVER_PORT}/swagger-ui.html`.

---

<div align="center">

![Backend Inventario](https://img.shields.io/badge/Inventario-Taller_Mecánico-4A90D9?style=for-the-badge)

</div>
