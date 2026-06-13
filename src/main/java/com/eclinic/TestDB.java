package com.eclinic;
import com.eclinic.database.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
public class TestDB {
    public static void main(String[] args) throws Exception {
        ConnectionManager.init("jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require", "postgres.jmzhakwlvnuqbqslgyzg", "mf1LCzRzqKqsWavn");
        Connection conn = ConnectionManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users");
        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {
            System.out.println("USER: " + rs.getString("username"));
        }
    }
}
