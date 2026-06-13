import java.sql.*;
public class TestPaymentsJson {
    public static void main(String[] args) throws Exception {
        String dbUrl = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require";
        String dbUser = "postgres.jmzhakwlvnuqbqslgyzg";
        String dbPassword = "mf1LCzRzqKqsWavn";
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        try {
            String sql = "SELECT pay.id AS payment_id, COALESCE(pay.amount, pres.total_price) AS amount, " +
            "COALESCE(pay.payment_status, 'PENDING') AS payment_status, COALESCE(pay.payment_method, 'CASH') AS payment_method, " +
            "pay.paid_at, COALESCE(pay.created_at, pres.created_at) AS payment_created_at, " +
            "pres.id as prescription_id, pres.medical_record_id, pres.total_price, " +
            "pat.full_name as patient_name, d.full_name as doctor_name " +
            "FROM prescriptions pres " +
            "LEFT JOIN payments pay ON pay.prescription_id = pres.id " +
            "LEFT JOIN medical_records mr ON pres.medical_record_id = mr.id " +
            "LEFT JOIN appointments a ON mr.appointment_id = a.id " +
            "LEFT JOIN patients pat ON a.patient_id = pat.id " +
            "LEFT JOIN doctors d ON a.doctor_id = d.id " +
            "ORDER BY COALESCE(pay.created_at, pres.created_at) DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                System.out.println(rs.getLong("prescription_id"));
                System.out.println(rs.getString("patient_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        conn.close();
    }
}
