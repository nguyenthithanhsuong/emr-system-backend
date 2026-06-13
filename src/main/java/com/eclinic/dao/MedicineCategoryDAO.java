package com.eclinic.dao;

import com.eclinic.database.ConnectionManager;
import com.eclinic.models.MedicineCategory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MedicineCategoryDAO {

    public List<MedicineCategory> findAll() throws SQLException {
        String sql = "SELECT id, name, name_vi, description, display_order, created_at FROM medicine_categories ORDER BY display_order ASC";
        Connection conn = ConnectionManager.getConnection();
        List<MedicineCategory> categories = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.add(new MedicineCategory(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("name_vi"),
                    rs.getString("description"),
                    rs.getInt("display_order"),
                    rs.getString("created_at")
                ));
            }
            return categories;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public MedicineCategory findById(long id) throws SQLException {
        String sql = "SELECT id, name, name_vi, description, display_order, created_at FROM medicine_categories WHERE id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new MedicineCategory(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("name_vi"),
                    rs.getString("description"),
                    rs.getInt("display_order"),
                    rs.getString("created_at")
                );
            }
            return null;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public long create(String name, String nameVi, String description, int displayOrder) throws SQLException {
        String sql = "INSERT INTO medicine_categories (name, name_vi, description, display_order) VALUES (?, ?, ?, ?) RETURNING id";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, nameVi);
            stmt.setString(3, description);
            stmt.setInt(4, displayOrder);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong(1);
            throw new SQLException("Failed to create medicine category");
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public boolean update(long id, String name, String nameVi, String description, int displayOrder) throws SQLException {
        String sql = "UPDATE medicine_categories SET name = ?, name_vi = ?, description = ?, display_order = ? WHERE id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, nameVi);
            stmt.setString(3, description);
            stmt.setInt(4, displayOrder);
            stmt.setLong(5, id);
            return stmt.executeUpdate() > 0;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM medicine_categories WHERE id = ?";
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
