package com.eclinic.dao;

import com.eclinic.database.ConnectionManager;
import com.eclinic.models.Doctor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    public long create(long userId, String fullName, String specialty, String phone, String email, String roomNumber) throws SQLException {
        String sql = "INSERT INTO doctors (user_id, full_name, specialty, phone, email, room_number) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            stmt.setString(2, fullName);
            stmt.setString(3, specialty);
            stmt.setString(4, phone);
            stmt.setString(5, email);
            stmt.setString(6, roomNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to create doctor.");
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public Doctor findById(long id) throws SQLException {
        String sql = "SELECT id, user_id, full_name, specialty, phone, email, room_number, created_at FROM doctors WHERE id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Doctor(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getString("full_name"),
                    rs.getString("specialty"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("room_number"),
                    rs.getString("created_at")
                );
            }
            return null;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public List findAll() throws SQLException {
        String sql = "SELECT id, user_id, full_name, specialty, phone, email, room_number, created_at FROM doctors";
        Connection conn = ConnectionManager.getConnection();
        List doctors = new ArrayList();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                doctors.add(new Doctor(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getString("full_name"),
                    rs.getString("specialty"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("room_number"),
                    rs.getString("created_at")
                ));
            }
            return doctors;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public boolean update(long id, String fullName, String specialty, String email, String roomNumber) throws SQLException {
        String sql = "UPDATE doctors SET full_name = ?, specialty = ?, email = ?, room_number = ? WHERE id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, fullName);
            stmt.setString(2, specialty);
            stmt.setString(3, email);
            stmt.setString(4, roomNumber);
            stmt.setLong(5, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM doctors WHERE id = ?";
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
}
