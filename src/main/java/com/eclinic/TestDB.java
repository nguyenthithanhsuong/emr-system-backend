package com.eclinic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestDB {
    public static void main(String[] args) {
        try {
            String url = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require";
            String user = "postgres.jmzhakwlvnuqbqslgyzg";
            String pass = "mf1LCzRzqKqsWavn";
            Connection conn = DriverManager.getConnection(url, user, pass);
            String sql = "SELECT id, action, created_at FROM audit_logs WHERE DATE(created_at) >= CAST(? AS DATE) AND DATE(created_at) <= CAST(? AS DATE) LIMIT 5";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "2026-06-01");
            stmt.setString(2, "2026-06-15");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString("id") + " - " + rs.getString("action") + " - " + rs.getString("created_at"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
