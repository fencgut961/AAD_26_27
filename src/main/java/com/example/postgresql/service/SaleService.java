package com.example.postgresql.service;

import com.example.postgresql.config.PostgresqlDriver;
import com.example.postgresql.model.Sale;
import com.example.postgresql.model.SaleDetail;
import com.example.postgresql.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final PostgresqlDriver driver;
    private final SaleRepository saleRepository;

    public Sale processSale(Long storeId, List<SaleDetail> details) {
        BigDecimal total = details.stream()
                .map(detail -> detail.unitPrice().multiply(BigDecimal.valueOf(detail.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Sale sale = Sale.builder()
                .storeId(storeId)
                .total(total)
                .build();
        try (Connection conn = driver.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Sale createdSale = saleRepository.create(sale, conn);
                String sql = "INSERT INTO sale_details (sale_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (SaleDetail detail : details) {
                        ps.setLong(1, createdSale.id());
                        ps.setLong(2, detail.productId());
                        ps.setInt(3, detail.quantity());
                        ps.setBigDecimal(4, detail.unitPrice());
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                return createdSale;
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                if (e instanceof SQLException sqlException) {
                    throw new IllegalStateException("Error processing sale", sqlException);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error processing sale", e);
        }
    }

    public BigDecimal getTotalSalesByStore(Long storeId) {
        String sql = "{ ? = call calculate_total_sales_by_store(?) }";
        try (Connection conn = driver.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.registerOutParameter(1, Types.NUMERIC);
            cs.setLong(2, storeId);
            cs.execute();
            return cs.getBigDecimal(1);
        } catch (SQLException e) {
            throw new IllegalStateException("Error calculating total sales for store " + storeId, e);
        }
    }
}
