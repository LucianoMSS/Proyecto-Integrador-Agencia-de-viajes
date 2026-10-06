import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final String[] ESTATUS_VALIDOS = {
            "COTIZADA", "CONFIRMADA", "EN_CURSO", "CONCLUIDA", "CANCELADA"
    };

    private final Scanner scanner = new Scanner(System.in);
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final OperacionDAO operacionDAO = new OperacionDAO();

    public static void main(String[] args) {
        new Main().ejecutar();
    }

    private void ejecutar() {
        boolean salir = false;
        while (!salir) {
            System.out.println();
            System.out.println("=== AGENCIA DE VIAJES ===");
            System.out.println("1. Clientes");
            System.out.println("2. Operaciones");
            System.out.println("3. Reporte: operaciones con cliente y asesor");
            System.out.println("0. Salir");
            int opcion = leerEnteroEnRango("Opción: ", 0, 3);
            switch (opcion) {
                case 1 -> menuClientes();
                case 2 -> menuOperaciones();
                case 3 -> ejecutarReporteJoin();
                case 0 -> salir = true;
                default -> System.out.println("Opción no válida.");
            }
        }
        System.out.println("Hasta luego.");
    }

    private void menuClientes() {
        boolean volver = false;
        while (!volver) {
            System.out.println();
            System.out.println("--- Clientes ---");
            System.out.println("1. Crear");
            System.out.println("2. Listar");
            System.out.println("3. Buscar por id");
            System.out.println("4. Actualizar");
            System.out.println("5. Eliminar");
            System.out.println("0. Volver");
            int opcion = leerEnteroEnRango("Opción: ", 0, 5);
            switch (opcion) {
                case 1 -> crearCliente();
                case 2 -> listarClientes();
                case 3 -> buscarCliente();
                case 4 -> actualizarCliente();
                case 5 -> eliminarCliente();
                case 0 -> volver = true;
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void menuOperaciones() {
        boolean volver = false;
        while (!volver) {
            System.out.println();
            System.out.println("--- Operaciones ---");
            System.out.println("1. Crear");
            System.out.println("2. Listar");
            System.out.println("3. Buscar por id");
            System.out.println("4. Buscar por folio");
            System.out.println("5. Actualizar");
            System.out.println("6. Eliminar");
            System.out.println("0. Volver");
            int opcion = leerEnteroEnRango("Opción: ", 0, 6);
            switch (opcion) {
                case 1 -> crearOperacion();
                case 2 -> listarOperaciones();
                case 3 -> buscarOperacionPorId();
                case 4 -> buscarOperacionPorFolio();
                case 5 -> actualizarOperacion();
                case 6 -> eliminarOperacion();
                case 0 -> volver = true;
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void crearCliente() {
        System.out.println("--- Crear cliente ---");
        String rfc = leerTextoNoVacio("RFC: ");
        String nombre = leerTextoNoVacio("Nombre: ");
        String correo = leerTextoNoVacio("Correo: ");
        String telefono = leerTextoOpcional("Teléfono (opcional): ");
        Cliente c = new Cliente();
        c.setRfc(rfc);
        c.setNombre(nombre);
        c.setCorreo(correo);
        c.setTelefono(telefono.isEmpty() ? null : telefono);
        try {
            int id = clienteDAO.crear(c);
            System.out.println("Cliente creado correctamente. id_cliente=" + id);
        } catch (SQLException ex) {
            System.out.println(SqlUtil.mensajeAmigable(ex));
        }
    }

    private void listarClientes() {
        try {
            List<Cliente> lista = clienteDAO.listar();
            imprimirClientes(lista);
        } catch (SQLException ex) {
            System.out.println(SqlUtil.mensajeAmigable(ex));
        }
    }

    private void buscarCliente() {
        int id = leerEnteroPositivo("id_cliente: ");
        try {
            Optional<Cliente> opt = clienteDAO.buscarPorId(id);
            if (opt.isPresent()) {
                imprimirClientes(List.of(opt.get()));
            } else {
                System.out.println("No se encontró cliente con id " + id + ".");
            }
        } catch (SQLException ex) {
            System.out.println(SqlUtil.mensajeAmigable(ex));
        }
    }

    private void actualizarCliente() {
        int id = leerEnteroPositivo("id_cliente a actualizar: ");
        try {
            Optional<Cliente> opt = clienteDAO.buscarPorId(id);
            if (opt.isEmpty()) {
                System.out.println("No se encontró cliente con id " + id + ".");
                return;
            }
            System.out.println("Deje vacío para conservar el valor actual.");
            Cliente actual = opt.get();
            String rfc = leerTextoOpcional("RFC [" + actual.getRfc() + "]: ");
            String nombre = leerTextoOpcional("Nombre [" + actual.getNombre() + "]: ");
            String correo = leerTextoOpcional("Correo [" + actual.getCorreo() + "]: ");
            String telefono = leerTextoOpcional("Teléfono [" + nuloAVacio(actual.getTelefono()) + "]: ");
            if (!rfc.isEmpty()) {
                actual.setRfc(rfc);
            }
            if (!nombre.isEmpty()) {
                actual.setNombre(nombre);
            }
            if (!correo.isEmpty()) {
                actual.setCorreo(correo);
            }
            if (!telefono.isEmpty()) {
                actual.setTelefono(telefono);
            }
            if (clienteDAO.actualizar(actual)) {
                System.out.println("Cliente actualizado correctamente.");
                listarClientes();
            } else {
                System.out.println("No se pudo actualizar el cliente.");
            }
        } catch (SQLException ex) {
            System.out.println(SqlUtil.mensajeAmigable(ex));
        }
    }

    private void eliminarCliente() {
        int id = leerEnteroPositivo("id_cliente a eliminar: ");
        if (!confirmar("¿Confirma eliminar el cliente " + id + "? (s/n): ")) {
            System.out.println("Operación cancelada.");
            return;
        }
        try {
            if (clienteDAO.eliminar(id)) {
                System.out.println("Cliente eliminado correctamente.");
            } else {
                System.out.println("No se encontró cliente con id " + id + ".");
            }
        } catch (SQLException ex) {
            System.out.println(SqlUtil.mensajeAmigable(ex));
        }
    }

    private void crearOperacion() {
        System.out.println("--- Crear operación ---");
        String folio = leerTextoNoVacio("Folio: ");
        int idCliente = leerEnteroPositivo("id_cliente: ");
        int idAsesor = leerEnteroPositivo("id_asesor (1-3 en datos de prueba): ");
        String destino = leerTextoNoVacio("Destino: ");
        String fechaSalida = leerFecha("Fecha salida (yyyy-MM-dd): ");
        String fechaRegreso = leerFecha("Fecha regreso (yyyy-MM-dd): ");
        if (LocalDate.parse(fechaRegreso).isBefore(LocalDate.parse(fechaSalida))) {
            System.out.println("La fecha de regreso debe ser igual o posterior a la de salida.");
            return;
        }
        String estatus = leerEstatus();
        Operacion op = new Operacion();
        op.setFolio(folio);
        op.setIdCliente(idCliente);
        op.setIdAsesor(idAsesor);
        op.setDestino(destino);
        op.setFechaSalida(fechaSalida);
        op.setFechaRegreso(fechaRegreso);
        op.setEstatus(estatus);
        try {
            int id = operacionDAO.crear(op);
            System.out.println("Operación creada correctamente. id_operacion=" + id);
        } catch (SQLException ex) {
            if ("CLIENTE_INEXISTENTE".equals(ex.getMessage())) {
                System.out.println("No existe el cliente indicado. Verifique id_cliente.");
            } else if ("ASESOR_INEXISTENTE".equals(ex.getMessage())) {
                System.out.println("No existe el asesor indicado. Verifique id_asesor.");
            } else {
                System.out.println(SqlUtil.mensajeAmigable(ex));
            }
        }
    }

    private void listarOperaciones() {
        try {
            List<Operacion> lista = operacionDAO.listar();
            imprimirOperaciones(lista);
        } catch (SQLException ex) {
            System.out.println(SqlUtil.mensajeAmigable(ex));
        }
    }

    private void buscarOperacionPorId() {
        int id = leerEnteroPositivo("id_operacion: ");
        try {
            Optional<Operacion> opt = operacionDAO.buscarPorId(id);
            if (opt.isPresent()) {
                imprimirOperaciones(List.of(opt.get()));
            } else {
                System.out.println("No se encontró operación con id " + id + ".");
            }
        } catch (SQLException ex) {
            System.out.println(SqlUtil.mensajeAmigable(ex));
        }
    }

    private void buscarOperacionPorFolio() {
        String folio = leerTextoNoVacio("Folio: ");
        try {
            Optional<Operacion> opt = operacionDAO.buscarPorFolio(folio);
            if (opt.isPresent()) {
                imprimirOperaciones(List.of(opt.get()));
            } else {
                System.out.println("No se encontró operación con folio " + folio + ".");
            }
        } catch (SQLException ex) {
            System.out.println(SqlUtil.mensajeAmigable(ex));
        }
    }

    private void actualizarOperacion() {
        int id = leerEnteroPositivo("id_operacion a actualizar: ");
        try {
            Optional<Operacion> opt = operacionDAO.buscarPorId(id);
            if (opt.isEmpty()) {
                System.out.println("No se encontró operación con id " + id + ".");
                return;
            }
            Operacion actual = opt.get();
            System.out.println("Deje vacío para conservar el valor actual.");
            String destino = leerTextoOpcional("Destino [" + actual.getDestino() + "]: ");
            String fechaSalida = leerTextoOpcional("Fecha salida [" + actual.getFechaSalida() + "]: ");
            String fechaRegreso = leerTextoOpcional("Fecha regreso [" + actual.getFechaRegreso() + "]: ");
            String estatusStr = leerTextoOpcional("Estatus [" + actual.getEstatus() + "]: ");
            String idAsesorStr = leerTextoOpcional("id_asesor [" + actual.getIdAsesor() + "]: ");
            if (!destino.isEmpty()) {
                actual.setDestino(destino);
            }
            if (!fechaSalida.isEmpty()) {
                if (!esFechaValida(fechaSalida)) {
                    System.out.println("Fecha de salida no válida.");
                    return;
                }
                actual.setFechaSalida(fechaSalida);
            }
            if (!fechaRegreso.isEmpty()) {
                if (!esFechaValida(fechaRegreso)) {
                    System.out.println("Fecha de regreso no válida.");
                    return;
                }
                actual.setFechaRegreso(fechaRegreso);
            }
            if (LocalDate.parse(actual.getFechaRegreso()).isBefore(LocalDate.parse(actual.getFechaSalida()))) {
                System.out.println("La fecha de regreso debe ser igual o posterior a la de salida.");
                return;
            }
            if (!estatusStr.isEmpty()) {
                if (!esEstatusValido(estatusStr)) {
                    System.out.println("Estatus no válido.");
                    return;
                }
                actual.setEstatus(estatusStr.toUpperCase());
            }
            if (!idAsesorStr.isEmpty()) {
                try {
                    actual.setIdAsesor(Integer.parseInt(idAsesorStr));
                } catch (NumberFormatException e) {
                    System.out.println("id_asesor debe ser un número entero.");
                    return;
                }
            }
            if (operacionDAO.actualizar(actual)) {
                System.out.println("Operación actualizada correctamente.");
                listarOperaciones();
            } else {
                System.out.println("No se pudo actualizar la operación.");
            }
        } catch (SQLException ex) {
            if ("ASESOR_INEXISTENTE".equals(ex.getMessage())) {
                System.out.println("No existe el asesor indicado. Verifique id_asesor.");
            } else {
                System.out.println(SqlUtil.mensajeAmigable(ex));
            }
        }
    }

    private void eliminarOperacion() {
        int id = leerEnteroPositivo("id_operacion a eliminar: ");
        System.out.println("Al eliminar una operación también se eliminan en cascada sus pasajeros, "
                + "servicios y pagos asociados (según el DDL).");
        if (!confirmar("¿Confirma eliminar la operación " + id + "? (s/n): ")) {
            System.out.println("Operación cancelada.");
            return;
        }
        try {
            if (operacionDAO.eliminar(id)) {
                System.out.println("Operación eliminada correctamente.");
            } else {
                System.out.println("No se encontró operación con id " + id + ".");
            }
        } catch (SQLException ex) {
            System.out.println(SqlUtil.mensajeAmigable(ex));
        }
    }

    private void ejecutarReporteJoin() {
        System.out.println("--- Reporte: operaciones con cliente y asesor ---");
        try {
            operacionDAO.reporteOperacionesClienteAsesor();
        } catch (SQLException ex) {
            System.out.println(SqlUtil.mensajeAmigable(ex));
        }
    }

    private void imprimirClientes(List<Cliente> lista) {
        if (lista.isEmpty()) {
            System.out.println("(sin registros)");
            return;
        }
        System.out.printf("%-6s %-14s %-24s %-28s %-12s%n",
                "id", "rfc", "nombre", "correo", "telefono");
        System.out.println("-".repeat(90));
        for (Cliente c : lista) {
            System.out.printf("%-6d %-14s %-24s %-28s %-12s%n",
                    c.getIdCliente(),
                    c.getRfc(),
                    truncar(c.getNombre(), 24),
                    truncar(c.getCorreo(), 28),
                    nuloAVacio(c.getTelefono()));
        }
        System.out.println("Total: " + lista.size());
    }

    private void imprimirOperaciones(List<Operacion> lista) {
        if (lista.isEmpty()) {
            System.out.println("(sin registros)");
            return;
        }
        System.out.printf("%-6s %-14s %-22s %-20s %-16s %-12s %-12s %-12s%n",
                "id", "folio", "cliente", "asesor", "destino", "salida", "regreso", "estatus");
        System.out.println("-".repeat(120));
        for (Operacion o : lista) {
            System.out.printf("%-6d %-14s %-22s %-20s %-16s %-12s %-12s %-12s%n",
                    o.getIdOperacion(),
                    o.getFolio(),
                    truncar(o.getNombreCliente(), 22),
                    truncar(o.getNombreAsesor(), 20),
                    truncar(o.getDestino(), 16),
                    o.getFechaSalida(),
                    o.getFechaRegreso(),
                    o.getEstatus());
        }
        System.out.println("Total: " + lista.size());
    }

    private int leerEnteroEnRango(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String linea = scanner.nextLine().trim();
            try {
                int valor = Integer.parseInt(linea);
                if (valor >= min && valor <= max) {
                    return valor;
                }
                System.out.println("Ingrese un número entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Entrada no válida. Ingrese un número entero.");
            }
        }
    }

    private int leerEnteroPositivo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linea = scanner.nextLine().trim();
            try {
                int valor = Integer.parseInt(linea);
                if (valor > 0) {
                    return valor;
                }
                System.out.println("Ingrese un entero mayor que cero.");
            } catch (NumberFormatException e) {
                System.out.println("Entrada no válida. Ingrese un número entero.");
            }
        }
    }

    private String leerTextoNoVacio(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linea = scanner.nextLine().trim();
            if (!linea.isEmpty()) {
                return linea;
            }
            System.out.println("El campo no puede estar vacío.");
        }
    }

    private String leerTextoOpcional(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String leerFecha(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linea = scanner.nextLine().trim();
            if (esFechaValida(linea)) {
                return linea;
            }
            System.out.println("Formato inválido. Use yyyy-MM-dd.");
        }
    }

    private boolean esFechaValida(String texto) {
        try {
            LocalDate.parse(texto);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private String leerEstatus() {
        System.out.println("Estatus válidos: COTIZADA, CONFIRMADA, EN_CURSO, CONCLUIDA, CANCELADA");
        while (true) {
            String valor = leerTextoNoVacio("Estatus: ").toUpperCase();
            if (esEstatusValido(valor)) {
                return valor;
            }
            System.out.println("Estatus no válido. Intente de nuevo.");
        }
    }

    private boolean esEstatusValido(String valor) {
        for (String e : ESTATUS_VALIDOS) {
            if (e.equalsIgnoreCase(valor)) {
                return true;
            }
        }
        return false;
    }

    private boolean confirmar(String prompt) {
        while (true) {
            String resp = leerTextoOpcional(prompt).toLowerCase();
            if (resp.equals("s") || resp.equals("si") || resp.equals("sí")) {
                return true;
            }
            if (resp.equals("n") || resp.equals("no")) {
                return false;
            }
            System.out.println("Responda s o n.");
        }
    }

    private static String nuloAVacio(String s) {
        return s == null ? "" : s;
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
