package com.eclinic.dao;

import com.eclinic.database.ConnectionManager;
import com.eclinic.models.MedicalRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordDAO {

    public long create(Long appointmentId, String symptoms, String diagnosis, String recordType, String treatmentPlan) throws SQLException {
        String sql = "INSERT INTO medical_records (appointment_id, symptoms, diagnosis, record_type, treatment_plan) VALUES (?, ?, ?, ?, ?) RETURNING id";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (appointmentId != null && appointmentId > 0) {
                stmt.setLong(1, appointmentId);
            } else {
                stmt.setNull(1, java.sql.Types.BIGINT);
            }
            stmt.setString(2, symptoms);
            stmt.setString(3, diagnosis);
            stmt.setString(4, recordType);
            stmt.setString(5, treatmentPlan);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to create medical record.");
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public MedicalRecord findById(long id) throws SQLException {
        String sql = "SELECT mr.id, mr.appointment_id, mr.symptoms, mr.diagnosis, mr.record_type, mr.treatment_plan, mr.created_at, COALESCE(MAX(p.full_name), MAX(pq.patient_name)) AS patient_name " +
                     "FROM medical_records mr " +
                     "LEFT JOIN appointments a ON mr.appointment_id = a.id " +
                     "LEFT JOIN patients p ON a.patient_id = p.id " +
                     "LEFT JOIN patient_queue pq ON pq.appointment_id = mr.appointment_id " +
                     "WHERE mr.id = ? " +
                     "GROUP BY mr.id, mr.appointment_id, mr.symptoms, mr.diagnosis, mr.record_type, mr.treatment_plan, mr.created_at";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                MedicalRecord mr = new MedicalRecord(
                    rs.getLong("id"),
                    rs.getLong("appointment_id"),
                    rs.getString("symptoms"),
                    rs.getString("diagnosis"),
                    rs.getString("record_type"),
                    rs.getString("treatment_plan"),
                    rs.getString("created_at")
                );
                mr.setPatientName(rs.getString("patient_name"));
                return mr;
            }
            return null;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public MedicalRecord findByAppointmentId(long appointmentId) throws SQLException {
        String sql = "SELECT mr.id, mr.appointment_id, mr.symptoms, mr.diagnosis, mr.record_type, mr.treatment_plan, mr.created_at, COALESCE(MAX(p.full_name), MAX(pq.patient_name)) AS patient_name " +
                     "FROM medical_records mr " +
                     "LEFT JOIN appointments a ON mr.appointment_id = a.id " +
                     "LEFT JOIN patients p ON a.patient_id = p.id " +
                     "LEFT JOIN patient_queue pq ON pq.appointment_id = mr.appointment_id " +
                     "WHERE mr.appointment_id = ? " +
                     "GROUP BY mr.id, mr.appointment_id, mr.symptoms, mr.diagnosis, mr.record_type, mr.treatment_plan, mr.created_at";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, appointmentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                MedicalRecord mr = new MedicalRecord(
                    rs.getLong("id"),
                    rs.getLong("appointment_id"),
                    rs.getString("symptoms"),
                    rs.getString("diagnosis"),
                    rs.getString("record_type"),
                    rs.getString("treatment_plan"),
                    rs.getString("created_at")
                );
                mr.setPatientName(rs.getString("patient_name"));
                return mr;
            }
            return null;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public List findAll() throws SQLException {
        String sql = "SELECT mr.id, mr.appointment_id, mr.symptoms, mr.diagnosis, mr.record_type, mr.treatment_plan, mr.created_at, COALESCE(MAX(p.full_name), MAX(pq.patient_name)) AS patient_name " +
                     "FROM medical_records mr " +
                     "LEFT JOIN appointments a ON mr.appointment_id = a.id " +
                     "LEFT JOIN patients p ON a.patient_id = p.id " +
                     "LEFT JOIN patient_queue pq ON pq.appointment_id = mr.appointment_id " +
                     "GROUP BY mr.id, mr.appointment_id, mr.symptoms, mr.diagnosis, mr.record_type, mr.treatment_plan, mr.created_at " +
                     "ORDER BY mr.created_at DESC";
        Connection conn = ConnectionManager.getConnection();
        List records = new ArrayList();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MedicalRecord mr = new MedicalRecord(
                    rs.getLong("id"),
                    rs.getLong("appointment_id"),
                    rs.getString("symptoms"),
                    rs.getString("diagnosis"),
                    rs.getString("record_type"),
                    rs.getString("treatment_plan"),
                    rs.getString("created_at")
                );
                mr.setPatientName(rs.getString("patient_name"));
                records.add(mr);
            }
            return records;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public List findByPatientId(long patientId) throws SQLException {
        String sql = "SELECT mr.id, mr.appointment_id, mr.symptoms, mr.diagnosis, mr.record_type, mr.treatment_plan, mr.created_at, p.full_name AS patient_name " +
                     "FROM medical_records mr " +
                     "INNER JOIN appointments a ON mr.appointment_id = a.id AND a.patient_id = ? " +
                     "INNER JOIN patients p ON a.patient_id = p.id " +
                     "ORDER BY mr.created_at DESC";
        Connection conn = ConnectionManager.getConnection();
        List records = new ArrayList();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MedicalRecord mr = new MedicalRecord(
                    rs.getLong("id"),
                    rs.getLong("appointment_id"),
                    rs.getString("symptoms"),
                    rs.getString("diagnosis"),
                    rs.getString("record_type"),
                    rs.getString("treatment_plan"),
                    rs.getString("created_at")
                );
                mr.setPatientName(rs.getString("patient_name"));
                records.add(mr);
            }
            return records;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public boolean update(long id, String diagnosis, String treatmentPlan) throws SQLException {
        String sql = "UPDATE medical_records SET diagnosis = ?, treatment_plan = ? WHERE id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, diagnosis);
            stmt.setString(2, treatmentPlan);
            stmt.setLong(3, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM medical_records WHERE id = ?";
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
