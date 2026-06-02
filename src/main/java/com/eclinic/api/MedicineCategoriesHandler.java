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
            } else {
                sendError(exchange, "Method not allowed", 405);
            }
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 500);
        }
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
