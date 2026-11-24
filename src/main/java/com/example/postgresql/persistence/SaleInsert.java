package com.example.postgresql.persistence;

import com.example.postgresql.model.Sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public final class SaleInsert {

    private SaleInsert() {
    }

    public static Sale insert(Connection conn, Sale sale) throws SQLException {
        String sql = "INSERT INTO sales (store_id, total) VALUES (?, ?) RETURNING id, sale_date";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sale.storeId());
            ps.setBigDecimal(2, sale.total());
            try (ResultSet result = ps.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Creating sale returned no generated key");
                }
                Timestamp date = result.getTimestamp("sale_date");
                return Sale.builder()
                        .id(result.getLong("id"))
                        .storeId(sale.storeId())
                        .saleDate(date != null ? date.toLocalDateTime() : null)
                        .total(sale.total())
                        .build();
            }
        }
    }
}
