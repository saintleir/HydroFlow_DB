package hydroflow.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// change URL / USER / PASSWORD below to match your own MySQL

public class DBConnection {

    private static final String URL = read("HYDROFLOW_DB_URL",
            "jdbc:mysql://localhost:3306/hydroflow_db?useSSL=false&allowPublicKeyRetrieval=true");
    private static final String USER = read("HYDROFLOW_DB_USER", "root");
    private static final String PASSWORD = read("HYDROFLOW_DB_PASS", "cj122006");

    private DBConnection() { }

    // opens a new connection. always use it inside try-with-resources so it gets closed
    
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new DatabaseException("Cannot connect to MySQL.\n"
                    + "1) Is MySQL running?  2) Did you run sql/schema.sql?\n"
                    + "3) Is the MySQL Connector/J jar added to the build path (see lib/README.txt)?\n"
                    + "4) Are the URL / user / password in DBConnection.java correct?", e);
        }
    }

    private static String read(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }
}
