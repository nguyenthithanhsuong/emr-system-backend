import java.sql.*;
public class TestDb {
    public static void main(String[] args) throws Exception {
        String dbUrl = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require";
        String dbUser = "postgres.jmzhakwlvnuqbqslgyzg";
        String dbPassword = "mf1LCzRzqKqsWavn";
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        System.out.println("Connected.");
        try {
            String timeframe = "today";
            String aptDateCond = "DATE(appointment_start_date) = CURRENT_DATE";
            String payDateCond = "DATE(created_at) = CURRENT_DATE";
            String presDateCond = "DATE(p.created_at) = CURRENT_DATE";
            String userDateCond = "DATE(created_at) = CURRENT_DATE";

            conn.prepareStatement("SELECT COUNT(*), SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) FROM appointments WHERE " + aptDateCond).executeQuery();
            System.out.println("1 passed");

            conn.prepareStatement("SELECT SUM(amount), SUM(CASE WHEN " + payDateCond + " THEN amount ELSE 0 END) FROM payments WHERE payment_status = 'CONFIRMED'").executeQuery();
            System.out.println("2 passed");

            conn.prepareStatement("SELECT EXTRACT(MONTH FROM created_at) AS month, SUM(amount) AS total FROM payments WHERE payment_status = 'CONFIRMED' AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE) GROUP BY EXTRACT(MONTH FROM created_at)").executeQuery();
            System.out.println("3 passed");

            conn.prepareStatement("SELECT COUNT(*) FROM prescriptions pres LEFT JOIN payments pay ON pay.prescription_id = pres.id WHERE pay.id IS NULL OR pay.payment_status != 'CONFIRMED'").executeQuery();
            System.out.println("4 passed");

            String topMedSql = "SELECT m.name as name, m.unit as unit, SUM(pd.quantity) as total_quantity " +
                               "FROM prescription_details pd " +
                               "JOIN prescriptions p ON pd.prescription_id = p.id " +
                               "JOIN payments pay ON pay.prescription_id = p.id " +
                               "JOIN medicines m ON pd.medicine_id = m.id " +
                               "WHERE pay.payment_status = 'CONFIRMED' AND " + presDateCond + " " +
                               "GROUP BY m.id, m.name, m.unit " +
                               "ORDER BY total_quantity DESC LIMIT 5";
            conn.prepareStatement(topMedSql).executeQuery();
            System.out.println("5 passed");
            
            conn.prepareStatement("SELECT COUNT(*) FROM patients WHERE " + userDateCond).executeQuery();
            System.out.println("6 passed");
            
            conn.prepareStatement("SELECT COUNT(*) FROM users WHERE role = 'PATIENT' AND " + userDateCond).executeQuery();
            System.out.println("7 passed");

        } catch (Exception e) {
            e.printStackTrace();
        }
        conn.close();
    }
}
