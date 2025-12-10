#!/bin/bash

# Library Management System Runner
echo "Starting Library Management System..."

# Ensure we're in the correct directory
cd "$(dirname "$0")"

# Clean and run the application
mvn clean spring-boot:run

echo "Library Management System stopped."