# Productos

Aplicación monolítica con Spring Boot 4 para la gestión de productos. Un único JAR ejecutable que incluye backend y frontend en la misma unidad de despliegue.

---

> **Rama `docker-deploy`**
>
> Esta rama extiende el pipeline de CI/CD de `master` agregando tres stages de despliegue con Docker:
>
> | Stage adicional | Descripción |
> |---|---|
> | `Docker Build` | Construye la imagen Docker a partir del `Dockerfile` multi-stage |
> | `Docker Push` | Publica la imagen en Docker Hub con tag `{versión}` y `latest` |
> | `Deploy` | Levanta la aplicación en el servidor con `docker compose up` |
>
> Los nuevos archivos de soporte son `Dockerfile`, `compose.yaml` y `.dockerignore`.
> [Ver detalle completo →](#docker-deploy)

---

## Infraestructura requerida

Este proyecto ejecuta su pipeline en un **Jenkins pre-configurado** aprovisionado con Terraform + Ansible sobre AWS. El repositorio de infraestructura es público:

**[https://github.com/edsonmgoz/terraform-aws-devops-tools](https://github.com/edsonmgoz/terraform-aws-devops-tools)**

### Servicios habilitados por defecto

| Servicio | Estado |
|----------|--------|
| Docker   | Habilitado por defecto |
| Jenkins  | Habilitado por defecto |

### Servicios que deben habilitarse para este pipeline

El pipeline requiere **SonarQube** (stage `SonarQube`) y **JFrog Artifactory** (stage `Publish`). Ambos están disponibles en el repositorio de infraestructura pero **comentados por defecto**. Debes activarlos antes de ejecutar `terraform apply`.

**SonarQube Community** — descomenta en `resources.tf`:
```hcl
"mkdir -p /home/ubuntu/ansible/sonarqube-community/conf",
"mv /home/ubuntu/docker-compose.sonarqube-community.yml /home/ubuntu/ansible/sonarqube-community/docker-compose.sonarqube-community.yml",
"mv /home/ubuntu/sonar.properties /home/ubuntu/ansible/sonarqube-community/conf/sonar.properties",
"mv /home/ubuntu/site-sonarqube-community.yml /home/ubuntu/ansible/sonarqube-community/site-sonarqube-community.yml",
"ansible-playbook /home/ubuntu/ansible/sonarqube-community/site-sonarqube-community.yml",
```

**JFrog Artifactory OSS** — descomenta en `resources.tf`:
```hcl
"mkdir -p /home/ubuntu/ansible/jfrog-artifactory",
"mv /home/ubuntu/site-artifactory-oss.yml /home/ubuntu/ansible/jfrog-artifactory/site-artifactory-oss.yml",
"ansible-playbook /home/ubuntu/ansible/jfrog-artifactory/site-artifactory-oss.yml",
```

> Activa ambos servicios en una sola edición antes de aplicar. Cada `terraform apply` re-aprovisiona la instancia desde cero.

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

<a id="docker-deploy"></a>

## Despliegue con Docker — rama `docker-deploy`

### Pipeline completo

El Jenkinsfile de esta rama usa `agent none` con dos bloques de agentes diferenciados:

- **Stage `CI` (nested):** agente `maven:3.9-eclipse-temurin-25`. Contiene todos los stages del pipeline base (Compile → Test → Coverage → Package → SonarQube → Publish). Al finalizar, el JAR se guarda vía `stash` para que el siguiente bloque pueda acceder a él.
- **Stages Docker:** agente `any` (nodo Jenkins con acceso al Docker daemon del host). Consumen el JAR via `unstash` y ejecutan el ciclo build → push → deploy.

| Stage        | Descripción |
|---|---|
| `Docker Build` | `docker build` usando el JAR del `stash` + el `Dockerfile` multi-stage |
| `Docker Push`  | Login en Docker Hub con credencial Jenkins, push de tag versionado y `latest` |
| `Deploy`       | `docker compose up -d` con la imagen recién publicada |

El tag de la imagen se toma de la versión declarada en `pom.xml` (ej. `0.0.4`), no del número de build de Jenkins. Esto mantiene consistencia con el artefacto publicado en Artifactory.

---

### Dockerfile

Imagen base: `eclipse-temurin:25-jre`. Construcción en dos etapas:

1. **Builder:** extrae las capas del JAR con `-Djarmode=tools` (Spring Boot 4.x) para aprovechar el cache de capas de Docker.
2. **Runtime:** imagen limpia con usuario no-root (`appuser`) para mayor seguridad. Copia solo las capas extraídas.

```
edsonmgoz/products-monolit:{versión}
edsonmgoz/products-monolit:latest
```

La aplicación queda expuesta en el puerto `8080` del contenedor.

---

### compose.yaml

```yaml
services:
  products:
    image: edsonmgoz/products-monolit:${TAG:-latest}
    ports:
      - "8086:8080"        # puerto del host → puerto del contenedor
    healthcheck:
      test: curl -sf http://localhost:8080/api/products
      interval: 30s
      retries: 3
    restart: unless-stopped
```

El `TAG` se inyecta desde el pipeline (`TAG=${IMAGE_TAG} docker compose up`). Si se ejecuta manualmente sin definir `TAG`, usa `latest` por defecto.

---

### Credenciales requeridas en Jenkins

| ID en Jenkins           | Tipo                   | Uso |
|---|---|---|
| `dockerhub-credentials` | Username with password | Login en Docker Hub. El password debe ser un **Access Token** de Docker Hub (no la contraseña de la cuenta). |

Para crear el Access Token: Docker Hub → Account Settings → Personal access tokens → Generate new token.

---
