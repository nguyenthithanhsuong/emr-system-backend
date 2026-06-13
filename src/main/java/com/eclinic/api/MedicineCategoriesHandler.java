package com.eclinic.api;

import com.sun.net.httpserver.HttpExchange;
import com.eclinic.dao.MedicineCategoryDAO;
import com.eclinic.models.MedicineCategory;
import java.io.IOException;
import java.util.List;

public class MedicineCategoriesHandler extends BaseHandler {

    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        MedicineCategoryDAO dao = new MedicineCategoryDAO();

        try {
            if ("GET".equals(method)) {
                List<MedicineCategory> categories = dao.findAll();
                String json = listToJson(categories);
                sendJson(exchange, json, 200);
            } else if ("POST".equals(method)) {
                if (!requireRole(exchange, "ADMIN")) return;
                String body = readBody(exchange);
                String name = extractString(body, "name");
                String nameVi = extractString(body, "nameVi");
                String description = extractString(body, "description");
                int displayOrder = Integer.parseInt(extractString(body, "displayOrder"));

                long id = dao.create(name, nameVi, description, displayOrder);
                sendJson(exchange, "{\"id\": " + id + ", \"status\": \"created\"}", 201);
            } else if ("PUT".equals(method)) {
                if (!requireRole(exchange, "ADMIN")) return;
                String path = exchange.getRequestURI().getPath();
                long id = Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
                String body = readBody(exchange);
                String name = extractString(body, "name");
                String nameVi = extractString(body, "nameVi");
                String description = extractString(body, "description");
                int displayOrder = Integer.parseInt(extractString(body, "displayOrder"));

                boolean updated = dao.update(id, name, nameVi, description, displayOrder);
                if (updated) {
                    sendJson(exchange, "{\"status\": \"updated\"}", 200);
                } else {
                    sendError(exchange, "Category not found", 404);
                }
            } else if ("DELETE".equals(method)) {
                if (!requireRole(exchange, "ADMIN")) return;
                String path = exchange.getRequestURI().getPath();
                long id = Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
                
                boolean deleted = dao.delete(id);
                if (deleted) {
                    sendJson(exchange, "{\"status\": \"deleted\"}", 200);
                } else {
                    sendError(exchange, "Category not found", 404);
                }
            } else {
                sendError(exchange, "Method not allowed", 405);
            }
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 500);
        }
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

    private String toJson(MedicineCategory c) {
        if (c == null) return "null";
        return "{" +
            "\"id\": " + c.getId() + ", " +
            "\"name\": \"" + escapeJson(c.getName()) + "\", " +
            "\"nameVi\": \"" + escapeJson(c.getNameVi()) + "\", " +
            "\"description\": " + (c.getDescription() != null ? "\"" + escapeJson(c.getDescription()) + "\"" : "null") + ", " +
            "\"displayOrder\": " + c.getDisplayOrder() + ", " +
            "\"createdAt\": \"" + escapeJson(c.getCreatedAt() != null ? c.getCreatedAt() : "") + "\"" +
        "}";
    }

    private String listToJson(List<MedicineCategory> categories) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < categories.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(categories.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }
}
