package com.eclinic.api;

import com.sun.net.httpserver.HttpExchange;
import com.eclinic.dao.PrescriptionTemplateDAO;
import com.eclinic.models.PrescriptionTemplate;
import java.io.IOException;
import java.util.List;

public class PrescriptionTemplatesHandler extends BaseHandler {

    private final PrescriptionTemplateDAO dao = new PrescriptionTemplateDAO();

    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = normalizePath(exchange.getRequestURI().getPath());
        String query = exchange.getRequestURI().getQuery();

        // RBAC: only DOCTOR and ADMIN can manage prescription templates
        if (!requireRole(exchange, "DOCTOR", "ADMIN")) return;

        try {
            if ("GET".equals(method)) {
                if (query != null && query.contains("doctorId=")) {
                    long doctorId = parseQueryLong(query, "doctorId");
                    handleGetByDoctor(exchange, doctorId);
                } else {
                    handleGetAll(exchange);
                }
            } else if ("POST".equals(method)) {
                handleCreate(exchange);
            } else if ("DELETE".equals(method)) {
                long id = parseId(path, "/api/prescription-templates/");
                handleDelete(exchange, id);
            } else {
                sendError(exchange, "Method not allowed", 405);
            }
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 500);
        }
    }

    private void handleGetAll(HttpExchange exchange) throws Exception {
        List<PrescriptionTemplate> templates = dao.findAll();
        sendJson(exchange, toJsonArray(templates), 200);
    }

    private void handleGetByDoctor(HttpExchange exchange, long doctorId) throws Exception {
        List<PrescriptionTemplate> templates = dao.findByDoctorId(doctorId);
        sendJson(exchange, toJsonArray(templates), 200);
    }

    private void handleCreate(HttpExchange exchange) throws Exception {
        String body = readBody(exchange);
        long doctorIdRaw = extractLong(body, "doctorId");
        String name = extractString(body, "name");
        String items = extractJsonArray(body, "items");

        if (name.isEmpty()) {
            sendError(exchange, "Name is required", 400);
            return;
        }

        // doctorId is nullable in the DB; treat a missing/0 value as null
        // rather than silently inserting 0 as a real doctor id.
        Long doctorId = doctorIdRaw == 0 ? null : doctorIdRaw;

        long id = dao.create(doctorId, name, items);
        sendJson(exchange, "{\"id\": " + id + ", \"status\": \"created\"}", 201);
    }

    private void handleDelete(HttpExchange exchange, long id) throws Exception {
        // TODO: verify the requesting doctor owns this template before deleting,
        // e.g. compare dao.findById(id).getDoctorId() against the caller's id
        // from the session/JWT, once BaseHandler exposes that. Currently any
        // DOCTOR/ADMIN can delete any template.
        boolean deleted = dao.delete(id);
        if (deleted) {
            sendJson(exchange, "{\"status\": \"deleted\"}", 200);
        } else {
            sendError(exchange, "Template not found", 404);
        }
    }

    private String toJsonArray(List<PrescriptionTemplate> templates) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (PrescriptionTemplate t : templates) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{");
            sb.append("\"id\": ").append(t.getId()).append(", ");
            sb.append("\"doctorId\": ").append(t.getDoctorId() == null ? "null" : t.getDoctorId()).append(", ");
            sb.append("\"name\": \"").append(escapeJson(t.getName())).append("\", ");
            sb.append("\"items\": ").append(t.getItems()).append(", ");
            sb.append("\"createdAt\": \"").append(escapeJson(t.getCreatedAt())).append("\"");
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String extractJsonArray(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx == -1) return "[]";
        int start = json.indexOf("[", idx);
        if (start == -1) return "[]";
        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '[') depth++;
            else if (json.charAt(i) == ']') depth--;
            if (depth == 0) return json.substring(start, i + 1);
        }
        return "[]";
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

    private long parseQueryLong(String query, String key) {
        String[] parts = query.split("&");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && key.equals(kv[0])) return Long.parseLong(kv[1]);
        }
        return 0;
    }
}