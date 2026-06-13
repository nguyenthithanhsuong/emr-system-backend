import java.sql.*;
public class CheckDb2 {
    public static void main(String[] args) throws Exception {
        String dbUrl = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require";
        String dbUser = "postgres.jmzhakwlvnuqbqslgyzg";
        String dbPassword = "mf1LCzRzqKqsWavn";
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        
        ResultSet rs = conn.prepareStatement("SELECT id, prescription_id, payment_status FROM payments").executeQuery();
        while (rs.next()) {
            System.out.println("Payment ID: " + rs.getLong("id") + 
                               ", Pres ID: " + rs.getLong("prescription_id") + 
                               ", Status: " + rs.getString("payment_status"));
        }
        conn.close();
    }
}
