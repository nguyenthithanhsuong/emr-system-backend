import java.sql.*;
public class TestDb3 {
    public static void main(String[] args) throws Exception {
        String dbUrl = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require";
        String dbUser = "postgres.jmzhakwlvnuqbqslgyzg";
        String dbPassword = "mf1LCzRzqKqsWavn";
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        try {
            conn.prepareStatement("SELECT COUNT(*) FROM users WHERE role='PATIENT' AND 1=1").executeQuery();
            System.out.println("1 passed");
        } catch (Exception e) {
            e.printStackTrace();
        }
        conn.close();
    }
}
