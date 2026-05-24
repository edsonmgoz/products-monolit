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
