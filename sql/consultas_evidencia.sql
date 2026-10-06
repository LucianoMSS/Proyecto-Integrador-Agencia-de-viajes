-- ============================================================
-- CONSULTAS DE EVIDENCIA - Entrega final
-- Base de datos: agencia_viajes
-- Ejecutar: docker exec -i mysql_agencia mysql -uroot -proot_password_clase agencia_viajes < sql/consultas_evidencia.sql
-- ============================================================

USE agencia_viajes;

-- Reporte principal (misma consulta que la opción 3 de la aplicación de consola).
SELECT o.folio,
       c.nombre AS cliente,
       CONCAT(a.nombre, ' ', a.apellido) AS asesor,
       o.destino,
       o.fecha_salida,
       o.estatus
FROM operaciones o
INNER JOIN clientes c ON c.id_cliente = o.id_cliente
INNER JOIN asesores a ON a.id_asesor = o.id_asesor
ORDER BY o.fecha_salida;

-- Servicios de la clienta Ana López García (álgebra: proyección de una restricción sobre la reunión).
SELECT
    c.nombre AS cliente,
    o.folio,
    s.tipo_servicio,
    s.descripcion,
    s.tarifa_publica,
    s.moneda
FROM clientes AS c
INNER JOIN operaciones AS o ON o.id_cliente = c.id_cliente
INNER JOIN servicios_contratados AS s ON s.id_operacion = o.id_operacion
WHERE c.correo = 'ana.lopez@correo.mx'
ORDER BY s.id_servicio;

-- Pagos del cliente a la agencia para el folio OP-2026-001.
SELECT
    o.folio,
    p.monto,
    p.moneda,
    p.tipo_cambio,
    p.fecha_pago,
    p.estatus
FROM operaciones AS o
INNER JOIN pagos AS p ON p.id_operacion = o.id_operacion
WHERE o.folio = 'OP-2026-001'
  AND p.tipo_flujo = 'CLIENTE_AGENCIA'
ORDER BY p.fecha_pago;

-- Reconstrucción del listado de pasajeros que antes iba en un solo campo.
SELECT
    o.folio,
    GROUP_CONCAT(
        CONCAT(pa.nombre, ' ', pa.apellido)
        ORDER BY pa.id_pasajero
        SEPARATOR ', '
    ) AS pasajeros
FROM operaciones AS o
INNER JOIN pasajeros AS pa ON pa.id_operacion = o.id_operacion
GROUP BY o.id_operacion, o.folio
ORDER BY o.folio;

-- Reunión sin pérdida: operaciones con clientes por id_cliente.
DROP TEMPORARY TABLE IF EXISTS r_operacion;
DROP TEMPORARY TABLE IF EXISTS r1;
DROP TEMPORARY TABLE IF EXISTS r2;

CREATE TEMPORARY TABLE r_operacion AS
SELECT
    o.id_operacion,
    o.folio,
    o.id_cliente,
    c.nombre AS nombre_cliente,
    c.correo AS correo_cliente,
    o.id_asesor,
    o.destino,
    o.fecha_salida,
    o.fecha_regreso,
    o.estatus
FROM operaciones AS o
INNER JOIN clientes AS c ON c.id_cliente = o.id_cliente;

CREATE TEMPORARY TABLE r1 AS
SELECT
    id_operacion, folio, id_cliente, id_asesor,
    destino, fecha_salida, fecha_regreso, estatus
FROM r_operacion;

CREATE TEMPORARY TABLE r2 AS
SELECT DISTINCT id_cliente, nombre_cliente, correo_cliente
FROM r_operacion;

SELECT
    'operaciones-clientes' AS descomposicion,
    (SELECT COUNT(*) FROM r_operacion) AS filas_original,
    (SELECT COUNT(*) FROM r1 INNER JOIN r2 ON r1.id_cliente = r2.id_cliente) AS filas_reunion;

-- Reunión sin pérdida al retirar nombre_proveedor de los servicios.
DROP TEMPORARY TABLE IF EXISTS r_servicio;
DROP TEMPORARY TABLE IF EXISTS s1;
DROP TEMPORARY TABLE IF EXISTS s2;

CREATE TEMPORARY TABLE r_servicio AS
SELECT
    s.id_servicio,
    s.id_proveedor,
    p.nombre_proveedor,
    s.tipo_servicio,
    s.tarifa_publica
FROM servicios_contratados AS s
INNER JOIN proveedores AS p ON p.id_proveedor = s.id_proveedor;

CREATE TEMPORARY TABLE s1 AS
SELECT id_servicio, id_proveedor, tipo_servicio, tarifa_publica
FROM r_servicio;

CREATE TEMPORARY TABLE s2 AS
SELECT DISTINCT id_proveedor, nombre_proveedor
FROM r_servicio;

SELECT
    'servicios-proveedores' AS descomposicion,
    (SELECT COUNT(*) FROM r_servicio) AS filas_original,
    (SELECT COUNT(*) FROM s1 INNER JOIN s2 ON s1.id_proveedor = s2.id_proveedor) AS filas_reunion;

-- Reunión sin pérdida al retirar destino y nombre del cliente de los pagos.
DROP TEMPORARY TABLE IF EXISTS r_pago;
DROP TEMPORARY TABLE IF EXISTS p1;
DROP TEMPORARY TABLE IF EXISTS p2;

CREATE TEMPORARY TABLE r_pago AS
SELECT
    pg.id_pago,
    pg.id_operacion,
    o.destino,
    c.nombre AS nombre_cliente,
    pg.monto
FROM pagos AS pg
INNER JOIN operaciones AS o ON o.id_operacion = pg.id_operacion
INNER JOIN clientes AS c ON c.id_cliente = o.id_cliente;

CREATE TEMPORARY TABLE p1 AS
SELECT id_pago, id_operacion, monto
FROM r_pago;

CREATE TEMPORARY TABLE p2 AS
SELECT DISTINCT id_operacion, destino, nombre_cliente
FROM r_pago;

SELECT
    'pagos-operaciones' AS descomposicion,
    (SELECT COUNT(*) FROM r_pago) AS filas_original,
    (SELECT COUNT(*) FROM p1 INNER JOIN p2 ON p1.id_operacion = p2.id_operacion) AS filas_reunion;
