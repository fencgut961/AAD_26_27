package com.example.postgresql.repository;

import com.example.postgresql.config.PostgresqlDriver;
import com.example.postgresql.model.Store;
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
public class StoreRepository implements CrudRepository<Store, Long> {

    private final PostgresqlDriver driver;

    @Override
    public Store create(Store store) {
        String sql = "INSERT INTO stores (code, name, country, region) VALUES (?, ?, ?, ?)";
        try (Connection conn = driver.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, store.code());
            ps.setString(2, store.name());
            ps.setString(3, store.country());
            ps.setString(4, store.region());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return Store.builder()
                        .id(keys.getLong("id"))
                        .code(store.code())
                        .name(store.name())
                        .country(store.country())
                        .region(store.region())
                        .build();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error creating store", e);
        }
    }

    @Override
    public Store findById(Long id) {
        String sql = "SELECT id, code, name, country, region FROM stores WHERE id = ?";
        try (Connection conn = driver.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error finding store " + id, e);
        }
    }

    @Override
    public List<Store> findAll() {
        String sql = "SELECT id, code, name, country, region FROM stores ORDER BY id";
        List<Store> result = new ArrayList<>();
        try (Connection conn = driver.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Error listing stores", e);
        }
    }

    @Override
    public boolean update(Store store) {
        String sql = "UPDATE stores SET code = ?, name = ?, country = ?, region = ? WHERE id = ?";
        try (Connection conn = driver.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, store.code());
            ps.setString(2, store.name());
            ps.setString(3, store.country());
            ps.setString(4, store.region());
            ps.setLong(5, store.id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error updating store " + store.id(), e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM stores WHERE id = ?";
        try (Connection conn = driver.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error deleting store " + id, e);
        }
    }

    private Store map(ResultSet rs) throws SQLException {
        return Store.builder()
                .id(rs.getLong("id"))
                .code(rs.getString("code"))
                .name(rs.getString("name"))
                .country(rs.getString("country"))
                .region(rs.getString("region"))
                .build();
    }
}
