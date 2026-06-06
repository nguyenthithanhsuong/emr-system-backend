package com.eclinic.api;

import com.sun.net.httpserver.HttpExchange;
import com.eclinic.dao.MedicineDAO;
import com.eclinic.models.Medicine;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.List;

public class MedicinesHandler extends BaseHandler {

    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = normalizePath(exchange.getRequestURI().getPath());
        String query = exchange.getRequestURI().getQuery();

        // RBAC: write operations require ADMIN; GET available to all authenticated
        if (!"GET".equals(method)) {
            if (!requireRole(exchange, "ADMIN")) return;
        }

        MedicineDAO dao = new MedicineDAO();

        try {
            if ("GET".equals(method)) {
                if (path.matches("/api/medicines/\\d+")) {
                    long id = Long.parseLong(path.substring(15));
                    Medicine med = dao.findById(id);
                    if (med != null) {
                        sendJson(exchange, toJson(med), 200);
                    } else {
                        sendError(exchange, "Medicine not found", 404);
                    }
                } else {
                    // Parse search params
                    String searchTerm = parseQueryString(query, "search");
                    Long categoryId = parseQueryLong(query, "categoryId");

                    List<Medicine> medicines;
                    if (searchTerm != null || categoryId != null) {
                        medicines = dao.search(searchTerm, categoryId);
                    } else {
                        medicines = dao.findAll();
                    }
                    sendJson(exchange, listToJson(medicines), 200);
                }
            } else if ("POST".equals(method)) {
                String body = readBody(exchange);
                String name = extractString(body, "name");
                String unit = extractString(body, "unit");
                BigDecimal price = new BigDecimal(extractString(body, "price"));
                int stock = Integer.parseInt(extractString(body, "stockQuantity"));
                String expiry = extractString(body, "expiryDate");
                Long categoryId = extractNullableLong(body, "categoryId");

                long id = dao.create(name, unit, price, stock, expiry, categoryId);
                sendJson(exchange, "{\"id\": " + id + ", \"status\": \"created\"}", 201);
            } else if ("PUT".equals(method)) {
                long id = Long.parseLong(path.substring(15));
                String body = readBody(exchange);
                int newQuantity = Integer.parseInt(extractString(body, "stockQuantity"));

                boolean updated = dao.updateStock(id, newQuantity);
                if (updated) {
                    sendJson(exchange, "{\"status\": \"updated\"}", 200);
                } else {
                    sendError(exchange, "Medicine not found", 404);
                }
            } else if ("DELETE".equals(method)) {
                long id = Long.parseLong(path.substring(15));
                boolean deleted = dao.delete(id);
                if (deleted) {
                    sendJson(exchange, "{\"status\": \"deleted\"}", 200);
                } else {
                    sendError(exchange, "Medicine not found", 404);
                }
            } else {
                sendError(exchange, "Method not allowed", 405);
            }
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 500);
        }
    }

    private String toJson(Medicine m) {
        if (m == null) return "null";
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\": ").append(m.getId()).append(", ");
        sb.append("\"name\": \"").append(escapeJson(m.getName())).append("\", ");
        sb.append("\"unit\": \"").append(escapeJson(m.getUnit())).append("\", ");
        sb.append("\"price\": ").append(m.getPrice()).append(", ");
        sb.append("\"stockQuantity\": ").append(m.getStockQuantity()).append(", ");
        sb.append("\"expiryDate\": \"").append(escapeJson(m.getExpiryDate())).append("\", ");
        sb.append("\"categoryId\": ").append(m.getCategoryId() != null ? m.getCategoryId() : "null").append(", ");
        sb.append("\"categoryName\": ").append(m.getCategoryName() != null ? "\"" + escapeJson(m.getCategoryName()) + "\"" : "null").append(", ");
        sb.append("\"categoryNameVi\": ").append(m.getCategoryNameVi() != null ? "\"" + escapeJson(m.getCategoryNameVi()) + "\"" : "null");
        sb.append("}");
        return sb.toString();
    }

    private String listToJson(List<Medicine> medicines) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < medicines.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(medicines.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx == -1) {
            search = "\"" + key + "\":";
            idx = json.indexOf(search);
            if (idx == -1) return "";
            int start = idx + search.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
        int start = idx + search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return json.substring(start, end);
    }

    private Long extractNullableLong(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx == -1) return null;
        int start = idx + search.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        String raw = json.substring(start, end).trim();
        if (raw.equals("null") || raw.isEmpty()) return null;
        try { long v = Long.parseLong(raw); return v > 0 ? v : null; }
        catch (Exception e) { return null; }
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) return "";
        String normalized = path.split("\\?")[0];
        while (normalized.length() > 1 && normalized.endsWith("/"))
            normalized = normalized.substring(0, normalized.length() - 1);
        return normalized;
    }

    private String parseQueryString(String query, String key) {
        if (query == null || query.isEmpty()) return null;
        String[] parts = query.split("&");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && key.equals(kv[0])) {
                try { String decoded = URLDecoder.decode(kv[1], "UTF-8"); return decoded.isEmpty() ? null : decoded; }
                catch (Exception e) { return kv[1].isEmpty() ? null : kv[1]; }
            }
        }
        return null;
    }

    private Long parseQueryLong(String query, String key) {
        if (query == null || query.isEmpty()) return null;
        String[] parts = query.split("&");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && key.equals(kv[0])) {
                try { return Long.parseLong(kv[1]); }
                catch (Exception e) { return null; }
            }
        }
        return null;
    }
}
