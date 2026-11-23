package hydroflow.dao;

import hydroflow.db.DBConnection;
import hydroflow.db.DatabaseException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// TABLE: users (login screen)
public class UserDAO {

    // if the username exists and the password matches the stored hash
	
    public boolean authenticate(String username, String password) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND password_hash = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check the login.", e);
        }
    }

    // turns the password into a SHA-256 text, the same way it is stored in the users table
    
    public static String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
