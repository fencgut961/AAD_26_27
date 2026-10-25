# ra2_act1: AadTex Store Persistence Engine (JDBC + TDD)

**Módulo:** Acceso a Datos (AAD)  
**Unidad Didáctica:** RA2 – Manejo de Conectores
**Metodología:** Desarrollo Guiado por Pruebas (TDD - *Test-Driven Development*)  
**Entregables:** Repositorio **GitHub** con el proyecto Spring Boot / Java, suite de pruebas **JUnit 5** superada al 100%, archivo **`ra2_act1.zip`** subido a Moodle y defensa oral presencial.

---

## 1. Contexto de Negocio Empresarial (Caso AadTex v3.0 - Sede Central)

### ¿Cuál es el problema real que estamos resolviendo?
Tras desplegar con éxito el motor de ingestión y distribución de ficheros en el RA1 (**AadTex App 1 y App 2**), la multinacional textil **AadTex** evoluciona la arquitectura de la Sede Central. La dirección técnica exige **abandonar los volcados planos en disco e instaurar un motor relacional centralizado en PostgreSQL (`aadtex_db`)** para consolidar los datos operativos de sus tiendas globales y garantizar la integridad ACID de las ventas.

En esta primera fase, se requiere construir el **motor de persistencia a bajo nivel (JDBC)** mediante **TDD (Test-Driven Development)**. El objetivo pedagógico es dominar la gestión explícita de conexiones, el cargador de scripts DDL, la prevención de inyección SQL mediante consultas parametrizadas, la invocación de funciones almacenadas y la coordinación transaccional manual (`commit`/`rollback`) respondiendo a una batería de pruebas automatizada real.

---

## 2. Objetivos de Ingeniería de Software (RA2 Core)

* [x] **Infraestructura con Docker Compose:** Despliegue e inicialización del contenedor PostgreSQL (`aadtex_postgres`).
* [x] **Driver de Conexión Manual (`PostgresqlDriver`):** Carga e inicialización de scripts SQL (`01_schema.sql` y `02_procedures.sql`) mediante JDBC .
* [x] **Modelado:** Definición de entidades (`Store`, `Product`, `Sale`, `SaleDetail`).
* [x] **Arquitectura en Capas:** Separación limpia de responsabilidades (`config`, `model`, `repository`, `service`).
* [x] **Repositorios CRUD con JDBC:** Implementación de consultas SQL parametrizadas con `PreparedStatement` y lectura con `ResultSet`.
* [x] **Invocación de Funciones PL/pgSQL:** Uso de `CallableStatement` para ejecutar la función de cálculo de ventas acumuladas por tienda.
* [x] **Gestión Transaccional Manual (ACID):** Control de `setAutoCommit(false)`, `commit()` y `rollback()` al registrar ventas compuestas con sus líneas de detalle.
* [x] **Desarrollo Guiado por Pruebas (TDD con JUnit 5):** Superación al 100% de la suite de pruebas unitarias y de integración proporcionada (`AadTexAssignmentTest`).

---

## 3. Estructura de Proyecto Obligatoria (Organización por Capas)

El proyecto debe respetar estrictamente la siguiente jerarquía de paquetes y recursos:

```text
src/main/java/com/<usuario>/aad/
├── config/
│   ├── DatabaseProperties.java       <-- Componente para lectura y mapeo de propiedades de application.yml
│   └── PostgresqlDriver.java         <-- Gestor manual de conexión, scripts y transacciones (inyecta DatabaseProperties)
├── model/
│   ├── Store.java                    <-- Record / Entity Tienda
│   ├── Product.java                  <-- Record / Entity Producto
│   ├── Sale.java                     <-- Record / Entity Venta
│   └── SaleDetail.java               <-- Record / Entity Línea de Detalle
├── repository/
│   ├── CrudRepository.java           <-- Interfaz genérica CRUD
│   ├── StoreRepository.java          <-- Repositorio JDBC de Tiendas (tienda)
│   ├── ProductRepository.java        <-- Repositorio JDBC de Productos (producto)
│   └── SaleRepository.java           <-- Repositorio JDBC de Ventas y Detalle (venta + detalle_venta)
├── service/
│   ├── StoreService.java             <-- Lógica de negocio y gestión de Tiendas
│   ├── ProductService.java           <-- Lógica de negocio y catálogo de Productos
│   └── SaleService.java              <-- Coordinación transaccional de Ventas (commit/rollback)
└── Application.java                  <-- Punto de entrada principal Spring Boot

src/test/java/com/<usuario>/aad/
├── repository/
│   └── AadTexRepositoriesTest.java   <-- Test Suite 1: Cobertura completa de la capa de Repositorios (CRUD + SQL)
└── service/
    └── AadTexServicesTest.java       <-- Test Suite 2: Cobertura completa de la capa de Servicios y Negocio (ACID + TDD)

src/main/resources/
├── application.yml                   <-- Parámetros de conexión JDBC
├── docker-compose.yml                <-- Servicio PostgreSQL 15
└── sql/
    └── ddl/
        ├── 01_schema.sql             <-- Tablas, PKs, FKs y restricciones CHECK / UNIQUE
        └── 02_procedures.sql         <-- Función PL/pgSQL de cálculo de totales
```

---

## 4. Scripts SQL de Inicialización (`src/main/resources/sql/ddl/`)

### 📄 `01_schema.sql`
```sql
CREATE TABLE IF NOT EXISTS tienda (
    id_tienda SERIAL PRIMARY KEY,
    codigo VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    pais VARCHAR(50) NOT NULL,
    region VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS producto (
    id_producto SERIAL PRIMARY KEY,
    sku VARCHAR(40) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    precio NUMERIC(10, 2) NOT NULL CHECK (precio > 0)
);

CREATE TABLE IF NOT EXISTS venta (
    id_venta SERIAL PRIMARY KEY,
    id_tienda INT NOT NULL REFERENCES tienda(id_tienda) ON DELETE CASCADE,
    fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total NUMERIC(12, 2) NOT NULL CHECK (total >= 0)
);

CREATE TABLE IF NOT EXISTS detalle_venta (
    id_venta INT NOT NULL REFERENCES venta(id_venta) ON DELETE CASCADE,
    id_producto INT NOT NULL REFERENCES producto(id_producto) ON DELETE RESTRICT,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(10, 2) NOT NULL CHECK (precio_unitario > 0),
    PRIMARY KEY (id_venta, id_producto)
);
```

### 📄 `02_procedures.sql`
```sql
CREATE OR REPLACE FUNCTION calcular_total_ventas_tienda(p_id_tienda INT) 
RETURNS NUMERIC AS $$
DECLARE
    v_total NUMERIC(12, 2);
BEGIN
    SELECT COALESCE(SUM(total), 0.00) INTO v_total
    FROM venta
    WHERE id_tienda = p_id_tienda;
    
    RETURN v_total;
END;
$$ LANGUAGE plpgsql;
```

---

## 5. Requisitos de Desarrollo Paso a Paso

### 📍 Paso 1: Infraestructura y Configuración (`docker-compose.yml` y `application.yml`)
1. Configurar un contenedor con PostgreSQL 15 escuchando en el puerto `5432`.
2. Base de datos: `aadtex_db`, usuario: `aadtex_user`, contraseña: `aadtex_pass`.
3. Especificar las credenciales en `src/main/resources/application.yml`.

### 📍 Paso 2: Gestor Manual de Conexión (`PostgresqlDriver.java`)
Implementar la clase encargada de:
* Leer los parámetros de configuración de `application.yml`.
* Proveer objetos `Connection` mediante `DriverManager.getConnection()`.
* Ejecutar automáticamente al iniciar los scripts `01_schema.sql` y `02_procedures.sql`.
* Proporcionar métodos explícitos para iniciar transacción (`setAutoCommit(false)`), hacer `commit()` y realizar `rollback()`.

### 📍 Paso 3: Modelado con Java Records (`com.<usuario>.aad.model`)
Definir las entidades del dominio:
* `Store(Integer id, String code, String name, String country, String region)`
* `Product(Integer id, String sku, String name, BigDecimal price)`
* `Sale(Integer id, Integer storeId, LocalDateTime saleDate, BigDecimal total)`
* `SaleDetail(Integer saleId, Integer productId, Integer quantity, BigDecimal unitPrice)`

### 📍 Paso 4: Capa de Repositorios JDBC (`com.<usuario>.aad.repository`)
* **`CrudRepository<T, ID>`**: Interfaz genérica con métodos `create`, `findById`, `findAll`, `update`, `delete`.
* **`StoreRepository` y `ProductRepository`**: Implementación de las operaciones CRUD con `PreparedStatement` y `ResultSet`.
* **`SaleRepository`**:
  * Inserción de cabecera de venta retornando la clave primaria generada (`RETURN_GENERATED_KEYS`).
  * Inserción en lote (*batch*) de las líneas de detalle.
  * Invocación de la función `calcular_total_ventas_tienda` mediante `CallableStatement` registrando el parámetro de salida (`Types.NUMERIC`).

### 📍 Paso 5: Capa de Servicios y Transacciones (`com.<usuario>.aad.service`)
* **`StoreService`** y **`ProductService`**: Métodos delegados de gestión de tiendas y catálogo.
* **`SaleService`**: Lógica de negocio para procesar ventas garantizando la **Atomicidad (ACID)**:
  1. Iniciar transacción manual con `postgresqlDriver.beginTransaction()`.
  2. Validar existencia de la tienda.
  3. Registrar la cabecera de la venta.
  4. Insertar cada línea de detalle.
  5. Ejecutar `postgresqlDriver.commit()` en caso de éxito.
  6. Ante cualquier excepción o fallo de integridad, ejecutar inmediatamente `postgresqlDriver.rollback()`.

---

## 6. Metodología TDD y Batería de Pruebas Automatizadas (2 Test Suites)

En esta actividad se aplica **TDD (Test-Driven Development)** de forma estricta. Se entregan **dos suites de pruebas independientes** que cubren el 100% de los métodos de la aplicación:

1. **`AadTexRepositoriesTest.java`**: Audita la capa de acceso a datos (`StoreRepository`, `ProductRepository`, `SaleRepository`), comprobando todos los métodos del CRUD genérico (`create`, `findById`, `findAll`, `update`, `delete`), consultas parametrizadas contra SQL Injection e invocación de funciones PL/pgSQL mediante `CallableStatement`.
2. **`AadTexServicesTest.java`**: Audita la capa de lógica de negocio y coordinación transaccional (`StoreService`, `ProductService`, `SaleService`), comprobando reglas de validación de negocio, cálculo de totales, commit atómico y la prueba crítica de **Rollback ACID** ante errores de integridad.

---

### 🧪 Test Suite 1: Pruebas de la Capa de Repositorios (`src/test/java/com/<usuario>/aad/repository/AadTexRepositoriesTest.java`)

```java
package com.usuario.aad.repository;

import com.usuario.aad.config.PostgresqlDriver;
import com.usuario.aad.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Suite 1: Evaluación Completa de Repositorios JDBC (CRUD + SQL)")
public class AadTexRepositoriesTest {

    @Autowired
    private PostgresqlDriver postgresqlDriver;

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
    @DisplayName("01. StoreRepository: CREATE y FIND_BY_ID")
    void testStoreCreateAndFindById() {
        String uniqueCode = "STORE_REPO_" + System.currentTimeMillis();
        Store store = Store.builder()
                .code(uniqueCode)
                .name("Tienda Repo Test")
                .country("España")
                .region("EMEA")
                .build();

        Store created = storeRepository.create(store);
        assertNotNull(created.id(), "La tienda creada debe devolver su ID primario autonumérico");
        assertEquals(uniqueCode, created.code());

        Store fetched = storeRepository.findById(created.id());
        assertNotNull(fetched, "Debe encontrar la tienda por su ID");
        assertEquals("Tienda Repo Test", fetched.name());

        testStore = created;
    }

    @Test
    @Order(2)
    @DisplayName("02. StoreRepository: FIND_ALL, UPDATE y DELETE")
    void testStoreFindAllUpdateAndDelete() {
        // FIND_ALL
        List<Store> stores = storeRepository.findAll();
        assertFalse(stores.isEmpty(), "La lista de tiendas no debe estar vacía");

        // UPDATE
        Store toUpdate = Store.builder()
                .id(testStore.id())
                .code(testStore.code())
                .name("Tienda Repo Actualizada")
                .country("Francia")
                .region("EMEA")
                .build();

        boolean updated = storeRepository.update(toUpdate);
        assertTrue(updated, "El update de la tienda debe devolver true");

        Store fetchedAfterUpdate = storeRepository.findById(testStore.id());
        assertEquals("Tienda Repo Actualizada", fetchedAfterUpdate.name());
        assertEquals("Francia", fetchedAfterUpdate.country());

        // DELETE
        Store dummyStore = storeRepository.create(Store.builder()
                .code("DUMMY_" + System.currentTimeMillis())
                .name("Tienda Borrado")
                .country("España")
                .region("EMEA")
                .build());

        boolean deleted = storeRepository.delete(dummyStore.id());
        assertTrue(deleted, "El borrado de la tienda dummy debe ser exitoso");
        assertNull(storeRepository.findById(dummyStore.id()), "La tienda borrada ya no debe existir en BD");
    }

    @Test
    @Order(3)
    @DisplayName("03. ProductRepository: CREATE, FIND_BY_ID, FIND_ALL, UPDATE y DELETE")
    void testProductFullCrud() {
        String sku = "SKU-REPO-" + System.currentTimeMillis();
        Product product = Product.builder()
                .sku(sku)
                .name("Camisa Repo Test")
                .price(new BigDecimal("49.95"))
                .build();

        // CREATE
        Product created = productRepository.create(product);
        assertNotNull(created.id(), "El producto creado debe poseer ID");

        // FIND_BY_ID
        Product fetched = productRepository.findById(created.id());
        assertEquals("Camisa Repo Test", fetched.name());

        // FIND_ALL
        List<Product> products = productRepository.findAll();
        assertTrue(products.stream().anyMatch(p -> p.id().equals(created.id())));

        // UPDATE
        Product updatedProduct = Product.builder()
                .id(created.id())
                .sku(sku)
                .name("Camisa Repo Modificada")
                .price(new BigDecimal("59.95"))
                .build();

        boolean isUpdated = productRepository.update(updatedProduct);
        assertTrue(isUpdated);
        assertEquals(new BigDecimal("59.95"), productRepository.findById(created.id()).price());

        testProduct = created;
    }

    @Test
    @Order(4)
    @DisplayName("04. Prevention de SQL Injection mediante PreparedStatement")
    void testSqlInjectionProtectionInRepo() {
        String maliciousInput = "' OR '1'='1";
        Store maliciousStore = Store.builder()
                .code("SQLI_" + System.currentTimeMillis())
                .name(maliciousInput)
                .country("España")
                .region("EMEA")
                .build();

        Store created = storeRepository.create(maliciousStore);
        assertNotNull(created.id());

        Store fetched = storeRepository.findById(created.id());
        assertEquals(maliciousInput, fetched.name(), "El texto malicioso debe ser parametrizado sin ejecutarse como SQL");
    }

    @Test
    @Order(5)
    @DisplayName("05. SaleRepository: Cabecera, Detalles, Búsquedas e Invocación CallableStatement")
    void testSaleRepositoryOperations() throws Exception {
        assertNotNull(testStore);
        assertNotNull(testProduct);

        // Crear Venta Cabecera
        Sale saleHeader = Sale.builder()
                .storeId(testStore.id())
                .total(new BigDecimal("99.90"))
                .build();

        Sale createdSale = saleRepository.createHeader(saleHeader);
        assertNotNull(createdSale.id(), "La cabecera de la venta debe generar un ID");

        // Crear Detalle
        SaleDetail detail = SaleDetail.builder()
                .saleId(createdSale.id())
                .productId(testProduct.id())
                .quantity(2)
                .unitPrice(testProduct.price())
                .build();

        saleRepository.createDetail(createdSale.id(), detail);

        // FIND_BY_ID & FIND_ALL
        Sale fetchedSale = saleRepository.findById(createdSale.id());
        assertNotNull(fetchedSale);

        List<Sale> allSales = saleRepository.findAll();
        assertFalse(allSales.isEmpty());

        // Invocación de CallableStatement (Función PL/pgSQL)
        BigDecimal calculatedTotal = saleRepository.calculateTotalSalesByStore(testStore.id());
        assertNotNull(calculatedTotal);
        assertTrue(calculatedTotal.compareTo(BigDecimal.ZERO) >= 0);
    }
}
```

---

### 🧪 Test Suite 2: Pruebas de la Capa de Lógica de Negocio y Servicios (`src/test/java/com/<usuario>/aad/service/AadTexServicesTest.java`)

```java
package com.usuario.aad.service;

import com.usuario.aad.config.PostgresqlDriver;
import com.usuario.aad.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Suite 2: Evaluación Completa de Lógica de Negocio y Transacciones ACID")
public class AadTexServicesTest {

    @Autowired
    private PostgresqlDriver postgresqlDriver;

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
    @DisplayName("01. StoreService: Métodos de Negocio y Gestión de Tiendas")
    void testStoreServiceFullBusinessLogic() {
        String code = "SVC_STORE_" + System.currentTimeMillis();
        Store store = Store.builder()
                .code(code)
                .name("Zara Sol Service")
                .country("España")
                .region("EMEA")
                .build();

        // Crear
        Store created = storeService.createStore(store);
        assertNotNull(created.id());

        // Obtener por ID
        Store fetched = storeService.getStoreById(created.id());
        assertEquals("Zara Sol Service", fetched.name());

        // Listar todas
        List<Store> all = storeService.getAllStores();
        assertTrue(all.stream().anyMatch(s -> s.id().equals(created.id())));

        // Actualizar
        Store updatedData = Store.builder()
                .id(created.id())
                .code(code)
                .name("Zara Sol Service Renovada")
                .country("España")
                .region("EMEA")
                .build();
        boolean updated = storeService.updateStore(updatedData);
        assertTrue(updated);

        activeStore = storeService.getStoreById(created.id());
        assertEquals("Zara Sol Service Renovada", activeStore.name());
    }

    @Test
    @Order(2)
    @DisplayName("02. ProductService: Métodos de Negocio y Catálogo de Productos")
    void testProductServiceFullBusinessLogic() {
        String sku1 = "SKU-SVC-1-" + System.currentTimeMillis();
        String sku2 = "SKU-SVC-2-" + System.currentTimeMillis();

        Product p1 = productService.createProduct(Product.builder()
                .sku(sku1)
                .name("Abrigo Lana")
                .price(new BigDecimal("120.00"))
                .build());

        Product p2 = productService.createProduct(Product.builder()
                .sku(sku2)
                .name("Bufanda Sostenible")
                .price(new BigDecimal("25.00"))
                .build());

        assertNotNull(p1.id());
        assertNotNull(p2.id());

        List<Product> catalog = productService.getAllProducts();
        assertTrue(catalog.size() >= 2);

        activeProduct1 = p1;
        activeProduct2 = p2;
    }

    @Test
    @Order(3)
    @DisplayName("03. SaleService: Procesamiento Exitoso de Venta Transaccional (Commit ACID)")
    void testProcessSaleSuccessCommit() {
        assertNotNull(activeStore);
        assertNotNull(activeProduct1);
        assertNotNull(activeProduct2);

        // Detalle: (1 * 120.00) + (2 * 25.00) = 170.00 €
        List<SaleDetail> details = List.of(
            SaleDetail.builder().productId(activeProduct1.id()).quantity(1).unitPrice(activeProduct1.price()).build(),
            SaleDetail.builder().productId(activeProduct2.id()).quantity(2).unitPrice(activeProduct2.price()).build()
        );

        Sale sale = saleService.processSale(activeStore.id(), details);

        assertNotNull(sale.id(), "La venta debe haber sido persistida con ID asignado");
        assertEquals(0, new BigDecimal("170.00").compareTo(sale.total()), "El cálculo del total de la venta debe ser 170.00 €");
    }

    @Test
    @Order(4)
    @DisplayName("04. SaleService: Prueba Crítica de Rollback Transaccional ante Fallo de Integridad")
    void testProcessSaleRollbackOnFailure() throws Exception {
        assertNotNull(activeStore);

        int salesCountBefore = countSalesInDb();

        // Producto inexistente ID 999999 forzará un fallo de FK en detalle_venta
        int invalidProductId = 999999;
        List<SaleDetail> invalidDetails = List.of(
            SaleDetail.builder().productId(invalidProductId).quantity(1).unitPrice(new BigDecimal("50.00")).build()
        );

        // Se espera que el servicio lance excepción al detectar el fallo en la transacción
        assertThrows(RuntimeException.class, () -> {
            saleService.processSale(activeStore.id(), invalidDetails);
        }, "El servicio debe lanzar RuntimeException tras hacer rollback");

        int salesCountAfter = countSalesInDb();

        // Verificación ACID: El rollback manual debe haber deshecho la cabecera insertada previamente
        assertEquals(salesCountBefore, salesCountAfter, "El total de ventas en BD debe ser idéntico al inicial (Rollback ejecutado)");
    }

    @Test
    @Order(5)
    @DisplayName("05. SaleService: Validación de Invocación de Función PL/pgSQL de Totales por Tienda")
    void testGetTotalSalesByStore() {
        assertNotNull(activeStore);

        BigDecimal totalFacturado = saleService.getTotalSalesByStore(activeStore.id());

        assertNotNull(totalFacturado);
        assertTrue(totalFacturado.compareTo(BigDecimal.ZERO) > 0, "La función PL/pgSQL debe devolver el total acumulado de las ventas de la tienda");
    }

    private int countSalesInDb() throws Exception {
        try (Connection conn = postgresqlDriver.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM venta")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
```

---
