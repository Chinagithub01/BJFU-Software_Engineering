#!/bin/bash
set -e

WAR="/usr/local/tomcat/webapps/ROOT.war"
APP_DIR="/usr/local/tomcat/webapps/ROOT"
UPLOADS_DIR="${UPLOADS_DIR:-/data/uploads}"

mkdir -p "$UPLOADS_DIR"

if [ -f "$WAR" ]; then
  rm -rf "$APP_DIR"
  mkdir -p "$APP_DIR"
  cd "$APP_DIR" && jar xf "$WAR" && rm -f "$WAR"
fi

mkdir -p "$APP_DIR"
rm -rf "$APP_DIR/uploads"
ln -sfn "$UPLOADS_DIR" "$APP_DIR/uploads"

cat > "$APP_DIR/WEB-INF/classes/jdbc.properties" <<EOF
jdbc.driver=org.postgresql.Driver
jdbc.url=${JDBC_URL}
jdbc.username=${JDBC_USERNAME}
jdbc.password=${JDBC_PASSWORD}
EOF

echo ">> JDBC configured for ${JDBC_URL}"

exec catalina.sh run
