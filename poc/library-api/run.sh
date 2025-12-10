#!/bin/bash

# Library API Runner
echo "Starting Library API..."

# Ensure we're in the correct directory
cd "$(dirname "$0")"

# Clean and run the application
mvn clean spring-boot:run

echo "Library API stopped."