#!/bin/bash

# Run Keycloak POC
echo "Building and running Keycloak POC..."

# Ensure we're in the correct directory
cd "$(dirname "$0")"

# Clean and compile
mvn clean compile

# Run the application
mvn exec:java

echo "POC execution completed."