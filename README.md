# Delicias del Sur - Backend y API Gateway (DSY1104)

API REST de la tienda Delicias del Sur, hecha con Java 21, Spring Boot 3, Spring Security con JWT, MySQL y Swagger. El acceso desde el frontend pasa siempre por un API Gateway.

## Estructura

- `backend/`: API REST (puerto 8081). Entidades, servicios, controladores, seguridad y pruebas unitarias.
- `gateway/`: Spring Cloud Gateway (puerto 8080). Único punto de entrada: maneja el CORS y expone la ruta pública `/delicias/v1/**`, que traduce por dentro a `/api/v1/**` del backend. El frontend solo conoce esta dirección y esta ruta.
- `scripts/probar-api.ps1`: pruebas de permisos y reglas de negocio contra el gateway.

## Arquitectura

    Frontend React (5173)  ->  Gateway (8080)  ->  Backend (8081)  ->  MySQL (3306)

El backend no debe quedar expuesto públicamente: en el despliegue solo el gateway puede llegar al puerto 8081.

## Requisitos

- Java 21
- MySQL (Laragon en local: usuario `root`, sin contraseña, puerto 3306)

## Cómo ejecutar en local

Con MySQL encendido, en dos terminales distintas:

    cd backend
    .\mvnw spring-boot:run

    cd gateway
    .\mvnw spring-boot:run

La base `delicias_del_sur` se crea sola y el `DataSeeder` carga usuarios, categorías y productos si está vacía.

Swagger UI (solo local, directo al backend): http://localhost:8081/swagger-ui.html. No se expone a través del gateway.

## Variables de entorno

| Variable | Dónde | Uso | Valor por defecto |
|---|---|---|---|
| `DB_URL` | backend | URL JDBC de MySQL | Laragon local |
| `DB_USER` | backend | Usuario de MySQL | `root` |
| `DB_PASS` | backend | Contraseña de MySQL | vacía |
| `JWT_SECRET` | backend | Clave para firmar los tokens | clave de desarrollo, cambiarla en producción |
| `JWT_EXPIRATION` | backend | Duración del token en ms | 86400000 (24 h) |
| `BACKEND_URL` | gateway | Dirección del backend | `http://localhost:8081` |
| `CORS_ORIGINS` | gateway | Origen del frontend permitido | `http://localhost:5173` |

## Usuarios de prueba (solo desarrollo)

| Rol | Correo | Contraseña |
|---|---|---|
| ADMINISTRADOR | admin@deliciasdelsur.cl | admin123 |
| VENDEDOR | vendedor@deliciasdelsur.cl | vendedor123 |
| CLIENTE | cliente@deliciasdelsur.cl | cliente123 |

## Endpoints y permisos

Las rutas del backend están versionadas bajo `/api/v1`. Desde el frontend se llaman como `/delicias/v1/...` a través del gateway (por ejemplo `/delicias/v1/productos`).

| Ruta | Método | Quién puede |
|---|---|---|
| `/auth/login`, `/auth/registro` | POST | Público |
| `/productos`, `/productos/{id}` | GET | Público |
| `/categorias`, `/categorias/{id}` | GET | Público |
| `/productos`, `/categorias` | POST, PUT, DELETE | ADMINISTRADOR |
| `/usuarios` | GET, POST, PUT, DELETE | ADMINISTRADOR |
| `/boletas` | POST | CLIENTE y ADMINISTRADOR |
| `/boletas/mis-boletas` | GET | Cualquier usuario autenticado (solo ve las suyas) |
| `/boletas`, `/boletas/{id}` | GET | ADMINISTRADOR y VENDEDOR |

El vendedor solo puede consultar productos y órdenes. Cualquier otra operación recibe 403.

## Regla de negocio principal

Al crear una boleta el servidor valida el stock, calcula el total (aplicando la oferta de cada producto) y descuenta el stock, todo dentro de una transacción. El cliente solo envía productos y cantidades.

## Pruebas

Pruebas unitarias (JUnit 5 y Mockito) de los servicios:

    cd backend
    .\mvnw test

Pruebas de permisos contra la API en ejecución (backend y gateway encendidos):

    .\scripts\probar-api.ps1