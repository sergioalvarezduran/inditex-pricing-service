# Inditex Pricing Service

Servicio Spring Boot para resolver el caso de la prueba técnica: dado un `brandId`, `productId` y una `applicationDate`, devolver el precio final aplicable (PVP) según una tabla `PRICES` con vigencia por fechas y prioridad.

Proyecto implementado en arquitectura hexagonal (ports & adapters) y multi-módulo para que sea fácil de mantener y testear.

---
## Build & Test
mvn clean verify

--- 

## Run
mvn -f pricing-bootstrap/pom.xml spring-boot:run

---
## Qué resuelve exactamente (resumen del enunciado)

En la tabla `PRICES` existen registros con:

- `BRAND_ID`: cadena (1 = ZARA)
- `PRODUCT_ID`: producto
- `START_DATE` / `END_DATE`: rango en el que aplica el precio
- `PRICE_LIST`: tarifa aplicable
- `PRIORITY`: desambiguación si hay solape (mayor número => más prioridad)
- `PRICE`: PVP final
- `CURR`: moneda (ISO)

Se expone un endpoint REST que:

- recibe: `applicationDate`, `productId`, `brandId`
- devuelve: `productId`, `brandId`, `priceList`, `startDate`, `endDate`, `price`, `currency`

La base de datos es H2 en memoria e inicializada con los 4 registros del enunciado.

---

## Regla de negocio aplicada (cómo se elige el precio)

Con `brandId`, `productId` y `applicationDate`:

1. Se filtran los registros donde:
    - `startDate <= applicationDate <= endDate` (inclusive)
2. Si hay más de un resultado aplicable:
    - se elige el de mayor `priority`
3. Si hubiera empate real (caso general):
    - se elige el de `startDate` más reciente

---

## Arquitectura y módulos

### pricing-domain
- Modelo de dominio (Value Objects / entidad de precio)
- Validaciones (ids positivos, rango de fechas válido, money no negativo, etc.)
- Sin dependencias de Spring ni infraestructura

### pricing-application
- Casos de uso
- Puertos de entrada/salida (interfaces)
- Orquestación del dominio usando puertos
- Sin dependencias de Spring

### pricing-adapters
- Entrada: REST controller + DTOs
- Salida: persistencia (JPA / repository adapter)
- Mapea dominio ↔ infraestructura

### pricing-bootstrap
- Arranque Spring Boot (main)
- Wiring/configuración
- H2 + Liquibase
- Tests del endpoint (integración)

---

## Persistencia: H2 en memoria + Liquibase

Se usa una H2 in-memory y se inicializa con Liquibase.

Ubicación de changelogs:
- `pricing-bootstrap/src/main/resources/db/changelog`

Incluye:
- creación de tabla `prices`
- índice para consulta eficiente por producto/marca/fechas/prioridad
- inserción de los 4 registros de ejemplo

---

## API

### Endpoint

`GET /api/prices`

### Query params

- `applicationDate` (ISO local datetime, ejemplo: `2020-06-14T16:00:00`)
- `productId` (long)
- `brandId` (long)

### Ejemplo

`GET /api/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1`

### Response (ejemplo)

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 2,
  "startDate": "2020-06-14T15:00:00",
  "endDate": "2020-06-14T18:30:00",
  "price": 25.45,
  "currency": "EUR"
}
```

## Tests (qué se ha implementado y por qué)

El enunciado pide explícitamente tests al endpoint REST validando 5 peticiones concretas. Esos son los tests “obligatorios” y están implementados como tests de integración (se levanta la app real con H2 + Liquibase).

Además, hay unit tests en capas internas para que el core se valide rápido y sin framework.

---

### 1) Tests obligatorios del enunciado (endpoint REST)

Ubicación:

`pricing-bootstrap/src/test/java/com/inditex/pricing/adapters/in/rest/price/PriceControllerTest.java`

Qué validan:

- flujo completo `controller -> use case -> repository -> H2`
- que Liquibase ha cargado los datos correctos
- que la regla de selección de precio (prioridad y vigencia) se cumple en los 5 escenarios pedidos

Casos cubiertos:

- Test 1: petición a las 10:00 del día 14 (producto 35455, brand 1)  
  Resultado esperado: `priceList=1`, `price=35.50`

- Test 2: petición a las 16:00 del día 14 (producto 35455, brand 1)  
  Resultado esperado: `priceList=2`, `price=25.45`

- Test 3: petición a las 21:00 del día 14 (producto 35455, brand 1)  
  Resultado esperado: `priceList=1`, `price=35.50`

- Test 4: petición a las 10:00 del día 15 (producto 35455, brand 1)  
  Resultado esperado: `priceList=3`, `price=30.50`

- Test 5: petición a las 21:00 del día 16 (producto 35455, brand 1)  
  Resultado esperado: `priceList=4`, `price=38.95`

---

### 2) Unit tests (core)

Además del endpoint, hay unit tests para demostrar diseño y calidad:

- Dominio: validaciones y comportamiento (appliesAt inclusivo, rango válido, etc.)
- Caso de uso: que delega en el puerto y devuelve correctamente `Optional<Price>`

Esto no era estrictamente requerido por el enunciado, pero reduce regresiones y deja claro el enfoque de diseño.

## Mejoras que aplicaría en un entorno “productivo”

Para la prueba técnica he implementado lo necesario para el enunciado + estructura mantenible.  
En un servicio real, añadiría:

---

### Cobertura y quality gates

JaCoCo por módulo, con reglas diferentes por capa:

- `pricing-domain`: objetivo 100%
- `pricing-application`: objetivo 100%
- `pricing-adapters` / `pricing-bootstrap`: objetivo razonable (depende de integración/infra)

Además:
- failing build si no se cumplen umbrales (line/branch)

---

### Separación de suites

- Unit tests con Surefire
- Integration tests con Failsafe (por ejemplo, sufijo `*IT`)

---

### Calidad de código y pipeline

- formato automático (Spotless)
- Checkstyle / Sonar
- pipeline CI con reportes

> Nota: No están implementadas todas estas piezas porque no eran requisito explícito del ejercicio, pero el proyecto está preparado para crecer hacia ahí sin reestructuración.
