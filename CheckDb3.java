import java.sql.*;
public class CheckDb3 {
    public static void main(String[] args) throws Exception {
        String dbUrl = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require";
        String dbUser = "postgres.jmzhakwlvnuqbqslgyzg";
        String dbPassword = "mf1LCzRzqKqsWavn";
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        
        ResultSet rs = conn.prepareStatement("SELECT table_name FROM information_schema.tables WHERE table_schema='public'").executeQuery();
        while (rs.next()) {
            System.out.println(rs.getString("table_name"));
        }
        conn.close();
    }
}
