#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

configurar_java() {
  if javac -version >/dev/null 2>&1; then
    return 0
  fi
  if [ -d "/opt/homebrew/opt/openjdk@21/bin" ]; then
    export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
  fi
  if javac -version >/dev/null 2>&1; then
    return 0
  fi
  echo "Error: no se encontro Java (JDK 17 o superior). Instale OpenJDK, por ejemplo: brew install openjdk@21"
  exit 1
}

configurar_java

LIB_DIR="lib"
OUT_DIR="out"
mkdir -p "$LIB_DIR" "$OUT_DIR"

if ! ls "$LIB_DIR"/mysql-connector-j-*.jar >/dev/null 2>&1; then
  echo "Descargando conector MySQL JDBC..."
  curl -fsSL -o "$LIB_DIR/mysql-connector-j-8.4.0.jar" \
    "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
fi

echo "Compilando..."
javac -cp "$LIB_DIR/*" -d "$OUT_DIR" src/*.java

echo "Ejecutando aplicación..."
java -cp "$OUT_DIR:$LIB_DIR/*" Main
