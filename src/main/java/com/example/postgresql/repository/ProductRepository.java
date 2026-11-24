package com.example.postgresql.repository;

import com.example.postgresql.config.PostgresqlDriver;
import com.example.postgresql.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRepository implements CrudRepository<Product, Long> {

    private final PostgresqlDriver driver;

    @Override
    public Product create(Product product) {
        String sql = "INSERT INTO products (sku, name, price) VALUES (?, ?, ?)";
        try (Connection conn = driver.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.sku());
            ps.setString(2, product.name());
            ps.setBigDecimal(3, product.price());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return Product.builder()
                        .id(keys.getLong("id"))
                        .sku(product.sku())
                        .name(product.name())
                        .price(product.price())
                        .build();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error creating product", e);
        }
    }

    @Override
    public Product findById(Long id) {
        String sql = "SELECT id, sku, name, price FROM products WHERE id = ?";
        try (Connection conn = driver.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error finding product " + id, e);
        }
    }

    @Override
    public List<Product> findAll() {
        String sql = "SELECT id, sku, name, price FROM products ORDER BY id";
        List<Product> result = new ArrayList<>();
        try (Connection conn = driver.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Error listing products", e);
        }
    }

    @Override
    public boolean update(Product product) {
        String sql = "UPDATE products SET sku = ?, name = ?, price = ? WHERE id = ?";
        try (Connection conn = driver.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.sku());
            ps.setString(2, product.name());
            ps.setBigDecimal(3, product.price());
            ps.setLong(4, product.id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error updating product " + product.id(), e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = driver.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error deleting product " + id, e);
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        return Product.builder()
                .id(rs.getLong("id"))
                .sku(rs.getString("sku"))
                .name(rs.getString("name"))
                .price(rs.getBigDecimal("price"))
                .build();
    }
}
