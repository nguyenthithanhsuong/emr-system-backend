package com.eclinic.api;

import com.sun.net.httpserver.HttpExchange;
import com.eclinic.dao.PatientDAO;
import com.eclinic.models.Patient;
import java.io.IOException;
import java.util.List;

public class PatientsHandler extends BaseHandler {

    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        PatientDAO dao = new PatientDAO();

        try {
            if ("GET".equals(method)) {
                if (path.matches("/api/patients/\\d+")) {
                    long id = Long.parseLong(path.substring(13));
                    Patient patient = dao.findById(id);
                    if (patient != null) {
                        String json = toJson(patient);
                        sendJson(exchange, json, 200);
                    } else {
                        sendError(exchange, "Patient not found", 404);
                    }
                } else {
                    List allPatients = dao.findAll();
                    String json = listToJson(allPatients);
                    sendJson(exchange, json, 200);
                }
            } else if ("POST".equals(method)) {
                String body = readBody(exchange);
                Long userId = null;
                String userIdStr = extractString(body, "userId");
                if (userIdStr != null && !userIdStr.equals("")) {
                    userId = Long.parseLong(userIdStr);
                }
                String fullName = extractString(body, "fullName");
                String dob = extractString(body, "dob");
                String gender = extractString(body, "gender");
                String phone = extractString(body, "phone");
                String address = extractString(body, "address");
                String insuranceCode = extractString(body, "insuranceCode");

                long id = dao.create(userId, fullName, dob, gender, phone, address, insuranceCode);
                String json = "{\"id\": " + id + ", \"status\": \"created\"}";
                sendJson(exchange, json, 201);
            } else if ("PUT".equals(method)) {
                long id = Long.parseLong(path.substring(13));
                String body = readBody(exchange);
                String fullName = extractString(body, "fullName");
                String phone = extractString(body, "phone");
                String address = extractString(body, "address");

                boolean updated = dao.update(id, fullName, phone, address);
                if (updated) {
                    sendJson(exchange, "{\"status\": \"updated\"}", 200);
                } else {
                    sendError(exchange, "Patient not found", 404);
                }
            } else if ("DELETE".equals(method)) {
                long id = Long.parseLong(path.substring(13));
                boolean deleted = dao.delete(id);
                if (deleted) {
                    sendJson(exchange, "{\"status\": \"deleted\"}", 200);
                } else {
                    sendError(exchange, "Patient not found", 404);
                }
            } else {
                sendError(exchange, "Method not allowed", 405);
            }
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 500);
        }
    }

    private String toJson(Patient p) {
        if (p == null) return "null";
        return "{" +
            "\"id\": " + p.getId() + ", " +
            "\"userId\": " + (p.getUserId() != null ? p.getUserId() : "null") + ", " +
            "\"fullName\": \"" + escapeJson(p.getFullName()) + "\", " +
            "\"dob\": \"" + escapeJson(p.getDob()) + "\", " +
            "\"gender\": \"" + escapeJson(p.getGender()) + "\", " +
            "\"phone\": \"" + escapeJson(p.getPhone()) + "\", " +
            "\"address\": \"" + escapeJson(p.getAddress()) + "\", " +
            "\"insuranceCode\": \"" + escapeJson(p.getInsuranceCode()) + "\", " +
            "\"createdAt\": \"" + escapeJson(p.getCreatedAt()) + "\"" +
            "}";
    }

    private String listToJson(List patients) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < patients.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson((Patient) patients.get(i)));
        }
        sb.append("]");
        return sb.toString();
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
}
