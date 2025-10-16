# Api Orquestador Rest y Consulta de Comprobantes

| SPRING VERSION |  SWAGGER | ACTUATOR |  AUTHENTICATION |
|----------------|---------|----------|----------------
| 2.1.6          |  ✅      | ❌       | JWT           |

## Descripción General
Este sistema permite el procesamiento de orquestador por lotes. La solución se encarga de leer archivos, procesar sus registros en batch y enviar peticiones de orquestador a un servicio externo. El flujo se realiza de forma asíncrona y mediante ejecución concurrente, con validaciones y manejo de errores para garantizar la continuidad del proceso sin detener toda la ejecución ante fallos puntuales.
Ahora, esta API también provee un servicio robusto para la consulta de comprobantes orquestadors, ofreciendo filtros avanzados y paginación para una recuperación eficiente de la información.
El servicio de Orquestador Restful es una API diseñada para centralizar y simplificar el proceso de orquestador y cancelación de documentos. Actúa como un middleware entre las aplicaciones internas de Actinver y el proveedor de servicios de orquestador (PAC), encapsulando la lógica de comunicación, manejo de errores y formato de datos requerido para el orquestador y la cancelación fiscal. Además,
Este repositorio contiene el código fuente y la documentación para el servicio de orquestador Restful de Actinver. Este servicio permite la integración con el proveedor de orquestador para procesar y certificar documentos fiscales.

## 🚀 Tecnologías Utilizadas
## Java 11
## Spring Boot 2.x (Versión 2.1.6), (asumido por ser una práctica común para servicios REST en Java)
## Maven
## Spring Data JDBC / JDBC puro (para la conexión a la base de datos)
## Base de Datos Oracle XSA
## Spring Security & JWT (para autenticación con Bearer Token)
## Lombok (para reducir código repetitivo en DTOs)
## Swagger/OpenAPI (Springfox) (para documentación automática de la API)
## Log4j2 / Logback (para el manejo de logs)
## Formato de Datos: JSON y TXT (para solicitudes y respuestas)

## Endpoints del Servicio de Orquestador
## Esta sección detalla los endpoints relacionados con el procesamiento y orquestador por lotes de comprobantes.

# Autenticación

# 1. Autenticación y Autorización
- Este servicio utiliza OAuth 2.0 para la autenticación y autorización de las solicitudes. Para acceder a los endpoints protegidos (/api/orquestador, /api/cancelar), primero debes obtener un token de acceso (Bearer Token) del endpoint /oauth/token.

# Esta API utiliza JSON Web Tokens (JWT) para la autenticación y autorización en sus endpoints protegidos. Para acceder a estos, se debe:
#  Obtener un JWT válido de tu servicio de autenticación (generalmente a través de una petición POST con credenciales a tu endpoint de login/token).
# Incluir este JWT en el encabezado Authorization de tus peticiones, usando el prefijo Bearer.
# Ejemplo de encabezado de autenticación:

# Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiZXhwIjoxNjk2OTEwNjAwLCJpYXQiOjE2OTY5MDcwMDB9.LA_FIRMA_DE_TU_JWT

# Obtención del Token de Acceso

**URL: /oauth/token**

- **\[POST\]**

- Descripción: Este endpoint es utilizado por los clientes para obtener un token de acceso válido utilizando el flujo de Password Credentials de OAuth 2.0.

# Headers Requeridos:

### Content-Type: application/json

**Request \[POST\]:**
```json
{
  "username": "apiexstream",
  "password": "4pi3XsTre4m"
}


```
---

**Response 200 OK:**

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhcGlleHN0cmVhbSIsImV4cCI6MTc1MzkwNzA2MSwiaWF0IjoxNzUzOTAzNDYxfQ.DWtPj5MOlxynmInXoULFOZzW7obCGdkK0z7ConC_xalULYQI5bzGB7ouulTJaZ8PpSJP6hrmLsgz14T2LZcBJA"
}

```
---

**Response Body (JSON - Error HTTP 400 Bad Request)::**

```json
{
  "error": "invalid_grant",
  "error_description": "Bad credentials"
}

```
---

# Uso del Token de Acceso
- Una vez obtenido el access_token, este debe ser incluido en el encabezado Authorization de todas las solicitudes a los endpoints protegidos.

# Header Requerido:

- Authorization: Bearer <your_access_token>

# Ejemplo de solicitud con token:

***HTTP:**

# POST /api/orquestador HTTP/1.1
# Host: v2dljboss19.actinver.com.mx:8193
# Content-Type: application/json
# Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

### Endpoints de la API

### 2. Endpoint del Servicio de Orquestador

- **\[POST\]** `/api/orquestador`
    -  Envía un documento una producción al proveedor de orquestador para su certificación y gestiona el flujo de procesamiento posterior, incluyendo reintentos y clasificación de errores.
---

