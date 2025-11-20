package businessmanager.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionDB {

    private static final Properties props = new Properties();
    private static final String JDBC_URL;
    private static final String USER;
    private static final String PASSWORD;

    
    static {
        try (InputStream input = ConexionDB.class.getClassLoader().getResourceAsStream("database.properties")) {

            if (input == null) {
                System.err.println("¡Error Fatal! No se encuentra el archivo config.properties");
                throw new RuntimeException("No se pudo encontrar config.properties");
            }

            props.load(input);

            JDBC_URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error al leer config.properties", ex);
        }
    }


    public static Connection getConnection() throws SQLException {
        
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }
}