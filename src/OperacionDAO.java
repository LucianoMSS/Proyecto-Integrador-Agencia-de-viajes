import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OperacionDAO {
    private static final String SQL_LISTAR_JOIN =
            "SELECT o.id_operacion, o.folio, o.id_cliente, o.id_asesor, o.destino, "
                    + "o.fecha_salida, o.fecha_regreso, o.estatus, "
                    + "c.nombre AS nombre_cliente, CONCAT(a.nombre, ' ', a.apellido) AS nombre_asesor "
                    + "FROM operaciones o "
                    + "INNER JOIN clientes c ON c.id_cliente = o.id_cliente "
                    + "INNER JOIN asesores a ON a.id_asesor = o.id_asesor "
                    + "ORDER BY o.id_operacion";

    public int crear(Operacion op) throws SQLException {
        if (!existeCliente(op.getIdCliente())) {
            throw new SQLException("CLIENTE_INEXISTENTE");
        }
        if (!existeAsesor(op.getIdAsesor())) {
            throw new SQLException("ASESOR_INEXISTENTE");
        }
        String sql = "INSERT INTO operaciones (folio, id_cliente, id_asesor, destino, fecha_salida, fecha_regreso, estatus) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, op.getFolio());
            ps.setInt(2, op.getIdCliente());
            ps.setInt(3, op.getIdAsesor());
            ps.setString(4, op.getDestino());
            ps.setString(5, op.getFechaSalida());
            ps.setString(6, op.getFechaRegreso());
            ps.setString(7, op.getEstatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No se obtuvo id_operacion al insertar.");
        }
    }

    public List<Operacion> listar() throws SQLException {
        List<Operacion> lista = new ArrayList<>();
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_JOIN);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearJoin(rs));
            }
        }
        return lista;
    }

    public Optional<Operacion> buscarPorId(int id) throws SQLException {
        String sql = SQL_LISTAR_JOIN.replace("ORDER BY o.id_operacion", "WHERE o.id_operacion = ?");
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearJoin(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Operacion> buscarPorFolio(String folio) throws SQLException {
        String sql = SQL_LISTAR_JOIN.replace("ORDER BY o.id_operacion", "WHERE o.folio = ?");
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, folio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearJoin(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean actualizar(Operacion op) throws SQLException {
        if (!existeAsesor(op.getIdAsesor())) {
            throw new SQLException("ASESOR_INEXISTENTE");
        }
        String sql = "UPDATE operaciones SET id_asesor = ?, destino = ?, fecha_salida = ?, fecha_regreso = ?, estatus = ? "
                + "WHERE id_operacion = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, op.getIdAsesor());
            ps.setString(2, op.getDestino());
            ps.setString(3, op.getFechaSalida());
            ps.setString(4, op.getFechaRegreso());
            ps.setString(5, op.getEstatus());
            ps.setInt(6, op.getIdOperacion());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int idOperacion) throws SQLException {
        String sql = "DELETE FROM operaciones WHERE id_operacion = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOperacion);
            return ps.executeUpdate() > 0;
        }
    }

    public void reporteOperacionesClienteAsesor() throws SQLException {
        String sql =
                "SELECT o.folio, c.nombre AS cliente, CONCAT(a.nombre, ' ', a.apellido) AS asesor, "
                        + "o.destino, o.fecha_salida, o.estatus "
                        + "FROM operaciones o "
                        + "INNER JOIN clientes c ON c.id_cliente = o.id_cliente "
                        + "INNER JOIN asesores a ON a.id_asesor = o.id_asesor "
                        + "ORDER BY o.fecha_salida";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.printf("%-14s %-22s %-28s %-18s %-12s %-12s%n",
                    "folio", "cliente", "asesor", "destino", "fecha_salida", "estatus");
            System.out.println("-".repeat(110));
            int filas = 0;
            while (rs.next()) {
                filas++;
                System.out.printf("%-14s %-22s %-28s %-18s %-12s %-12s%n",
                        rs.getString("folio"),
                        truncar(rs.getString("cliente"), 22),
                        truncar(rs.getString("asesor"), 28),
                        truncar(rs.getString("destino"), 18),
                        rs.getString("fecha_salida"),
                        rs.getString("estatus"));
            }
            System.out.println("Filas: " + filas);
        }
    }

    private boolean existeCliente(int idCliente) throws SQLException {
        String sql = "SELECT 1 FROM clientes WHERE id_cliente = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean existeAsesor(int idAsesor) throws SQLException {
        String sql = "SELECT 1 FROM asesores WHERE id_asesor = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAsesor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Operacion mapearJoin(ResultSet rs) throws SQLException {
        Operacion op = new Operacion();
        op.setIdOperacion(rs.getInt("id_operacion"));
        op.setFolio(rs.getString("folio"));
        op.setIdCliente(rs.getInt("id_cliente"));
        op.setIdAsesor(rs.getInt("id_asesor"));
        op.setDestino(rs.getString("destino"));
        op.setFechaSalida(rs.getString("fecha_salida"));
        op.setFechaRegreso(rs.getString("fecha_regreso"));
        op.setEstatus(rs.getString("estatus"));
        op.setNombreCliente(rs.getString("nombre_cliente"));
        op.setNombreAsesor(rs.getString("nombre_asesor"));
        return op;
    }

    private static String truncar(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max - 1) + "…";
    }
}
