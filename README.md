# Agencia de Viajes – Entrega final (Proyecto Integrador)

Aplicación de consola en Java (JDBC) para administrar **clientes** y **operaciones** de una agencia de viajes. La base de datos MySQL 8.0 corre en Docker; el esquema (7 tablas, 3FN) proviene del Avance 3.

**Integrantes:** Luis Luciano Matos Santos, Felipe Balan Xaman, Pedro Roman Dominguez Rodriguez.

## Requisitos

- Docker Desktop (o Docker Engine) con `docker compose`
- Java **17+** (probado con OpenJDK **21.0.12.1** en macOS). `./run.sh` intenta usar Homebrew `openjdk@21` si el `javac` del sistema no funciona; si falla: `brew install openjdk@21`
- `curl` (para descargar el conector JDBC la primera vez)

## Configuración y ejecución

1. Clonar o copiar este repositorio y entrar a la carpeta:

   ```bash
   cd proyecto_final
   ```

2. Levantar MySQL:

   ```bash
   docker compose up -d
   ```

   Espere unos segundos a que MySQL termine de inicializar. Si ya existe un contenedor llamado `mysql_agencia` de otra carpeta, deténgalo o reutilícelo en el puerto **3307**.

3. Ejecutar la aplicación:

   ```bash
   chmod +x run.sh
   ./run.sh
   ```

   La app se conecta a `localhost:3307`, base `agencia_viajes`, usuario `root`, contraseña `root_password_clase` (solo entorno de demostración).

Variables opcionales: `JDBC_URL`, `DB_USER`, `DB_PASSWORD`.

## Estructura del repositorio

```
proyecto_final/
  src/              Código Java (Main, DAOs, modelos)
  sql/              DDL y consultas de evidencia
  docker-compose.yml
  run.sh
  docs/             Guion de video y evidencias (consulta JOIN)
  criterios.md      Checklist de entrega
  fuera_de_alcance.md
```

## Esquema y CRUD

- **7 tablas:** clientes, proveedores, asesores, operaciones, pasajeros, servicios_contratados, pagos.
- **CRUD en la app:** solo `clientes` y `operaciones` (relacionadas por `operaciones.id_cliente`).
- **Consulta JOIN:** menú principal → opción 3 (operaciones + clientes + asesores).

## Reiniciar la base desde cero

```bash
docker compose down -v
docker compose up -d
```

Esto vuelve a ejecutar `sql/agencia_viajes.sql` y la carga de prueba.

## Consultas de evidencia (SQL)

```bash
docker exec -i mysql_agencia mysql -uroot -proot_password_clase agencia_viajes < sql/consultas_evidencia.sql
```


