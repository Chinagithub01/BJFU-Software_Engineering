#!/bin/sh
set -e

ENDPOINT="${MINIO_ENDPOINT:-http://minio:9000}"
ALIAS="${MINIO_ALIAS:-local}"
BUCKET="${MINIO_BUCKET:-peerreview-uploads}"
USER="${MINIO_ROOT_USER:-minioadmin}"
PASS="${MINIO_ROOT_PASSWORD:-minioadmin123}"

echo ">> Waiting for MinIO at ${ENDPOINT}..."
for i in $(seq 1 60); do
  if mc alias set "$ALIAS" "$ENDPOINT" "$USER" "$PASS" >/dev/null 2>&1; then
    if mc ready "$ALIAS" >/dev/null 2>&1; then
      echo ">> MinIO is ready"
      break
    fi
  fi
  if [ "$i" -eq 60 ]; then
    echo ">> MinIO did not become ready in time"
    exit 1
  fi
  sleep 2
done

echo ">> Ensuring bucket ${BUCKET}..."
mc mb -p "${ALIAS}/${BUCKET}" 2>/dev/null || true

echo ">> MinIO bucket initialization complete"
