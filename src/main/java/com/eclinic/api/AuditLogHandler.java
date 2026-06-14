package com.eclinic.api;

import com.sun.net.httpserver.HttpExchange;
import com.eclinic.dao.AuditLogDAO;
import com.eclinic.models.AuditLog;
import java.io.IOException;
import java.util.List;

public class AuditLogHandler extends BaseHandler {

    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        // RBAC: only ADMIN can view audit logs
        if (!requireRole(exchange, "ADMIN")) return;

        if (!"GET".equals(method)) {
            sendError(exchange, "Method not allowed", 405);
            return;
        }

        try {
            String timeframe = "all";
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("timeframe=")) {
                for (String param : query.split("&")) {
                    if (param.startsWith("timeframe=")) {
                        timeframe = param.substring("timeframe=".length());
                    }
                }
            }

            AuditLogDAO dao = new AuditLogDAO();
            List logs = dao.findRecentByTimeframe(50, timeframe);
            String json = listToJson(logs);
            sendJson(exchange, json, 200);
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 500);
        }
    }

    private String listToJson(List logs) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < logs.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson((AuditLog) logs.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJson(AuditLog log) {
        return "{" +
            "\"id\": " + log.getId() + ", " +
            "\"action\": \"" + escapeJson(log.getAction()) + "\", " +
            "\"actor\": \"" + escapeJson(log.getActor()) + "\", " +
            "\"target\": \"" + escapeJson(log.getTarget()) + "\", " +
            "\"timestamp\": \"" + escapeJson(log.getCreatedAt()) + "\"" +
            "}";
    }
}
