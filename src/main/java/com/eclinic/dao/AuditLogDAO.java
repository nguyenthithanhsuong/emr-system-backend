package com.eclinic.dao;

import com.eclinic.database.ConnectionManager;
import com.eclinic.models.AuditLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public void log(String action, String actor, String target) throws SQLException {
        String sql = "INSERT INTO audit_logs (action, actor, target) VALUES (?, ?, ?)";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, action);
            stmt.setString(2, actor);
            stmt.setString(3, target);
            stmt.executeUpdate();
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public List findRecent(int limit) throws SQLException {
        String sql = "SELECT id, action, actor, target, created_at FROM audit_logs ORDER BY created_at DESC LIMIT ?";
        Connection conn = ConnectionManager.getConnection();
        List logs = new ArrayList();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(new AuditLog(
                    rs.getLong("id"),
                    rs.getString("action"),
                    rs.getString("actor"),
                    rs.getString("target"),
                    rs.getString("created_at")
                ));
            }
            return logs;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    public List findRecentByTimeframe(int limit, String timeframe, String startDate, String endDate) throws SQLException {
        String dateCond = "DATE(created_at) = CURRENT_DATE";
        boolean isCustom = false;
        
        if ("week".equals(timeframe)) {
            dateCond = "created_at >= CURRENT_DATE - INTERVAL '7 days'";
        } else if ("month".equals(timeframe)) {
            dateCond = "created_at >= CURRENT_DATE - INTERVAL '30 days'";
        } else if ("year".equals(timeframe)) {
            dateCond = "EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE)";
        } else if ("all".equals(timeframe)) {
            dateCond = "1 = 1";
        } else if ("custom".equals(timeframe) && startDate != null && endDate != null) {
            dateCond = "DATE(created_at) >= CAST(? AS DATE) AND DATE(created_at) <= CAST(? AS DATE)";
            isCustom = true;
        }

        String sql = "SELECT id, action, actor, target, created_at FROM audit_logs WHERE " + dateCond + " ORDER BY created_at DESC LIMIT ?";
        Connection conn = ConnectionManager.getConnection();
        List logs = new ArrayList();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            int paramIndex = 1;
            
            if (isCustom) {
                stmt.setString(paramIndex++, startDate);
                stmt.setString(paramIndex++, endDate);
            }
            stmt.setInt(paramIndex++, limit);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(new AuditLog(
                    rs.getLong("id"),
                    rs.getString("action"),
                    rs.getString("actor"),
                    rs.getString("target"),
                    rs.getString("created_at")
                ));
            }
            return logs;
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }
}
