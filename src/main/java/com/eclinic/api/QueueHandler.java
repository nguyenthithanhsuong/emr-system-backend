package com.eclinic.api;

import com.sun.net.httpserver.HttpExchange;
import com.eclinic.database.ConnectionManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QueueHandler extends BaseHandler {

    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = normalizePath(exchange.getRequestURI().getPath());

        // RBAC: write operations require RECEPTIONIST or ADMIN
        if (!"GET".equals(method)) {
            if (!requireRole(exchange, "RECEPTIONIST", "ADMIN", "DOCTOR")) return;
        }

        try {
            if ("GET".equals(method)) {
                handleGetQueue(exchange);
            } else if ("POST".equals(method)) {
                if (path.endsWith("/remove")) {
                    long id = extractIdBeforeSegment(path, "/remove");
                    handleRemove(exchange, id);
                } else if (path.matches(".*/\\d+/patient")) {
                    long id = extractIdBeforeSegment(path, "/patient");
                    handleUpdatePatientId(exchange, id);
                } else {
                    handleEnqueue(exchange);
                }
            } else if ("DELETE".equals(method)) {
                long id = parseId(path, "/api/queue/");
                handleRemove(exchange, id);
            } else {
                sendError(exchange, "Method not allowed", 405);
            }
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 500);
        }
    }

    private void handleGetQueue(HttpExchange exchange) throws Exception {
        String sql = "SELECT id, patient_id, patient_name, medical_history_number, appointment_id, source, status, enqueued_at " +
            "FROM patient_queue WHERE status = 'WAITING' ORDER BY enqueued_at ASC";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("{");
                sb.append("\"id\": ").append(rs.getLong("id")).append(", ");
                long patientId = rs.getLong("patient_id");
                if (rs.wasNull()) {
                    sb.append("\"patientId\": null, ");
                } else {
                    sb.append("\"patientId\": ").append(patientId).append(", ");
                }
                sb.append("\"patientName\": \"").append(escapeJson(rs.getString("patient_name"))).append("\", ");
                sb.append("\"medicalHistoryNumber\": \"").append(escapeJson(rs.getString("medical_history_number") != null ? rs.getString("medical_history_number") : "")).append("\", ");
                long appointmentId = rs.getLong("appointment_id");
                if (rs.wasNull()) {
                    sb.append("\"appointmentId\": null, ");
                } else {
                    sb.append("\"appointmentId\": ").append(appointmentId).append(", ");
                }
                sb.append("\"source\": \"").append(escapeJson(rs.getString("source"))).append("\", ");
                sb.append("\"status\": \"").append(escapeJson(rs.getString("status"))).append("\", ");
                sb.append("\"enqueuedAt\": \"").append(escapeJson(rs.getString("enqueued_at"))).append("\"");
                sb.append("}");
            }
            sb.append("]");
            sendJson(exchange, sb.toString(), 200);
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    private void handleEnqueue(HttpExchange exchange) throws Exception {
        String body = readBody(exchange);
        String patientName = extractString(body, "patientName");
        String medicalHistoryNumber = extractString(body, "medicalHistoryNumber");
        String source = extractString(body, "source");
        long patientId = extractLong(body, "patientId");
        long appointmentId = extractLong(body, "appointmentId");

        if (patientName.isEmpty()) {
            sendError(exchange, "patientName is required", 400);
            return;
        }
        if (source.isEmpty()) source = "WALK_IN";

        String sql = "INSERT INTO patient_queue (patient_id, patient_name, medical_history_number, appointment_id, source) VALUES (?, ?, ?, ?, ?) RETURNING id, enqueued_at";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (patientId > 0) stmt.setLong(1, patientId); else stmt.setNull(1, java.sql.Types.BIGINT);
            stmt.setString(2, patientName);
            stmt.setString(3, medicalHistoryNumber);
            if (appointmentId > 0) stmt.setLong(4, appointmentId); else stmt.setNull(4, java.sql.Types.BIGINT);
            stmt.setString(5, source);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long id = rs.getLong("id");
                String enqueuedAt = rs.getString("enqueued_at");
                String json = "{\"id\": " + id + ", \"patientName\": \"" + escapeJson(patientName) + "\", \"enqueuedAt\": \"" + escapeJson(enqueuedAt) + "\", \"status\": \"created\"}";
                sendJson(exchange, json, 201);
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    private void handleUpdatePatientId(HttpExchange exchange, long queueId) throws Exception {
        String body = readBody(exchange);
        long patientId = extractLong(body, "patientId");
        if (patientId <= 0) {
            sendError(exchange, "patientId is required", 400);
            return;
        }

        String sql = "UPDATE patient_queue SET patient_id = ? WHERE id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, patientId);
            stmt.setLong(2, queueId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                sendJson(exchange, "{\"status\": \"updated\"}", 200);
            } else {
                sendError(exchange, "Queue item not found", 404);
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    private void handleRemove(HttpExchange exchange, long id) throws Exception {
        String sql = "UPDATE patient_queue SET status = 'DONE' WHERE id = ?";
        Connection conn = ConnectionManager.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                sendJson(exchange, "{\"status\": \"removed\"}", 200);
            } else {
                sendError(exchange, "Queue item not found", 404);
            }
        } finally {
            ConnectionManager.closeConnection(conn);
        }
    }

    private long extractLong(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx == -1) return 0;
        int end = json.indexOf(",", idx);
        if (end == -1) end = json.indexOf("}", idx);
        String num = json.substring(idx + search.length(), end).trim();
        try { return Long.parseLong(num); } catch (Exception e) { return 0; }
    }

    private String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx == -1) return "";
        int start = idx + search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return json.substring(start, end);
    }

    private String normalizePath(String path) {
        if (path == null) return "";
        String normalized = path.split("\\?")[0];
        while (normalized.length() > 1 && normalized.endsWith("/"))
            normalized = normalized.substring(0, normalized.length() - 1);
        return normalized;
    }

    private long parseId(String path, String prefix) {
        return Long.parseLong(path.substring(prefix.length()));
    }

    private long extractIdBeforeSegment(String path, String segment) {
        int segIdx = path.indexOf(segment);
        String before = path.substring(0, segIdx);
        String[] parts = before.split("/");
        return Long.parseLong(parts[parts.length - 1]);
    }
}
