# Productos

Aplicación monolítica con Spring Boot 4 para la gestión de productos. Un único JAR ejecutable que incluye backend y frontend en la misma unidad de despliegue.

---

## Stack tecnológico

| Capa          | Tecnología                        |
|---------------|-----------------------------------|
| Lenguaje      | Java 25                           |
| Framework     | Spring Boot 4.0.6                 |
| Persistencia  | Spring Data JPA + H2 (en memoria) |
| Validación    | Jakarta Validation                |
| Frontend      | HTML + CSS + JavaScript vanilla   |
| Build         | Maven (wrapper incluido)          |

---

## Arquitectura: `presentation-domain-data`

```
dev.edsonmm.products
├── presentation/
│   ├── controller/     ← Endpoints REST
│   └── request/        ← DTOs de entrada con validaciones
├── domain/
│   ├── entity/         ← Entidad JPA
│   ├── mapper/         ← Conversión DTO → entidad
│   └── service/
│       ├── interfaces/ ← Contrato del servicio
│       └── implement/  ← Implementación con lógica de negocio
├── data/
│   └── repository/     ← Repositorio Spring Data JPA
└── exception/
    ├── response/       ← Estructuras de respuesta de error
    └── ErrorHandler    ← Manejo centralizado de errores (@RestControllerAdvice)
```

Flujo de dependencias: `presentation → domain → data`

---

## Decisión de persistencia

Se usa **H2 en memoria** por defecto. No se requiere ninguna base de datos externa.

- Elegido por: configuración cero, arranque inmediato y facilidad de demostración.
- Los datos se reinician en cada arranque (comportamiento intencional para una POC).
- La consola H2 está disponible en `/h2-console` para inspección directa.

---

## Cómo ejecutar

```bash
./mvnw spring-boot:run
```

O bien, construir y ejecutar el JAR:

```bash
./mvnw clean package
java -jar target/products-0.0.1-SNAPSHOT.jar
```

---

## URLs principales

| Recurso      | URL                                  |
|--------------|--------------------------------------|
| Frontend     | http://localhost:8080/               |
| API REST     | http://localhost:8080/api/products   |
| Consola H2   | http://localhost:8080/h2-console     |

Credenciales H2: JDBC URL `jdbc:h2:mem:productsdb`, usuario `sa`, contraseña *(vacía)*.

---

## Endpoints

| Método | Ruta                   | Descripción             | Estado  |
|--------|------------------------|-------------------------|---------|
| GET    | `/api/products`        | Listar todos            | 200     |
| GET    | `/api/products/{id}`   | Obtener por ID          | 200/404 |
| POST   | `/api/products`        | Crear producto          | 201     |
| PUT    | `/api/products/{id}`   | Actualizar producto     | 200/404 |
| DELETE | `/api/products/{id}`   | Eliminar producto       | 204/404 |

Errores de validación devuelven `400` con detalle por campo. Recurso no encontrado devuelve `404` con mensaje.

---

## Campos del producto

| Campo       | Tipo          | Descripción                        |
|-------------|---------------|------------------------------------|
| id          | Long          | Generado automáticamente           |
| name        | String        | Obligatorio, 2–100 caracteres      |
| description | String        | Opcional, máximo 500 caracteres    |
| price       | BigDecimal    | Obligatorio, mayor o igual a cero  |
| stock       | Integer       | Obligatorio, mayor o igual a cero  |
| active      | boolean       | Activo por defecto                 |
| createdAt   | LocalDateTime | Asignado automáticamente al crear  |
| updatedAt   | LocalDateTime | Actualizado automáticamente        |

---

## Estrategia de eliminación

Eliminación **física**: `DELETE /api/products/{id}` elimina el registro de la base de datos. El campo `active` se gestiona desde el formulario de edición y permite marcar productos como inactivos sin eliminarlos.

---

## Pruebas

### Estructura

| Clase                          | Tipo               | Descripción                                      |
|--------------------------------|--------------------|--------------------------------------------------|
| `ProductMapperTest`            | Unitario puro      | Conversión DTO ↔ entidad, sin Spring             |
| `ProductRequestValidationTest` | Unitario puro      | Validaciones Jakarta sobre el DTO de entrada     |
| `ProductControllerTest`        | Slice `@WebMvcTest`| Endpoints REST con MockMvc y servicio mockeado   |
| `ProductServiceImplTest`       | Integración        | Lógica de negocio contra H2 real, con rollback   |
| `ProductsApplicationTests`     | Smoke test         | Verificación de arranque del contexto Spring     |

### Comandos

```bash
# Ejecutar todos los tests
./mvnw test

# Ejecutar una clase de test específica
./mvnw test -Dtest=ProductControllerTest

# Ejecutar un método de test específico
./mvnw test -Dtest=ProductControllerTest#getById_shouldReturn404WhenNotFound

# Ciclo completo: tests + reporte JaCoCo + validación de umbrales de cobertura
./mvnw verify
```

### Cobertura con JaCoCo

```bash
# Generar reporte tras ejecutar los tests
./mvnw test
./mvnw jacoco:report
```

El reporte HTML queda disponible en `target/site/jacoco/index.html`.
El reporte XML en `target/site/jacoco/jacoco.xml` es consumido por SonarQube.

Umbrales mínimos configurados: **80% de líneas** y **60% de ramas**. El build falla si no se alcanzan al ejecutar `./mvnw verify`.

---

## CI/CD

Pipeline declarativo en Jenkins. El agente es un contenedor Docker `maven:3.9-eclipse-temurin-25` — sin dependencias instaladas en el nodo Jenkins. El repositorio local Maven se almacena en `${WORKSPACE}/.m2` para aislarlo por ejecución.

| Stage       | Herramienta         | Resultado                                                              |
|-------------|---------------------|------------------------------------------------------------------------|
| `Compile`   | Maven               | Falla rápido si el código no compila                                   |
| `Test`      | JUnit 5 + Surefire  | Ejecuta 41 tests — reporte publicado en Jenkins                        |
| `Coverage`  | JaCoCo              | Reporte XML + HTML — mínimo 80% líneas / 60% ramas                     |
| `Package`   | Maven               | Genera el JAR ejecutable (`-DskipTests`)                               |
| `SonarQube` | SonarQube Community | Análisis estático de calidad — soporta branches y pull requests        |
| `Publish`   | JFrog Artifactory   | Publica en `products-monolit-snapshot` o `products-monolit-release` según versión del `pom.xml` |

El pipeline se dispara automáticamente en cada push via `githubPush()`. El workspace se limpia al finalizar cada ejecución (`cleanWs()`).

---
