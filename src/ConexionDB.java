import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL = System.getenv().getOrDefault(
            "JDBC_URL",
            "jdbc:mysql://localhost:3307/agencia_viajes?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8");
    private static final String USUARIO = System.getenv().getOrDefault("DB_USER", "root");
    private static final String CONTRASENA = System.getenv().getOrDefault("DB_PASSWORD", "root_password_clase");

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}
