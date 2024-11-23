#!/bin/bash
set -e

# Check if the user 'keycloak' exists
USER_EXISTS=$(psql -U "$POSTGRES_USER" -tAc "SELECT 1 FROM pg_roles WHERE rolname='keycloak'")

# Execute commands only if 'keycloak' user does not already exist
if [[ "$USER_EXISTS" != "1" ]]; then
  echo "Creating user 'keycloak' and setting up database permissions..."
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
      CREATE USER keycloak WITH SUPERUSER PASSWORD 'keycloak';
      CREATE DATABASE keycloak;
	  CREATE SCHEMA keycloak;
      GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
      GRANT ALL ON SCHEMA public TO keycloak;
EOSQL
else
  echo "User 'keycloak' already exists. Skipping user and database creation."
fi
