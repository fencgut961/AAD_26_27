package com.example.postgresql.repository;

import com.example.postgresql.config.PostgresqlDriver;
import com.example.postgresql.model.Sale;
import com.example.postgresql.persistence.SaleInsert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SaleRepository implements CrudRepository<Sale, Long> {

    private final PostgresqlDriver driver;

    @Override
    public Sale create(Sale sale) {
        try (Connection conn = driver.getConnection()) {
            return SaleInsert.insert(conn, sale);
        } catch (SQLException e) {
            throw new IllegalStateException("Error creating sale", e);
        }
    }

    @Override
    public Sale findById(Long id) {
        String sql = "SELECT id, store_id, sale_date, total FROM sales WHERE id = ?";
        try (Connection conn = driver.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error finding sale " + id, e);
        }
    }

    @Override
    public List<Sale> findAll() {
        String sql = "SELECT id, store_id, sale_date, total FROM sales ORDER BY id";
        List<Sale> result = new ArrayList<>();
        try (Connection conn = driver.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Error listing sales", e);
        }
    }

    @Override
    public boolean update(Sale sale) {
        String sql = "UPDATE sales SET store_id = ?, total = ? WHERE id = ?";
        try (Connection conn = driver.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sale.storeId());
            ps.setBigDecimal(2, sale.total());
            ps.setLong(3, sale.id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error updating sale " + sale.id(), e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM sales WHERE id = ?";
        try (Connection conn = driver.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error deleting sale " + id, e);
        }
    }

    private Sale map(ResultSet rs) throws SQLException {
        Timestamp date = rs.getTimestamp("sale_date");
        return Sale.builder()
                .id(rs.getLong("id"))
                .storeId(rs.getLong("store_id"))
                .saleDate(date != null ? date.toLocalDateTime() : null)
                .total(rs.getBigDecimal("total"))
                .build();
    }
}
