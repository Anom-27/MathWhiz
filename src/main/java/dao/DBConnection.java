package dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Opens JDBC connections using db.properties (environment variables DB_URL / DB_USER / DB_PASSWORD win). */
public final class DBConnection {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // driver jar missing: getConnection() will report "No suitable driver"
        }
    }

    private DBConnection() { }

    private static String setting(String envKey, String propKey) {
        String v = System.getenv(envKey);
        return (v != null && !v.isEmpty()) ? v : PROPS.getProperty(propKey);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(setting("DB_URL", "db.url"),
                setting("DB_USER", "db.user"), setting("DB_PASSWORD", "db.password"));
    }
}
