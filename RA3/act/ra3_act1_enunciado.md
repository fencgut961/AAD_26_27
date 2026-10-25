# ra3_act3_1: AadTex Enterprise Refactoring (Spring Data JPA, Hibernate & @Transactional)
**Módulo:** Acceso a Datos (AAD)  
**Unidad Didáctica:** UD03 – Herramientas de Mapeo Objeto-Relacional (RA3)  
**Entregables:** Repositorio **GitHub** refactorizado, archivo **ra3_act3_1.zip** subido a Moodle y defensa técnica presencial.

---

## 1. Contexto de Negocio Empresarial (Caso AadTex v4.0 - Refactorización a JPA/Hibernate)
### ¿Cuál es el problema real que estamos resolviendo?
Tras migrar el proyecto **AadTex** a Spring JDBC en la `ra2_act2` utilizando `JdbcTemplate` y `SimpleJdbcCall`, la Sede Central ha identificado que la aplicación sigue arrastrando acoplamiento y código repetitivo:
1. **Mantenimiento manual de esquemas (DDL):** La estructura relacional debe crearse y mantenerse manualmente en scripts SQL (`schema.sql`).
2. **Consultas SQL hardcodeadas y RowMappers:** Los repositorios continúan llenos de consultas SQL en texto plano y transformaciones manuales mediante `RowMapper`.
3. **Manejo explícito de claves foráneas:** Las relaciones entre tiendas, ventas, productos y detalles se gestionan mediante IDs enteros simples sin aprovechar el modelo de objetos.

En esta tercera práctica, el alumno refactorizará por completo el proyecto **AadTex** sustituyendo la capa de acceso a datos por el estándar **Spring Data JPA y Hibernate (ORM)**:

* **Eliminación de `schema.sql` y DDL manual:** Delegar la creación y actualización de la estructura relacional en Hibernate a partir de las anotaciones JPA (`spring.jpa.hibernate.ddl-auto=create-drop`).
* **Transformación a Entidades JPA:** Convertir los Java Records de dominio (`Store`, `Product`, `Sale`, `SaleDetail`) en clases de entidad anotadas con `@Entity`, `@Id`, `@GeneratedValue`, `@ManyToOne`, `@OneToMany` y reglas de validación de **Jakarta Bean Validation**.
* **Repositorios Declarativos (`JpaRepository`):** Sustituir los repositorios imperativos con `JdbcTemplate` por interfaces que extiendan de `JpaRepository<T, ID>`, aprovechando consultas derivadas por nombre y `@Query` con JPQL.
* **Optimización de Consultas (Prevención de N+1):** Aplicar `JOIN FETCH` en JPQL para recuperar cabeceras y detalles de ventas en un único viaje a la base de datos.
* **Transacciones Declarativas (`@Transactional`):** Mantener y consolidar la gestión ACID automatizada en la capa de servicios.

---

## 2. Objetivos de Ingeniería de Software (RA3 Core & Spring Data JPA)
* [x] **Mapeo de Entidades Relacionales:** Modelado JPA de `Store`, `Product`, `Sale` y `SaleDetail` con claves primarias autonuméricas y relaciones `@OneToMany` / `@ManyToOne`.
* [x] **Validación Declarativa de Integridad:** Aplicación de anotaciones Jakarta Bean Validation (`@NotBlank`, `@NotNull`, `@Min`, `@DecimalMin`).
* [x] **Eliminación de Fontanería SQL (Clean Code):** Supresión total de `schema.sql`, `JdbcTemplate`, `RowMapper` y `SimpleJdbcCall`.
* [x] **Repositorios Declarativos con Spring Data JPA:** Implementación de interfaces `JpaRepository` con consultas derivadas y JPQL personalizado.
* [x] **Optimización de Rendimiento:** Prevención del problema N+1 mediante `JOIN FETCH`.
* [x] **Gestión Transaccional Declarativa:** Control de transacciones con `@Transactional` e inyección de dependencias por constructor.
* [x] **Actualización de Pruebas TDD (JUnit 5):** Adaptación por parte del alumno de las suites de prueba `AadTexRepositoriesTest` y `AadTexServicesTest`.

---

## 3. Estructura del Proyecto Refactorizado
El proyecto debe eliminar el código SQL manual y organizar las clases según el estándar JPA de Spring Boot:

```text
src/main/java/com/<usuario>/aad/
├── model/
│   ├── Store.java                    <-- Entidad JPA (@Entity, @Table, Validation)
│   ├── Product.java                  <-- Entidad JPA (@Entity, SKU único, Validation)
│   ├── Sale.java                     <-- Entidad JPA Cabecera de Venta (@Entity, @ManyToOne Store)
│   └── SaleDetail.java               <-- Entidad JPA Detalle (@Entity, @ManyToOne Sale y Product)
├── repository/
│   ├── StoreRepository.java          <-- Interfaz JpaRepository<Store, Integer>
│   ├── ProductRepository.java        <-- Interfaz JpaRepository<Product, Integer>
│   └── SaleRepository.java           <-- Interfaz JpaRepository<Sale, Integer> con JPQL e JOIN FETCH
├── service/
│   ├── StoreService.java             <-- Servicio de gestión de tiendas (@Service)
│   ├── ProductService.java           <-- Servicio de gestión de productos (@Service)
│   └── SaleService.java              <-- Servicio transaccional de ventas (@Service + @Transactional)
└── Application.java                  <-- Punto de entrada Spring Boot

src/test/java/com/<usuario>/aad/
├── repository/
│   └── AadTexRepositoriesTest.java   <-- Suite 1: Test de Repositorios JPA (Actualizado por el alumno)
└── service/
    └── AadTexServicesTest.java       <-- Suite 2: Test de Servicios y Transacciones (Actualizado por el alumno)

src/main/resources/
├── application.yml                   <-- Configuración Spring Data JPA, HikariCP y Hibernate
└── docker-compose.yml                <-- Servicio PostgreSQL 15
```

⚠️ **REGLA DE LIMPIEZA OBLIGATORIA (Dead Code Removal)**: Debes **ELIMINAR COMPLETAMENTE** el archivo `src/main/resources/schema.sql` y cualquier rastro de `JdbcTemplate`, `RowMapper` o `SimpleJdbcCall`. La estructura de la base de datos debe ser generada de forma automática por Hibernate al arrancar la aplicación.

---

#### 4. Tabla Comparativa de Refactorización (UD02 vs. UD03)

| Componente / Operación | Práctica RA2 (Spring JDBC / JdbcTemplate) | Práctica RA3 (Spring Data JPA / Hibernate) |
| :--- | :--- | :--- |
| **Generación de Esquema BD** | Script manual `schema.sql` autoejecutado | Automática por Hibernate (`spring.jpa.hibernate.ddl-auto=create-drop`) |
| **Modelo de Dominio** | Java Records inmutables | Clases `@Entity` anotadas con getters/setters y constructor sin argumentos |
| **Mapeo de Resultados** | `RowMapper<T>` explícito con expresiones Lambda | Mapeo transparente y automático gestionado por el proveedor ORM |
| **Operaciones CRUD** | Consultas SQL imperativas (`jdbcTemplate.update`) | Métodos declarativos heredados de `JpaRepository<T, ID>` |
| **Claves Generadas (PK)** | Manejo de `KeyHolder` o `RETURNING` en SQL | Anotación `@GeneratedValue(strategy = GenerationType.IDENTITY)` |
| **Consultas Personalizadas** | Cadenas SQL nativas | Consultas derivadas por nombre o `@Query` con JPQL |
| **Relaciones entre Entidades** | Claves foráneas (IDs simples) en clases Java | Objetos asociados mediante `@ManyToOne` y `@OneToMany` |
| **Validación de Datos** | Comprobaciones manuales en métodos de servicio | Anotaciones estándar de **Jakarta Bean Validation** en las entidades |
| **Optimización N+1** | No aplicable (SQL nativo explícito) | Cláusula `JOIN FETCH` en JPQL para cargar relaciones perezosas (`LAZY`) |

---

## 5. Requisitos de Refactorización Paso a Paso

### 📍 Paso 1: Configuración de `application.yml`
1. Eliminar la propiedad `spring.sql.init.mode` y borrar el archivo `schema.sql`.
2. Configurar la autogeneración del esquema con Hibernate y la visualización formateada de las consultas SQL:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/aadtex_db
    username: postgres
    password: 1234
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      pool-name: AadTexHikariPool

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### 📍 Paso 2: Conversión del Modelo a Entidades JPA con Bean Validation
El alumno deberá sustituir los Records de Java por clases de entidad completas:
1. **`Store`**: Anotar con `@Entity` y `@Table(name = "tienda")`. Añadir validaciones `@NotBlank` en código y nombre.
2. **`Product`**: Anotar con `@Entity` y `@Table(name = "producto")`. El SKU debe ser único (`unique = true`) y no nulo. El precio debe ser mayor que cero (`@DecimalMin`).
3. **`Sale`**: Anotar con `@Entity` y `@Table(name = "venta")`. Representa la cabecera. Debe incluir la relación `@ManyToOne` con `Store` y la lista de detalles `@OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)`.
4. **`SaleDetail`**: Anotar con `@Entity` y `@Table(name = "detalle_venta")`. Debe incluir las relaciones `@ManyToOne` con `Sale` y con `Product`, además de la cantidad (`@Min(1)`) y el precio unitario.
5. **Requisito obligatorio**: Todas las entidades deben contar con un constructor sin argumentos (`@NoArgsConstructor`), getters y setters (`@Getter`, `@Setter`), y evitar referencias circulares en los métodos `toString()`, `equals()` y `hashCode()`.

### 📍 Paso 3: Definición de Repositorios Declarativos (`JpaRepository`)
El alumno deberá crear las interfaces de repositorio heredando de `JpaRepository`:
1. **`StoreRepository`**: Extender `JpaRepository<Store, Integer>`. Añadir consulta derivada `findByCode(String code)`.
2. **`ProductRepository`**: Extender `JpaRepository<Product, Integer>`. Añadir consulta derivada `findBySku(String sku)`.
3. **`SaleRepository`**: Extender `JpaRepository<Sale, Integer>`.
   - Implementar consulta JPQL optimizada con `JOIN FETCH` para recuperar una venta con sus detalles y productos en una sola consulta evitando el problema N+1.
   - Implementar consulta JPQL para calcular la suma total de ventas por tienda.

### 📍 Paso 4: Capa de Servicio y Gestión Transaccional Declarativa
Refactorizar los servicios (`StoreService`, `ProductService`, `SaleService`):
1. Inyectar los repositorios JPA mediante constructor (`@RequiredArgsConstructor` de Lombok).
2. En `SaleService.processSale(...)`, recibir la petición de venta, asociar la tienda y sus detalles a la entidad `Sale`, y persistirla.
3. Decorar los métodos de modificación con `@Transactional` y las consultas de lectura con `@Transactional(readOnly = true)`.
4. Garantizar que ante cualquier `RuntimeException` en la venta, Spring realice el rollback automático de la cabecera y los detalles.

---

# 6. Adaptación de las Suites de Pruebas TDD por parte del Alumno
⚠️ **Instrucciones para la evaluación**: El alumno **DEBE ADAPTAR Y ACTUALIZAR** las dos suites de prueba JUnit 5 heredadas del proyecto RA2 (`AadTexRepositoriesTest.java` y `AadTexServicesTest.java`) para ajustarlas al comportamiento del nuevo motor Spring Data JPA:

1. **Adaptar `AadTexRepositoriesTest.java`**:
   - Verificar las operaciones CRUD directamente a través de las interfaces `JpaRepository`.
   - Validar que Hibernate asigna correctamente las claves primarias autonuméricas (`@GeneratedValue`).
   - Comprobar que las consultas derivadas (`findByCode`, `findBySku`) retornan los datos esperados.
   - Probar la consulta JPQL con `JOIN FETCH` verificando que los detalles de la venta se cargan sin lanzar excepciones de carga perezosa (`LazyInitializationException`).

2. **Adaptar `AadTexServicesTest.java`**:
   - Verificar la creación transaccional de ventas completas (cabecera + detalles) utilizando la capa de servicio.
   - Comprobar la reversión de la transacción (`@Transactional` Rollback) simulando un fallo o un producto inválido, asegurando que el número total de ventas en la base de datos no se incrementa.
   - Validar la invocación del cálculo total de ventas por tienda mediante la consulta JPQL del repositorio.

---

# 7. Criterios de Evaluación y Entregables
1. **Código Limpio y Arquitectura (30%):** Supresión total de JDBC manual, `schema.sql` y `RowMapper`. Correcta estructuración en entidades, repositorios y servicios.
2. **Modelado JPA y Bean Validation (30%):** Configuración precisa de relaciones (`@ManyToOne`, `@OneToMany`), anotaciones de validación y autogeneración DDL con Hibernate.
3. **Consultas JPQL y Rendimiento (20%):** Correcta resolución del problema N+1 mediante `JOIN FETCH` en las consultas de ventas.
4. **Verificación TDD con JUnit 5 (20%):** Adaptación correcta y superación de la totalidad de los tests en `AadTexRepositoriesTest` y `AadTexServicesTest`.
