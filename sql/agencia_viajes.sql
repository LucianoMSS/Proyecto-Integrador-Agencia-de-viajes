-- ============================================================
-- PROYECTO INTEGRADOR - ENTREGA FINAL
-- Materia: Fundamentos / Diseño de Bases de Datos
-- Equipo: Luis Luciano Matos Santos, Felipe Balan Xaman,
--         Pedro Roman Dominguez Rodriguez
-- Dominio: Agencia de viajes
-- Motor: MySQL 8.0 / Docker
-- Fecha de entrega: 06/10/2026
-- Versión: Entrega final
--
-- Esquema normalizado hasta la Tercera Forma Normal (3FN).
-- Base de datos: agencia_viajes
--
-- ÍNDICE DE TABLAS Y RELACIONES
--   clientes (PK id_cliente)
--   proveedores (PK id_proveedor)
--   asesores (PK id_asesor)
--   operaciones (PK id_operacion) -> FK id_cliente -> clientes
--                              -> FK id_asesor -> asesores
--   pasajeros (PK id_pasajero) -> FK id_operacion -> operaciones (CASCADE)
--   servicios_contratados (PK id_servicio) -> FK id_operacion, id_proveedor
--   pagos (PK id_pago) -> FK id_operacion (CASCADE), id_servicio
--
-- Consultas de evidencia (JOIN y demostraciones): sql/consultas_evidencia.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS agencia_viajes
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE agencia_viajes;

SET NAMES utf8mb4;

-- ============================================================
-- SECCIÓN: ELIMINACIÓN DE TABLAS (orden inverso a dependencias)
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS pagos;
DROP TABLE IF EXISTS servicios_contratados;
DROP TABLE IF EXISTS pasajeros;
DROP TABLE IF EXISTS operaciones;
DROP TABLE IF EXISTS asesores;
DROP TABLE IF EXISTS proveedores;
DROP TABLE IF EXISTS clientes;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- SECCIÓN: DEFINICIÓN DE TABLAS (CREATE TABLE)
-- ============================================================

-- ------------------------------------------------------------
-- clientes | Grado 6 | PK id_cliente
-- Propósito: datos de contacto de quien contrata viajes.
-- ------------------------------------------------------------
CREATE TABLE clientes (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    rfc VARCHAR(13) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_clientes_rfc UNIQUE (rfc),
    CONSTRAINT uk_clientes_correo UNIQUE (correo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- proveedores | Grado 6 | PK id_proveedor
-- Propósito: aerolíneas, hoteles y demás proveedores de servicio.
-- ------------------------------------------------------------
CREATE TABLE proveedores (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    rfc VARCHAR(13) NOT NULL,
    nombre_proveedor VARCHAR(150) NOT NULL,
    tipo_proveedor ENUM('AEROLINEA', 'HOTEL', 'NAVIERA', 'ASEGURADORA') NOT NULL,
    correo VARCHAR(100) NULL,
    telefono VARCHAR(20) NULL,
    CONSTRAINT uk_proveedores_rfc UNIQUE (rfc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- asesores | Grado 5 | PK id_asesor
-- Propósito: empleados que atienden operaciones de viaje.
-- ------------------------------------------------------------
CREATE TABLE asesores (
    id_asesor INT AUTO_INCREMENT PRIMARY KEY,
    num_empleado VARCHAR(20) NOT NULL,
    nombre VARCHAR(60) NOT NULL,
    apellido VARCHAR(80) NOT NULL,
    correo VARCHAR(100) NOT NULL,
    CONSTRAINT uk_asesores_num_empleado UNIQUE (num_empleado),
    CONSTRAINT uk_asesores_correo UNIQUE (correo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- operaciones | Grado 9 | PK id_operacion
-- FK: id_cliente -> clientes (RESTRICT delete), id_asesor -> asesores (RESTRICT delete)
-- chk_operaciones_fechas: la fecha de regreso no puede ser anterior a la de salida.
-- Se retiraron nombre_cliente, correo_cliente y pasajeros (normalización 3FN).
-- ------------------------------------------------------------
CREATE TABLE operaciones (
    id_operacion INT AUTO_INCREMENT PRIMARY KEY,
    folio VARCHAR(20) NOT NULL,
    id_cliente INT NOT NULL,
    id_asesor INT NOT NULL,
    destino VARCHAR(100) NOT NULL,
    fecha_salida DATE NOT NULL,
    fecha_regreso DATE NOT NULL,
    estatus ENUM('COTIZADA', 'CONFIRMADA', 'EN_CURSO', 'CONCLUIDA', 'CANCELADA')
        NOT NULL DEFAULT 'COTIZADA',
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_operaciones_folio UNIQUE (folio),
    CONSTRAINT chk_operaciones_fechas CHECK (fecha_regreso >= fecha_salida),
    CONSTRAINT fk_operaciones_clientes FOREIGN KEY (id_cliente)
        REFERENCES clientes (id_cliente)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_operaciones_asesores FOREIGN KEY (id_asesor)
        REFERENCES asesores (id_asesor)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- pasajeros | Grado 4 | PK id_pasajero
-- FK: id_operacion -> operaciones (CASCADE al borrar operación).
-- Un pasajero por tupla (atomización del campo multivaluado pasajeros).
-- ------------------------------------------------------------
CREATE TABLE pasajeros (
    id_pasajero INT AUTO_INCREMENT PRIMARY KEY,
    id_operacion INT NOT NULL,
    nombre VARCHAR(60) NOT NULL,
    apellido VARCHAR(80) NOT NULL,
    CONSTRAINT fk_pasajeros_operaciones FOREIGN KEY (id_operacion)
        REFERENCES operaciones (id_operacion)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- servicios_contratados | Grado 11 | PK id_servicio
-- FK: id_operacion (CASCADE), id_proveedor (RESTRICT).
-- Se retiró nombre_proveedor; el nombre vive en proveedores.
-- ------------------------------------------------------------
CREATE TABLE servicios_contratados (
    id_servicio INT AUTO_INCREMENT PRIMARY KEY,
    id_operacion INT NOT NULL,
    id_proveedor INT NOT NULL,
    tipo_servicio ENUM('VUELO', 'HOSPEDAJE', 'CRUCERO', 'SEGURO', 'TRASLADO') NOT NULL,
    descripcion VARCHAR(200) NOT NULL,
    tarifa_neta DECIMAL(12, 2) NOT NULL,
    tarifa_publica DECIMAL(12, 2) NOT NULL,
    moneda CHAR(3) NOT NULL,
    comision DECIMAL(12, 2) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NULL,
    CONSTRAINT chk_servicios_neta CHECK (tarifa_neta > 0),
    CONSTRAINT chk_servicios_publica CHECK (tarifa_publica >= tarifa_neta),
    CONSTRAINT chk_servicios_comision CHECK (comision >= 0),
    CONSTRAINT chk_servicios_moneda CHECK (moneda IN ('MXN', 'USD', 'EUR')),
    CONSTRAINT chk_servicios_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
    CONSTRAINT fk_servicios_operaciones FOREIGN KEY (id_operacion)
        REFERENCES operaciones (id_operacion)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_servicios_proveedores FOREIGN KEY (id_proveedor)
        REFERENCES proveedores (id_proveedor)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- pagos | Grado 10 | PK id_pago
-- FK: id_operacion (CASCADE), id_servicio (sin CASCADE por chk_pagos_flujo_servicio).
-- chk_pagos_flujo_servicio: pago a proveedor exige id_servicio; pago de cliente no.
-- Se retiraron nombre_cliente y destino (normalización 3FN).
-- ------------------------------------------------------------
CREATE TABLE pagos (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_operacion INT NOT NULL,
    id_servicio INT NULL,
    tipo_flujo ENUM('CLIENTE_AGENCIA', 'AGENCIA_PROVEEDOR') NOT NULL,
    monto DECIMAL(12, 2) NOT NULL,
    moneda CHAR(3) NOT NULL,
    tipo_cambio DECIMAL(12, 6) NOT NULL DEFAULT 1.000000,
    fecha_pago DATE NOT NULL,
    estatus ENUM('PENDIENTE', 'PARCIAL', 'LIQUIDADO', 'CANCELADO')
        NOT NULL DEFAULT 'PENDIENTE',
    referencia VARCHAR(50) NULL,
    CONSTRAINT chk_pagos_monto CHECK (monto > 0),
    CONSTRAINT chk_pagos_moneda CHECK (moneda IN ('MXN', 'USD', 'EUR')),
    CONSTRAINT chk_pagos_tipo_cambio CHECK (tipo_cambio > 0),
    CONSTRAINT chk_pagos_flujo_servicio CHECK (
        (tipo_flujo = 'CLIENTE_AGENCIA' AND id_servicio IS NULL)
        OR (tipo_flujo = 'AGENCIA_PROVEEDOR' AND id_servicio IS NOT NULL)
    ),
    CONSTRAINT fk_pagos_operaciones FOREIGN KEY (id_operacion)
        REFERENCES operaciones (id_operacion)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_pagos_servicios FOREIGN KEY (id_servicio)
        REFERENCES servicios_contratados (id_servicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- SECCIÓN: CARGA DE PRUEBA
-- Cardinalidad inicial:
--   clientes 4, proveedores 5, asesores 3, operaciones 4,
--   pasajeros 7, servicios_contratados 8, pagos 6
-- ============================================================

INSERT INTO clientes (rfc, nombre, correo, telefono) VALUES
    ('LOGA850101AB1', 'Ana López García', 'ana.lopez@correo.mx', '9991110001'),
    ('MERC900215CD2', 'Carlos Méndez Ruiz', 'carlos.mendez@correo.mx', '9991110002'),
    ('FEGM880707EF3', 'María Fernández Gil', 'maria.fernandez@correo.mx', '9991110003'),
    ('CAPJ920909GH4', 'Jorge Castillo Pech', 'jorge.castillo@correo.mx', '9991110004');

INSERT INTO proveedores (rfc, nombre_proveedor, tipo_proveedor, correo, telefono) VALUES
    ('AEM920101AA1', 'Aeroméxico', 'AEROLINEA', 'reservas@aeromexico.com', '5551002001'),
    ('HXC150303BB2', 'Hotel Xcaret México', 'HOTEL', 'grupos@hotelxcaret.mx', '9982003002'),
    ('RCL050505CC3', 'Royal Caribbean', 'NAVIERA', 'mexico@royalcaribbean.com', '5551004003'),
    ('AXA100101DD4', 'AXA Seguros', 'ASEGURADORA', 'viajes@axa.mx', '5551005004'),
    ('HAT050808EE5', 'Hotel Artemide Roma', 'HOTEL', 'booking@hotelartemide.it', '3906123456');

INSERT INTO asesores (num_empleado, nombre, apellido, correo) VALUES
    ('EMP-001', 'Luis Luciano', 'Matos Santos', 'luis.matos@viajesmaya.mx'),
    ('EMP-002', 'Felipe', 'Balan Xaman', 'felipe.balan@viajesmaya.mx'),
    ('EMP-003', 'Pedro Roman', 'Dominguez Rodriguez', 'pedro.dominguez@viajesmaya.mx');

INSERT INTO operaciones (folio, id_cliente, id_asesor, destino, fecha_salida, fecha_regreso, estatus) VALUES
    ('OP-2026-001', 1, 1, 'Cancún', '2026-11-10', '2026-11-15', 'CONFIRMADA'),
    ('OP-2026-002', 2, 2, 'Roma', '2026-12-01', '2026-12-08', 'CONFIRMADA'),
    ('OP-2026-003', 3, 3, 'Ciudad de México', '2026-10-20', '2026-10-23', 'COTIZADA'),
    ('OP-2026-004', 4, 1, 'Caribe', '2027-01-05', '2027-01-12', 'CONFIRMADA');

INSERT INTO pasajeros (id_operacion, nombre, apellido) VALUES
    (1, 'Ana', 'López García'),
    (1, 'Sofía', 'López García'),
    (2, 'Carlos', 'Méndez Ruiz'),
    (3, 'María', 'Fernández Gil'),
    (3, 'Luis', 'Fernández Gil'),
    (4, 'Jorge', 'Castillo Pech'),
    (4, 'Elena', 'Castillo Pech');

INSERT INTO servicios_contratados (
    id_operacion, id_proveedor, tipo_servicio, descripcion,
    tarifa_neta, tarifa_publica, moneda, comision, fecha_inicio, fecha_fin
) VALUES
    (1, 1, 'VUELO', 'Vuelo MID-CUN redondo', 8500.00, 9800.00, 'MXN', 700.00, '2026-11-10', '2026-11-15'),
    (1, 2, 'HOSPEDAJE', 'Hotel Xcaret, 5 noches', 18000.00, 21500.00, 'MXN', 2000.00, '2026-11-10', '2026-11-15'),
    (1, 4, 'SEGURO', 'Seguro de viajero', 900.00, 1200.00, 'MXN', 200.00, '2026-11-10', '2026-11-15'),
    (2, 1, 'VUELO', 'Vuelo MEX-FCO', 22000.00, 25900.00, 'MXN', 2400.00, '2026-12-01', '2026-12-08'),
    (2, 5, 'HOSPEDAJE', 'Hotel Artemide, 7 noches', 1400.00, 1680.00, 'EUR', 180.00, '2026-12-01', '2026-12-08'),
    (3, 1, 'VUELO', 'Vuelo MID-MEX', 3200.00, 3800.00, 'MXN', 350.00, '2026-10-20', '2026-10-23'),
    (4, 3, 'CRUCERO', 'Crucero Caribe, 7 noches', 2400.00, 2900.00, 'USD', 300.00, '2027-01-05', '2027-01-12'),
    (4, 4, 'SEGURO', 'Seguro de crucero', 1100.00, 1450.00, 'MXN', 220.00, '2027-01-05', '2027-01-12');

INSERT INTO pagos (id_operacion, id_servicio, tipo_flujo, monto, moneda, tipo_cambio, fecha_pago, estatus, referencia) VALUES
    (1, NULL, 'CLIENTE_AGENCIA', 15000.00, 'MXN', 1.000000, '2026-09-01', 'LIQUIDADO', 'ANT-001'),
    (1, NULL, 'CLIENTE_AGENCIA', 17500.00, 'MXN', 1.000000, '2026-10-01', 'LIQUIDADO', 'LIQ-001'),
    (1, 2, 'AGENCIA_PROVEEDOR', 18000.00, 'MXN', 1.000000, '2026-10-05', 'LIQUIDADO', 'PROV-XC-01'),
    (2, NULL, 'CLIENTE_AGENCIA', 25900.00, 'MXN', 1.000000, '2026-09-15', 'LIQUIDADO', 'ANT-002'),
    (2, 5, 'AGENCIA_PROVEEDOR', 1400.00, 'EUR', 20.150000, '2026-11-01', 'PENDIENTE', 'PROV-ART-01'),
    (4, NULL, 'CLIENTE_AGENCIA', 1000.00, 'USD', 17.250000, '2026-09-20', 'PARCIAL', 'ANT-004');
