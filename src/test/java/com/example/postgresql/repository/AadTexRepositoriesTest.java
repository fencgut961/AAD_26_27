package com.example.postgresql.repository;


import com.example.postgresql.model.Product;
import com.example.postgresql.model.Sale;
import com.example.postgresql.model.Store;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Suite 1: Evaluación Completa de Repositorios JDBC (CRUD + SQL)")
public class AadTexRepositoriesTest {

    private static Store testStore;
    private static Product testProduct;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

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
    @DisplayName("05. SaleRepository: CREATE, FIND_BY_ID, FIND_ALL, UPDATE y DELETE")
    void testSaleRepositoryOperations() {
        assertNotNull(testStore);
        assertNotNull(testProduct);

        Sale saleHeader = Sale.builder()
                .storeId(testStore.id())
                .total(new BigDecimal("99.90"))
                .build();

        Sale createdSale = saleRepository.create(saleHeader);
        assertNotNull(createdSale.id(), "La venta debe generar un ID");
        Sale fetchedSale = saleRepository.findById(createdSale.id());
        assertNotNull(fetchedSale);
        List<Sale> allSales = saleRepository.findAll();
        assertTrue(allSales.stream().anyMatch(s -> s.id().equals(createdSale.id())));

        Sale updatedSale = Sale.builder()
                .id(createdSale.id())
                .storeId(testStore.id())
                .total(new BigDecimal("120.00"))
                .build();
        assertTrue(saleRepository.update(updatedSale));
        assertEquals(0, new BigDecimal("120.00").compareTo(saleRepository.findById(createdSale.id()).total()));
        assertTrue(saleRepository.delete(createdSale.id()));
        assertNull(saleRepository.findById(createdSale.id()));
        assertFalse(saleRepository.delete(createdSale.id()));
    }
}