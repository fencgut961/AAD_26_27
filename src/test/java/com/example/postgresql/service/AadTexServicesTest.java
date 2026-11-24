package com.example.postgresql.service;

import com.example.postgresql.config.PostgresqlDriver;
import com.example.postgresql.model.Product;
import com.example.postgresql.model.Sale;
import com.example.postgresql.model.SaleDetail;
import com.example.postgresql.model.Store;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Suite 2: Evaluación Completa de Lógica de Negocio y Transacciones ACID")
public class AadTexServicesTest {

    private static Store activeStore;
    private static Product activeProduct1;
    private static Product activeProduct2;

    @Autowired
    private PostgresqlDriver postgresqlDriver;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SaleService saleService;

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
    void testProcessSaleSuccessCommit() throws Exception {
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
        try (Connection conn = postgresqlDriver.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM sale_details WHERE sale_id = ?")) {
            ps.setLong(1, sale.id());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1));
            }
        }
    }

    @Test
    @Order(4)
    @DisplayName("04. SaleService: Prueba Crítica de Rollback Transaccional ante Fallo de Integridad")
    void testProcessSaleRollbackOnFailure() throws Exception {
        assertNotNull(activeStore);

        int salesCountBefore = countSalesInDb();

        // Producto inexistente ID 999999 forzará un fallo de FK en detalle_venta
        Long invalidProductId = 999999L;
        List<SaleDetail> invalidDetails = List.of(
                SaleDetail.builder().productId(activeProduct1.id()).quantity(1).unitPrice(activeProduct1.price()).build(),
                SaleDetail.builder().productId(invalidProductId).quantity(1).unitPrice(new BigDecimal("50.00")).build()
        );

        // Se espera que el servicio lance excepción al detectar el fallo en la transacción
        assertThrows(RuntimeException.class, () -> saleService.processSale(activeStore.id(), invalidDetails), "El servicio debe lanzar RuntimeException tras hacer rollback");

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
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM sales")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}