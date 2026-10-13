# ra2_act2: AadTex Enterprise Refactoring (Spring JDBC, JdbcTemplate & @Transactional)

**Módulo:** Acceso a Datos (AAD)  
**Unidad Didáctica:** UD02 – Manejo de Conectores (RA2)  
**Entregables:** Repositorio **GitHub** refactorizado, archivo **`ra2_act2.zip`** subido a Moodle y defensa técnica presencial.

---

## 1. Contexto de Negocio Empresarial (Caso AadTex v3.1 - Refactorización Empresarial)

### ❓ ¿Cuál es el problema real que estamos resolviendo?
Tras implementar con éxito el motor de persistencia a bajo nivel con JDBC puro en la ra2_act1, el equipo de Arquitectura de Software de la Sede Central de **AadTex** exige **modernizar la base del código y eliminar la fontanería manual de infraestructura**.

La gestión explícita de conexiones, la creación de `PreparedStatement`, el mapeo directo de `ResultSet` y el control imperativo de transacciones (`commit`/`rollback`) generan abundante código repetitivo (*boilerplate code*), dificultan el mantenimiento y aumentan el riesgo de fugas de recursos (*resource leaks*).

En esta segunda práctica, los alumnos refactorizarán el proyecto **AadTex** adaptando toda la capa de acceso a datos al ecosistema **Spring JDBC**:
* **Eliminar `PostgresqlDriver` y `DatabaseProperties`**: Delegar la creación del `DataSource` y la piscina de conexiones (HikariCP) en la autoconfiguración de Spring Boot.
* **Adopción de `JdbcTemplate`**: Sustituir el código JDBC imperativo por `JdbcTemplate` y expresiones Lambda / `RowMapper` para las operaciones CRUD.
* **Adopción de `SimpleJdbcCall`**: Reemplazar la invocación manual con `CallableStatement` por `SimpleJdbcCall` para ejecutar la función PL/pgSQL `calcular_total_ventas_tienda`.
* **Transacciones Declarativas (`@Transactional`)**: Eliminar los bloques `try-catch` con `commit()` y `rollback()` manuales e instaurar la gestión de transacciones gestionada por el contenedor de Spring.
* **Limpieza de Código Obsoleto (Clean Code)**: Eliminar del proyecto todo rastro de clases de configuración manuales para dejar un código profesional, conciso y mantenible.

---

## 2. Objetivos de Ingeniería de Software (RA2 Core & Spring Framework)

* [x] **Autoconfiguración de `DataSource`:** Eliminar la fontanería manual e integrar `spring-boot-starter-jdbc`.
* [x] **Refactorización a `JdbcTemplate`:** Consultas DML parametrizadas con mapeo fluido de entidades (`RowMapper`).
* [x] **Procedimientos Almacenados con `SimpleJdbcCall`:** Invocación limpia de funciones en PostgreSQL sin código JDBC de bajo nivel.
* [x] **Gestión Transaccional Declarativa (`@Transactional`):** Garantizar la atomicidad ACID de las ventas mediante anotaciones de Spring.
* [x] **Limpieza de Código y Arquitectura en Capas:** Supresión total de clases obsoletas (`PostgresqlDriver`, etc.).
* [x] **Desarrollo TDD:** Verificación de la refactorización utilizando 2 suites JUnit 5 adaptadas.

---

## 3. Estructura del Proyecto Refactorizado

El proyecto debe eliminar las clases manuales de conexión y adaptar la estructura al estándar empresarial de Spring Boot:

```text
src/main/java/com/<usuario>/aad/
├── model/
│   ├── Store.java                    <-- Record inmutable + Lombok @Builder
│   ├── Product.java                  <-- Record inmutable + Lombok @Builder (con atributo SKU)
│   ├── Sale.java                     <-- Record inmutable + Lombok @Builder
│   └── SaleDetail.java               <-- Record inmutable + Lombok @Builder
├── repository/
│   ├── CrudRepository.java           <-- Interfaz genérica CRUD
│   ├── StoreRepository.java          <-- Repositorio refactorizado con JdbcTemplate
│   ├── ProductRepository.java        <-- Repositorio refactorizado con JdbcTemplate
│   └── SaleRepository.java           <-- Repositorio con JdbcTemplate y SimpleJdbcCall
├── service/
│   ├── StoreService.java             <-- Lógica de negocio de Tiendas
│   ├── ProductService.java           <-- Lógica de negocio de Productos
│   └── SaleService.java              <-- Lógica de negocio decorada con @Transactional
└── Application.java                  <-- Punto de entrada Spring Boot

src/test/java/com/<usuario>/aad/
├── repository/
│   └── AadTexRepositoriesTest.java   <-- Test Suite 1: Pruebas TDD de Repositorios con JdbcTemplate
└── service/
    └── AadTexServicesTest.java       <-- Test Suite 2: Pruebas TDD de Servicios con @Transactional

src/main/resources/
├── application.yml                   <-- Configuración Spring Datasource e inicialización SQL
├── docker-compose.yml                <-- Servicio PostgreSQL 15
└── schema.sql                        <-- DDL Tablas y Funciones PL/pgSQL autoejecutado por Spring
```

> ⚠️ **REGLA DE LIMPIEZA OBLIGATORIA (Dead Code Removal)**: Debes **ELIMINAR COMPLETAMENTE** las clases `PostgresqlDriver.java` y `DatabaseProperties.java` del paquete `config/`. No deben quedar archivos muertos ni referencias no utilizadas en el proyecto.

---

## 4. Tabla Comparativa de Refactorización (Antes vs. Después)

| Componente / Operación | Práctica 1 (JDBC Puro) | Práctica 2 (Spring JDBC / Refactorizado) |
| :--- | :--- | :--- |
| **Conexión a BD** | `DriverManager.getConnection(...)` manual | Autoconfiguración mediante `DataSource` (HikariCP) |
| **Inicialización DDL/DML** | Lectura manual de ficheros `.sql` con `Statement` | Autoejecución de `schema.sql` por Spring Boot (`spring.sql.init.mode=always`) |
| **Consultas SQL y Lectura** | `PreparedStatement` y bucle `while (rs.next())` | `JdbcTemplate.query()`, `queryForObject()` y `RowMapper` |
| **Claves Generadas (PK)** | `ps.getGeneratedKeys()` manual | `KeyHolder` y `GeneratedKeyHolder` de Spring JDBC |
| **Funciones PL/pgSQL** | `CallableStatement` con `registerOutParameter()` | `SimpleJdbcCall` configurado con `.withFunctionName()` |
| **Transacciones (ACID)** | `setAutoCommit(false)`, `commit()` y `rollback()` | Anotación declarativa **`@Transactional`** en la capa de servicios |

---

## 5. Requisitos de Refactorización Paso a Paso

### 📍 Paso 1: Configuración en `application.yml` y Eliminación de Fontanería
1. Eliminar `PostgresqlDriver.java` y `DatabaseProperties.java`.
2. Configurar `application.yml` para delegar el control en Spring:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/aadtex_db
       username: postgres
       password: 1234
       driver-class-name: org.postgresql.Driver
     sql:
       init:
         mode: always
         schema-locations: classpath:schema.sql
   ```
3. Mover las sentencias DDL y la función PL/pgSQL a un único archivo `src/main/resources/schema.sql`.

### 📍 Paso 2: Refactorización de Repositorios con `JdbcTemplate`
Inyectar `JdbcTemplate` en todos los repositorios mediante constructor.

#### Ejemplo de `StoreRepository.java`:
```java
@Repository
public class StoreRepository implements CrudRepository<Store, Integer> {

    private final JdbcTemplate jdbcTemplate;

    public StoreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Store> storeRowMapper = (rs, rowNum) -> Store.builder()
            .id(rs.getInt("id_tienda"))
            .code(rs.getString("codigo"))
            .name(rs.getString("nombre"))
            .country(rs.getString("pais"))
            .region(rs.getString("region"))
            .build();

    @Override
    public Store create(Store store) {
        String sql = "INSERT INTO tienda (codigo, nombre, pais, region) VALUES (?, ?, ?, ?) RETURNING id_tienda";
        Integer generatedId = jdbcTemplate.queryForObject(sql, Integer.class,
                store.code(), store.name(), store.country(), store.region());
        return Store.builder()
                .id(generatedId)
                .code(store.code())
                .name(store.name())
                .country(store.country())
                .region(store.region())
                .build();
    }

    @Override
    public Store findById(Integer id) {
        String sql = "SELECT id_tienda, codigo, nombre, pais, region FROM tienda WHERE id_tienda = ?";
        List<Store> results = jdbcTemplate.query(sql, storeRowMapper, id);
        return results.stream().findFirst().orElse(null);
    }

    @Override
    public List<Store> findAll() {
        String sql = "SELECT id_tienda, codigo, nombre, pais, region FROM tienda";
        return jdbcTemplate.query(sql, storeRowMapper);
    }

    @Override
    public boolean update(Store store) {
        String sql = "UPDATE tienda SET codigo = ?, nombre = ?, pais = ?, region = ? WHERE id_tienda = ?";
        int rows = jdbcTemplate.update(sql, store.code(), store.name(), store.country(), store.region(), store.id());
        return rows > 0;
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM tienda WHERE id_tienda = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
```

### 📍 Paso 3: Invocación de Funciones PL/pgSQL con `SimpleJdbcCall`
En `SaleRepository.java`, sustituir `CallableStatement` por `SimpleJdbcCall`:
```java
@Repository
public class SaleRepository implements CrudRepository<Sale, Integer> {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall calculateTotalCall;

    public SaleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.calculateTotalCall = new SimpleJdbcCall(jdbcTemplate)
                .withFunctionName("calcular_total_ventas_tienda");
    }

    public BigDecimal calculateTotalSalesByStore(Integer storeId) {
        Map<String, Object> out = calculateTotalCall.execute(storeId);
        Object result = out.get("return");
        return result != null ? new BigDecimal(result.toString()) : BigDecimal.ZERO;
    }
    
    // resto de métodos createHeader, createDetail, etc. con JdbcTemplate...
}
```

### 📍 Paso 4: Transacciones Declarativas con `@Transactional`
En `SaleService.java`, eliminar las llamadas a `beginTransaction()`, `commit()` y `rollback()`. Anotar la clase o el método con `@Transactional`:

```java
@Service
public class SaleService {

    private final StoreRepository storeRepository;
    private final SaleRepository saleRepository;

    public SaleService(StoreRepository storeRepository, SaleRepository saleRepository) {
        this.storeRepository = storeRepository;
        this.saleRepository = saleRepository;
    }

    @Transactional
    public Sale processSale(Integer storeId, List<SaleDetail> details) {
        // 1. Validar existencia de la tienda
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            throw new IllegalArgumentException("La tienda no existe: " + storeId);
        }

        // 2. Calcular total acumulado de la venta
        BigDecimal total = details.stream()
                .map(d -> d.unitPrice().multiply(BigDecimal.valueOf(d.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Crear cabecera de la venta
        Sale newSale = saleRepository.createHeader(Sale.builder()
                .storeId(storeId)
                .total(total)
                .build());

        // 4. Registrar detalles
        for (SaleDetail detail : details) {
            saleRepository.createDetail(newSale.id(), detail);
        }

        // Spring gestiona el commit automáticamente al salir sin excepciones.
        // Si se lanza cualquier RuntimeException, Spring hace rollback automático.
        return newSale;
    }

    public BigDecimal getTotalSalesByStore(Integer storeId) {
        return saleRepository.calculateTotalSalesByStore(storeId);
    }
}
```

---

## 6. Suites de Pruebas TDD Adaptadas para Spring JDBC

### 🧪 Suite 1: Pruebas de Repositorios (`src/test/java/com/<usuario>/aad/repository/AadTexRepositoriesTest.java`)

```java
package com.usuario.aad.repository;

import com.usuario.aad.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Suite 1: Evaluación Completa de Repositorios con JdbcTemplate")
public class AadTexRepositoriesTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    private static Store testStore;
    private static Product testProduct;

    @Test
    @Order(1)
    @DisplayName("01. StoreRepository: CREATE y FIND_BY_ID con JdbcTemplate")
    void testStoreCreateAndFindById() {
        String uniqueCode = "STORE_JDBCTMP_" + System.currentTimeMillis();
        Store store = Store.builder()
                .code(uniqueCode)
                .name("Zara JdbcTemplate Sol")
                .country("España")
                .region("EMEA")
                .build();

        Store created = storeRepository.create(store);
        assertNotNull(created.id(), "JdbcTemplate debe recuperar la PK autonumérica");
        assertEquals(uniqueCode, created.code());

        Store fetched = storeRepository.findById(created.id());
        assertNotNull(fetched);
        assertEquals("Zara JdbcTemplate Sol", fetched.name());

        testStore = created;
    }

    @Test
    @Order(2)
    @DisplayName("02. StoreRepository: FIND_ALL, UPDATE y DELETE con JdbcTemplate")
    void testStoreFindAllUpdateAndDelete() {
        List<Store> stores = storeRepository.findAll();
        assertFalse(stores.isEmpty());

        Store toUpdate = Store.builder()
                .id(testStore.id())
                .code(testStore.code())
                .name("Zara Sol Renovada JdbcTemplate")
                .country("España")
                .region("EMEA")
                .build();

        boolean updated = storeRepository.update(toUpdate);
        assertTrue(updated);

        Store fetched = storeRepository.findById(testStore.id());
        assertEquals("Zara Sol Renovada JdbcTemplate", fetched.name());

        Store dummy = storeRepository.create(Store.builder()
                .code("DUMMY_JT_" + System.currentTimeMillis())
                .name("Dummy Store")
                .country("España")
                .region("EMEA")
                .build());

        boolean deleted = storeRepository.delete(dummy.id());
        assertTrue(deleted);
        assertNull(storeRepository.findById(dummy.id()));
    }

    @Test
    @Order(3)
    @DisplayName("03. ProductRepository: Operaciones CRUD Completas")
    void testProductFullCrud() {
        String sku = "SKU-JT-" + System.currentTimeMillis();
        Product product = Product.builder()
                .sku(sku)
                .name("Vestido Lino Spring")
                .price(new BigDecimal("79.95"))
                .build();

        Product created = productRepository.create(product);
        assertNotNull(created.id());

        Product fetched = productRepository.findById(created.id());
        assertEquals("Vestido Lino Spring", fetched.name());

        List<Product> products = productRepository.findAll();
        assertTrue(products.stream().anyMatch(p -> p.id().equals(created.id())));

        Product updatedProduct = Product.builder()
                .id(created.id())
                .sku(sku)
                .name("Vestido Lino Modificado")
                .price(new BigDecimal("89.95"))
                .build();

        assertTrue(productRepository.update(updatedProduct));
        assertEquals(new BigDecimal("89.95"), productRepository.findById(created.id()).price());

        assertTrue(productRepository.delete(created.id()));
        assertNull(productRepository.findById(created.id()));

        testProduct = productRepository.create(product);
    }

    @Test
    @Order(4)
    @DisplayName("04. Prevención de Inyección SQL en JdbcTemplate")
    void testSqlInjectionProtectionInJdbcTemplate() {
        String maliciousInput = "' OR '1'='1";
        Store maliciousStore = Store.builder()
                .code("SQLI_JT_" + System.currentTimeMillis())
                .name(maliciousInput)
                .country("España")
                .region("EMEA")
                .build();

        Store created = storeRepository.create(maliciousStore);
        assertNotNull(created.id());

        Store fetched = storeRepository.findById(created.id());
        assertEquals(maliciousInput, fetched.name());
    }

    @Test
    @Order(5)
    @DisplayName("05. SaleRepository: Cabecera, Detalles e Invocación con SimpleJdbcCall")
    void testSaleRepositoryWithSimpleJdbcCall() {
        assertNotNull(testStore);
        assertNotNull(testProduct);

        Sale saleHeader = Sale.builder()
                .storeId(testStore.id())
                .total(new BigDecimal("159.90"))
                .build();

        Sale createdSale = saleRepository.createHeader(saleHeader);
        assertNotNull(createdSale.id());

        SaleDetail detail = SaleDetail.builder()
                .saleId(createdSale.id())
                .productId(testProduct.id())
                .quantity(2)
                .unitPrice(testProduct.price())
                .build();

        saleRepository.createDetail(createdSale.id(), detail);

        BigDecimal calculatedTotal = saleRepository.calculateTotalSalesByStore(testStore.id());
        assertNotNull(calculatedTotal);
        assertTrue(calculatedTotal.compareTo(BigDecimal.ZERO) >= 0);
    }
}
```

---

### 🧪 Suite 2: Pruebas de Servicios y `@Transactional` (`src/test/java/com/<usuario>/aad/service/AadTexServicesTest.java`)

```java
package com.usuario.aad.service;

import com.usuario.aad.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Suite 2: Evaluación de Lógica de Negocio y Transacciones Declarativas (@Transactional)")
public class AadTexServicesTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SaleService saleService;

    private static Store activeStore;
    private static Product activeProduct1;
    private static Product activeProduct2;

    @Test
    @Order(1)
    @DisplayName("01. StoreService & ProductService: Gestión de Entidades")
    void testServicesManagement() {
        Store store = storeService.createStore(Store.builder()
                .code("SVC_SPRING_" + System.currentTimeMillis())
                .name("Zara Serrano Spring")
                .country("España")
                .region("EMEA")
                .build());
        assertNotNull(store.id());
        activeStore = store;

        Product p1 = productService.createProduct(Product.builder()
                .sku("SKU-SPRING-1-" + System.currentTimeMillis())
                .name("Gabardina Beige")
                .price(new BigDecimal("110.00"))
                .build());

        Product p2 = productService.createProduct(Product.builder()
                .sku("SKU-SPRING-2-" + System.currentTimeMillis())
                .name("Cinturón Cuero")
                .price(new BigDecimal("29.95"))
                .build());

        activeProduct1 = p1;
        activeProduct2 = p2;
    }

    @Test
    @Order(2)
    @DisplayName("02. SaleService: Venta Transaccional Exitosa con @Transactional (Commit)")
    void testTransactionalSaleSuccess() {
        assertNotNull(activeStore);
        assertNotNull(activeProduct1);
        assertNotNull(activeProduct2);

        // Total esperado: 110.00 + 29.95 = 139.95 €
        List<SaleDetail> details = List.of(
            SaleDetail.builder().productId(activeProduct1.id()).quantity(1).unitPrice(activeProduct1.price()).build(),
            SaleDetail.builder().productId(activeProduct2.id()).quantity(1).unitPrice(activeProduct2.price()).build()
        );

        Sale sale = saleService.processSale(activeStore.id(), details);
        assertNotNull(sale.id());
        assertEquals(0, new BigDecimal("139.95").compareTo(sale.total()));
    }

    @Test
    @Order(3)
    @DisplayName("03. SaleService: Rollback Automático Gestionado por Spring (@Transactional)")
    void testTransactionalSaleRollback() {
        assertNotNull(activeStore);

        int salesCountBefore = countSalesInDb();

        // Producto inexistente ID 999999 forzará DataAccessException / FK violation
        int invalidProductId = 999999;
        List<SaleDetail> invalidDetails = List.of(
            SaleDetail.builder().productId(invalidProductId).quantity(1).unitPrice(new BigDecimal("50.00")).build()
        );

        // @Transactional captura la excepción no controlada y desencadena el rollback automático
        assertThrows(Exception.class, () -> {
            saleService.processSale(activeStore.id(), invalidDetails);
        });

        int salesCountAfter = countSalesInDb();

        // Verificación ACID: El total de ventas registradas no debe incrementarse
        assertEquals(salesCountBefore, salesCountAfter, "Spring @Transactional debe revertir la cabecera insertada si fallan los detalles");
    }

    @Test
    @Order(4)
    @DisplayName("04. SaleService: Invocación de Función PL/pgSQL mediante SimpleJdbcCall")
    void testGetTotalSalesByStore() {
        assertNotNull(activeStore);

        BigDecimal total = saleService.getTotalSalesByStore(activeStore.id());
        assertNotNull(total);
        assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
    }

    private int countSalesInDb() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM venta", Integer.class);
        return count != null ? count : 0;
    }
}
```

---

## 7. Rúbrica de Evaluación y Criterios (CEv)

| Criterio de Evaluación | Ponderación | Descripción Detallada |
| :--- | :---: | :--- |
| **Limpieza de Código y Conexión (CEv a, b)** | **15%** | Eliminación total de `PostgresqlDriver`/`DatabaseProperties` y correcta autoconfiguración de `DataSource` e inicialización de `schema.sql`. |
| **Refactorización con `JdbcTemplate` (CEv c, d)** | **30%** | Sustitución completa del código JDBC tradicional en todos los repositorios por `JdbcTemplate` y `RowMapper`s limpios. |
| **Uso de `SimpleJdbcCall` (CEv e, f)** | **15%** | Correcta invocación de la función `calcular_total_ventas_tienda` usando `SimpleJdbcCall`. |
| **Transacciones Declarativas `@Transactional` (CEv g, h)** | **25%** | Eliminación del control manual de transacciones e implementación de `@Transactional` en `SaleService` con comprobación de rollback. |
| **Calidad de Código y TDD (CEv i, j, k)** | **15%** | Superación limpia de los tests en `AadTexRepositoriesTest` y `AadTexServicesTest`, estructura de paquetes y documentación `README.md`. |
