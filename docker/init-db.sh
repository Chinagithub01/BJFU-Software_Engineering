#!/bin/sh
set -e

echo ">> Waiting for PostgreSQL at ${PGHOST}:${PGPORT}..."
for i in $(seq 1 60); do
  if pg_isready -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" >/dev/null 2>&1; then
    echo ">> PostgreSQL is ready"
    break
  fi
  if [ "$i" -eq 60 ]; then
    echo ">> PostgreSQL did not become ready in time"
    exit 1
  fi
  sleep 2
done

echo ">> Running init.sql..."
psql -v ON_ERROR_STOP=1 -f /sql/init.sql

echo ">> Running test_data.sql..."
psql -v ON_ERROR_STOP=1 -f /sql/test_data.sql

echo ">> Database initialization complete"
