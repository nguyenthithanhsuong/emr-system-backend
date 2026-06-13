package com.eclinic.dao;

import com.eclinic.database.ConnectionManager;
import com.eclinic.models.Medicine;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    private Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine med = new Medicine(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("unit"),
            rs.getBigDecimal("price"),
            rs.getInt("stock_quantity"),
            rs.getString("expiry_date")
        );
        long catId = rs.getLong("category_id");
        med.setCategoryId(rs.wasNull() ? null : catId);
        med.setCategoryName(rs.getString("category_name"));
        med.setCategoryNameVi(rs.getString("category_name_vi"));
        return med;
    }

    public long create(String name, String unit, BigDecimal price, int stockQuantity, String expiryDate, Long categoryId) throws SQLException {
        String sql = "INSERT INTO medicines (name, unit, price, stock_quantity, expiry_date, category_id) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, unit);
            stmt.setBigDecimal(3, price);
            stmt.setInt(4, stockQuantity);
            stmt.setTimestamp(5, Timestamp.valueOf(expiryDate + " 00:00:00"));
            if (categoryId != null && categoryId > 0) stmt.setLong(6, categoryId);
            else stmt.setNull(6, Types.BIGINT);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong(1);
            throw new SQLException("Failed to create medicine.");
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    /** Legacy create without category */
    public long create(String name, String unit, BigDecimal price, int stockQuantity, String expiryDate) throws SQLException {
        return create(name, unit, price, stockQuantity, expiryDate, null);
    }

    public Medicine findById(long id) throws SQLException {
        String sql = "SELECT m.*, mc.name AS category_name, mc.name_vi AS category_name_vi " +
                     "FROM medicines m LEFT JOIN medicine_categories mc ON m.category_id = mc.id WHERE m.id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
            return null;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public List<Medicine> findAll() throws SQLException {
        String sql = "SELECT m.*, mc.name AS category_name, mc.name_vi AS category_name_vi " +
                     "FROM medicines m LEFT JOIN medicine_categories mc ON m.category_id = mc.id ORDER BY mc.display_order, m.name";
        Connection conn = ConnectionManager.getConnection();
        List<Medicine> medicines = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) medicines.add(mapRow(rs));
            return medicines;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    /** Search medicines by name and/or category */
    public List<Medicine> search(String searchTerm, Long categoryId) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT m.*, mc.name AS category_name, mc.name_vi AS category_name_vi " +
            "FROM medicines m LEFT JOIN medicine_categories mc ON m.category_id = mc.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" AND (LOWER(m.name) LIKE ? OR LOWER(mc.name_vi) LIKE ?)");
            String pattern = "%" + searchTerm.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND m.category_id = ?");
            params.add(categoryId);
        }
        sql.append(" ORDER BY mc.display_order, m.name");

        Connection conn = ConnectionManager.getConnection();
        List<Medicine> medicines = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) medicines.add(mapRow(rs));
            return medicines;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public boolean updateStock(long id, int newQuantity) throws SQLException {
        String sql = "UPDATE medicines SET stock_quantity = ? WHERE id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, newQuantity);
            stmt.setLong(2, id);
            return stmt.executeUpdate() > 0;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public boolean update(long id, String name, String unit, BigDecimal price, int stockQuantity, String expiryDate, Long categoryId) throws SQLException {
        String sql = "UPDATE medicines SET name = ?, unit = ?, price = ?, stock_quantity = ?, expiry_date = ?, category_id = ? WHERE id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, unit);
            stmt.setBigDecimal(3, price);
            stmt.setInt(4, stockQuantity);
            stmt.setTimestamp(5, Timestamp.valueOf(expiryDate + " 00:00:00"));
            if (categoryId != null && categoryId > 0) stmt.setLong(6, categoryId);
            else stmt.setNull(6, Types.BIGINT);
            stmt.setLong(7, id);
            return stmt.executeUpdate() > 0;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM medicines WHERE id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }
}
