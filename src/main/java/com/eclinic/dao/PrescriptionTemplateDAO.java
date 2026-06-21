package com.eclinic.dao;

import com.eclinic.database.ConnectionManager;
import com.eclinic.models.PrescriptionTemplate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionTemplateDAO {

    public long create(Long doctorId, String name, String itemsJson) throws SQLException {
        String sql = "INSERT INTO prescription_templates (doctor_id, name, items) VALUES (?, ?, ?::jsonb) RETURNING id";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (doctorId != null) {
                stmt.setLong(1, doctorId);
            } else {
                stmt.setNull(1, Types.BIGINT);
            }
            stmt.setString(2, name);
            stmt.setString(3, (itemsJson == null || itemsJson.isEmpty()) ? "[]" : itemsJson);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to create prescription template.");
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public PrescriptionTemplate findById(long id) throws SQLException {
        String sql = "SELECT id, doctor_id, name, items, created_at FROM prescription_templates WHERE id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public List<PrescriptionTemplate> findByDoctorId(long doctorId) throws SQLException {
        String sql = "SELECT id, doctor_id, name, items, created_at FROM prescription_templates WHERE doctor_id = ? ORDER BY created_at DESC";
        Connection conn = ConnectionManager.getConnection();
        List<PrescriptionTemplate> templates = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                templates.add(mapRow(rs));
            }
            return templates;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public List<PrescriptionTemplate> findAll() throws SQLException {
        String sql = "SELECT id, doctor_id, name, items, created_at FROM prescription_templates ORDER BY created_at DESC";
        Connection conn = ConnectionManager.getConnection();
        List<PrescriptionTemplate> templates = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                templates.add(mapRow(rs));
            }
            return templates;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM prescription_templates WHERE id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    private PrescriptionTemplate mapRow(ResultSet rs) throws SQLException {
        long doctorIdRaw = rs.getLong("doctor_id");
        Long doctorId = rs.wasNull() ? null : doctorIdRaw;
        return new PrescriptionTemplate(
            rs.getLong("id"),
            doctorId,
            rs.getString("name"),
            rs.getString("items"),
            rs.getString("created_at")
        );
    }
}