import java.sql.*;
public class TestDb2 {
    public static void main(String[] args) throws Exception {
        String dbUrl = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require";
        String dbUser = "postgres.jmzhakwlvnuqbqslgyzg";
        String dbPassword = "mf1LCzRzqKqsWavn";
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        System.out.println("Connected.");
        try {
            String timeframe = "all";
            String aptDateCond = "1=1";
            String payDateCond = "1=1";
            String presDateCond = "1=1";
            String userDateCond = "1=1";

            conn.prepareStatement("SELECT COUNT(*), SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) FROM appointments WHERE " + aptDateCond).executeQuery();
            System.out.println("1 passed");

            conn.prepareStatement("SELECT SUM(amount), SUM(CASE WHEN " + payDateCond + " THEN amount ELSE 0 END) FROM payments WHERE payment_status = 'CONFIRMED'").executeQuery();
            System.out.println("2 passed");
            
            String topMedSql = "SELECT m.name as name, m.unit as unit, SUM(pd.quantity) as total_quantity " +
                               "FROM prescription_details pd " +
                               "JOIN prescriptions p ON pd.prescription_id = p.id " +
                               "JOIN payments pay ON pay.prescription_id = p.id " +
                               "JOIN medicines m ON pd.medicine_id = m.id " +
                               "WHERE pay.payment_status = 'CONFIRMED' AND " + presDateCond + " " +
                               "GROUP BY m.id, m.name, m.unit " +
                               "ORDER BY total_quantity DESC LIMIT 5";
            conn.prepareStatement(topMedSql).executeQuery();
            System.out.println("3 passed");
        } catch (Exception e) {
            e.printStackTrace();
        }
        conn.close();
    }
}
