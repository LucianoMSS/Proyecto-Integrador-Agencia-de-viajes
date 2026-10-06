import java.sql.SQLException;

public final class SqlUtil {
    private SqlUtil() {
    }

    public static String mensajeAmigable(SQLException ex) {
        int code = ex.getErrorCode();
        if (code == 1062) {
            return "Ya existe un registro con ese RFC, correo o folio (dato duplicado).";
        }
        if (code == 1451) {
            return "No se puede eliminar: el cliente tiene operaciones asociadas; elimínalas primero.";
        }
        if (code == 1452) {
            return "No existe el cliente o el asesor indicado. Verifique los identificadores.";
        }
        if (code == 3819 || code == 4025) {
            return "Las fechas no son válidas: la fecha de regreso debe ser igual o posterior a la de salida.";
        }
        String msg = ex.getMessage();
        if (msg != null && msg.contains("chk_operaciones_fechas")) {
            return "Las fechas no son válidas: la fecha de regreso debe ser igual o posterior a la de salida.";
        }
        if (msg != null && msg.contains("Data truncated") && msg.contains("estatus")) {
            return "Estatus no válido. Use: COTIZADA, CONFIRMADA, EN_CURSO, CONCLUIDA o CANCELADA.";
        }
        return "Error de base de datos: " + (msg != null ? msg.split("\n")[0] : "operación rechazada");
    }
}
