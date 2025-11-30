package gym_app;

import java.sql.*;

public class DatabaseService {
    private Connection conn = null;

    public DatabaseService() {
        connectWithAutoCreate();
    }

    private void connectWithAutoCreate() {
        try {
            // Kết nối tới MySQL (không chỉ định DB trước)
            String rootUrl = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";
            Connection tempConn = DriverManager.getConnection(rootUrl, "root", ""); // đổi pass nếu có

            Statement stmt = tempConn.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS gym_smartcard");
            stmt.close();
            tempConn.close();

            // Kết nối lại vào gym_db
            String dbUrl = "jdbc:mysql://localhost:3306/gym_smartcard?useSSL=false&serverTimezone=UTC";
            conn = DriverManager.getConnection(dbUrl, "root", "");
            createTablesIfNotExist();
            System.out.println("Kết nối CSDL gym_db thành công!");
        } catch (Exception e) {
            System.out.println("Không kết nối được MySQL! Chạy ở chế độ offline.");
            e.printStackTrace();
        }
    }

    private void createTablesIfNotExist() throws SQLException {
        if (conn == null) return;

        String members = """
            CREATE TABLE IF NOT EXISTS members (
                id INT AUTO_INCREMENT PRIMARY KEY,
                card_id VARCHAR(20) UNIQUE NOT NULL,
                name_enc TEXT NOT NULL,
                phone_hash VARCHAR(64) NOT NULL,
                phone_enc TEXT NOT NULL,
                balance BIGINT DEFAULT 0,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                status VARCHAR(10) DEFAULT 'active'
            )""";

        String transactions = """
            CREATE TABLE IF NOT EXISTS transactions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                card_id VARCHAR(20),
                type VARCHAR(20),
                amount BIGINT,
                description TEXT,
                signature TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )""";

        Statement stmt = conn.createStatement();
        stmt.execute(members);
        stmt.execute(transactions);
        stmt.close();
    }

    public boolean registerMember(String name, String phone, String cardId) {
        if (conn == null) return false;
        String sql = "INSERT INTO members (card_id, name_enc, phone_hash, phone_enc) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cardId);
            ps.setString(2, SecurityUtils.encrypt(name));
            ps.setString(3, SecurityUtils.hashPhone(phone));
            ps.setString(4, SecurityUtils.encrypt(phone));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateBalance(String cardId, long balance) {
        if (conn == null) return;
        String sql = "UPDATE members SET balance = ? WHERE card_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, balance);
            ps.setString(2, cardId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logTransaction(String cardId, String type, long amount, String desc, String signature) {
        if (conn == null) return;
        String sql = "INSERT INTO transactions (card_id, type, amount, description, signature) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cardId);
            ps.setString(2, type);
            ps.setLong(3, amount);
            ps.setString(4, desc);
            ps.setString(5, signature);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return conn;
    }
}