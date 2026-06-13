package com.eclinic.api;

import com.sun.net.httpserver.HttpExchange;
import com.eclinic.dao.UserDAO;
import com.eclinic.dao.DoctorDAO;
import com.eclinic.dao.AppointmentDAO;
import com.eclinic.models.User;
import java.io.IOException;
import java.util.List;

public class DashboardHandler extends BaseHandler {

    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();


        if (!"GET".equals(method)) {
            sendError(exchange, "Method not allowed", 405);
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            DoctorDAO doctorDAO = new DoctorDAO();
            AppointmentDAO aptDAO = new AppointmentDAO();

            List allUsers = userDAO.findAll();
            List allDoctors = doctorDAO.findAll();

            int totalUsers = allUsers.size();
            int totalDoctors = allDoctors.size();
            int totalPatients = 0;
            for (int i = 0; i < allUsers.size(); i++) {
                User u = (User) allUsers.get(i);
                if ("PATIENT".equals(u.getRole())) {
                    totalPatients++;
                }
            }

            String timeframe = "today";
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("timeframe=")) {
                for (String param : query.split("&")) {
                    if (param.startsWith("timeframe=")) {
                        timeframe = param.substring("timeframe=".length());
                    }
                }
            }

            String aptDateCond = "DATE(appointment_start_date) = CURRENT_DATE";
            String payDateCond = "DATE(created_at) = CURRENT_DATE";
            String presDateCond = "DATE(p.created_at) = CURRENT_DATE";
            String userDateCond = "DATE(created_at) = CURRENT_DATE";

            if ("week".equals(timeframe)) {
                aptDateCond = "appointment_start_date >= CURRENT_DATE - INTERVAL '7 days'";
                payDateCond = "created_at >= CURRENT_DATE - INTERVAL '7 days'";
                presDateCond = "p.created_at >= CURRENT_DATE - INTERVAL '7 days'";
                userDateCond = "created_at >= CURRENT_DATE - INTERVAL '7 days'";
            } else if ("month".equals(timeframe)) {
                aptDateCond = "EXTRACT(MONTH FROM appointment_start_date) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM appointment_start_date) = EXTRACT(YEAR FROM CURRENT_DATE)";
                payDateCond = "EXTRACT(MONTH FROM created_at) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE)";
                presDateCond = "EXTRACT(MONTH FROM p.created_at) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM p.created_at) = EXTRACT(YEAR FROM CURRENT_DATE)";
                userDateCond = "EXTRACT(MONTH FROM created_at) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE)";
            } else if ("year".equals(timeframe)) {
                aptDateCond = "EXTRACT(YEAR FROM appointment_start_date) = EXTRACT(YEAR FROM CURRENT_DATE)";
                payDateCond = "EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE)";
                presDateCond = "EXTRACT(YEAR FROM p.created_at) = EXTRACT(YEAR FROM CURRENT_DATE)";
                userDateCond = "EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE)";
            } else if ("all".equals(timeframe)) {
                aptDateCond = "1 = 1";
                payDateCond = "1 = 1";
                presDateCond = "1 = 1";
                userDateCond = "1 = 1";
            }

            long periodAppointments = 0;
            long completedAppointments = 0;
            long newPatients = 0;
            java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;
            java.math.BigDecimal periodRevenue = java.math.BigDecimal.ZERO;
            long unpaidInvoices = 0;
            
            java.math.BigDecimal[] monthlyRevenue = new java.math.BigDecimal[12];
            for (int i = 0; i < 12; i++) {
                monthlyRevenue[i] = java.math.BigDecimal.ZERO;
            }

            StringBuilder topMedicinesJson = new StringBuilder("[");

            java.sql.Connection conn = com.eclinic.database.ConnectionManager.getConnection();
            try {
                // newPatients
                java.sql.PreparedStatement stmtP = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE role='PATIENT' AND " + userDateCond);
                java.sql.ResultSet rsP = stmtP.executeQuery();
                if (rsP.next()) newPatients = rsP.getLong(1);

                // periodAppointments & completedAppointments
                java.sql.PreparedStatement stmt1 = conn.prepareStatement("SELECT COUNT(*), SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) FROM appointments WHERE " + aptDateCond);
                java.sql.ResultSet rs1 = stmt1.executeQuery();
                if (rs1.next()) {
                    periodAppointments = rs1.getLong(1);
                    completedAppointments = rs1.getLong(2);
                }

                // revenue (total & period)
                java.sql.PreparedStatement stmt2 = conn.prepareStatement("SELECT SUM(amount), SUM(CASE WHEN " + payDateCond + " THEN amount ELSE 0 END) FROM payments WHERE payment_status = 'CONFIRMED'");
                java.sql.ResultSet rs2 = stmt2.executeQuery();
                if (rs2.next()) {
                    if (rs2.getBigDecimal(1) != null) totalRevenue = rs2.getBigDecimal(1);
                    if (rs2.getBigDecimal(2) != null) periodRevenue = rs2.getBigDecimal(2);
                }

                // monthlyRevenue
                java.sql.PreparedStatement stmt3 = conn.prepareStatement("SELECT EXTRACT(MONTH FROM created_at) AS month, SUM(amount) AS total FROM payments WHERE payment_status = 'CONFIRMED' AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE) GROUP BY EXTRACT(MONTH FROM created_at)");
                java.sql.ResultSet rs3 = stmt3.executeQuery();
                while (rs3.next()) {
                    int month = rs3.getInt("month");
                    java.math.BigDecimal total = rs3.getBigDecimal("total");
                    if (month >= 1 && month <= 12 && total != null) {
                        monthlyRevenue[month - 1] = total;
                    }
                }

                // unpaidInvoices
                java.sql.PreparedStatement stmt4 = conn.prepareStatement("SELECT COUNT(*) FROM prescriptions pres LEFT JOIN payments pay ON pay.prescription_id = pres.id WHERE pay.id IS NULL OR pay.payment_status != 'CONFIRMED'");
                java.sql.ResultSet rs4 = stmt4.executeQuery();
                if (rs4.next()) unpaidInvoices = rs4.getLong(1);
                // topMedicines
                String topMedSql = "SELECT m.name as name, m.unit as unit, SUM(pd.quantity) as total_quantity " +
                                   "FROM prescription_details pd " +
                                   "JOIN prescriptions p ON pd.prescription_id = p.id " +
                                   "JOIN payments pay ON pay.prescription_id = p.id " +
                                   "JOIN medicines m ON pd.medicine_id = m.id " +
                                   "WHERE pay.payment_status = 'CONFIRMED' AND " + presDateCond + " " +
                                   "GROUP BY m.id, m.name, m.unit " +
                                   "ORDER BY total_quantity DESC LIMIT 5";
                java.sql.PreparedStatement stmt5 = conn.prepareStatement(topMedSql);
                java.sql.ResultSet rs5 = stmt5.executeQuery();
                boolean firstMed = true;
                while (rs5.next()) {
                    if (!firstMed) topMedicinesJson.append(",");
                    topMedicinesJson.append("{");
                    topMedicinesJson.append("\"name\": \"").append(escapeJson(rs5.getString("name"))).append("\",");
                    topMedicinesJson.append("\"unit\": \"").append(escapeJson(rs5.getString("unit"))).append("\",");
                    topMedicinesJson.append("\"quantity\": ").append(rs5.getLong("total_quantity"));
                    topMedicinesJson.append("}");
                    firstMed = false;
                }
                topMedicinesJson.append("]");

            } finally {
                com.eclinic.database.ConnectionManager.closeConnection(conn);
            }

            StringBuilder monthlyRevJson = new StringBuilder("[");
            for (int i = 0; i < 12; i++) {
                if (i > 0) monthlyRevJson.append(",");
                monthlyRevJson.append(monthlyRevenue[i]);
            }
            monthlyRevJson.append("]");

            String json = "{" +
                "\"totalUsers\": " + totalUsers + ", " +
                "\"totalDoctors\": " + totalDoctors + ", " +
                "\"totalPatients\": " + totalPatients + ", " +
                "\"newPatients\": " + newPatients + ", " +
                "\"todayAppointments\": " + periodAppointments + ", " +
                "\"completedAppointments\": " + completedAppointments + ", " +
                "\"revenue\": " + totalRevenue + ", " +
                "\"todayRevenue\": " + periodRevenue + ", " +
                "\"monthlyRevenue\": " + monthlyRevJson.toString() + ", " +
                "\"unpaidInvoices\": " + unpaidInvoices + ", " +
                "\"topMedicines\": " + topMedicinesJson.toString() +
                "}";

            sendJson(exchange, json, 200);
        } catch (Exception e) {
            sendError(exchange, e.getMessage(), 500);
        }
    }
}
