package com.eclinic.dao;

import com.eclinic.database.ConnectionManager;
import com.eclinic.models.Appointment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public long create(long doctorId, long patientId, String startDate, String endDate, String reason, String status) throws SQLException {
        String sql = "INSERT INTO appointments (doctor_id, patient_id, appointment_start_date, appointment_end_date, reason, status) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, doctorId);
            stmt.setLong(2, patientId);
            stmt.setTimestamp(3, parseTimestamp(startDate));
            stmt.setTimestamp(4, parseTimestamp(endDate));
            stmt.setString(5, reason);
            stmt.setString(6, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to create appointment.");
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public Appointment findById(long id) throws SQLException {
        String sql = "SELECT id, doctor_id, patient_id, appointment_start_date, appointment_end_date, reason, status, created_at FROM appointments WHERE id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Appointment(
                    rs.getLong("id"),
                    rs.getLong("doctor_id"),
                    rs.getLong("patient_id"),
                    rs.getString("appointment_start_date"),
                    rs.getString("appointment_end_date"),
                    rs.getString("reason"),
                    rs.getString("status"),
                    rs.getString("created_at")
                );
            }
            return null;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public List findByPatientId(long patientId) throws SQLException {
        String sql = "SELECT id, doctor_id, patient_id, appointment_start_date, appointment_end_date, reason, status, created_at FROM appointments WHERE patient_id = ? ORDER BY appointment_start_date DESC";
        Connection conn = ConnectionManager.getConnection();
        List appointments = new ArrayList();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                appointments.add(new Appointment(
                    rs.getLong("id"),
                    rs.getLong("doctor_id"),
                    rs.getLong("patient_id"),
                    rs.getString("appointment_start_date"),
                    rs.getString("appointment_end_date"),
                    rs.getString("reason"),
                    rs.getString("status"),
                    rs.getString("created_at")
                ));
            }
            return appointments;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public List findAll() throws SQLException {
        String sql = "SELECT id, doctor_id, patient_id, appointment_start_date, appointment_end_date, reason, status, created_at FROM appointments ORDER BY appointment_start_date DESC";
        Connection conn = ConnectionManager.getConnection();
        List appointments = new ArrayList();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                appointments.add(new Appointment(
                    rs.getLong("id"),
                    rs.getLong("doctor_id"),
                    rs.getLong("patient_id"),
                    rs.getString("appointment_start_date"),
                    rs.getString("appointment_end_date"),
                    rs.getString("reason"),
                    rs.getString("status"),
                    rs.getString("created_at")
                ));
            }
            return appointments;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public boolean updateStatus(long id, String newStatus) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setLong(2, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM appointments WHERE id = ?";
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

    public boolean reschedule(long id, String startDate, String endDate, String status) throws SQLException {
    String sql = "UPDATE appointments SET appointment_start_date = ?, appointment_end_date = ?, status = ? WHERE id = ?";
    Connection conn = ConnectionManager.getConnection();
    try {
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setTimestamp(1, parseTimestamp(startDate));
        stmt.setTimestamp(2, parseTimestamp(endDate));
        stmt.setString(3, status);
        stmt.setLong(4, id);
        int rows = stmt.executeUpdate();
        return rows > 0;
    } finally {
        ConnectionManager.closeConnection(conn);
    }
}

public boolean reschedule(long id, String startDate, String endDate, String status, String reason) throws SQLException {
    String sql = "UPDATE appointments SET appointment_start_date = ?, appointment_end_date = ?, status = ?, reason = ? WHERE id = ?";
    Connection conn = ConnectionManager.getConnection();
    try {
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setTimestamp(1, parseTimestamp(startDate));
        stmt.setTimestamp(2, parseTimestamp(endDate));
        stmt.setString(3, status);
        stmt.setString(4, reason);
        stmt.setLong(5, id);
        int rows = stmt.executeUpdate();
        return rows > 0;
    } finally {
        ConnectionManager.closeConnection(conn);
    }
}

    /**
     * Parse a date string that may be ISO-8601 (from frontend) or 'yyyy-MM-dd HH:mm:ss' (legacy).
     */
    private Timestamp parseTimestamp(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }
        try {
            // Try ISO-8601 with offset (e.g. 2024-01-15T10:00:00.000Z or +07:00)
            OffsetDateTime odt = OffsetDateTime.parse(dateStr);
            return Timestamp.from(odt.toInstant());
        } catch (DateTimeParseException e1) {
            try {
                // Try ISO-8601 without offset (e.g. 2024-01-15T10:00:00)
                LocalDateTime ldt = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return Timestamp.valueOf(ldt);
            } catch (DateTimeParseException e2) {
                // Fallback to legacy format: yyyy-MM-dd HH:mm:ss
                return Timestamp.valueOf(dateStr);
            }
        }
    }
}
