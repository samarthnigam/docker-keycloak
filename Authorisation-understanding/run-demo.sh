#!/bin/bash

# Keycloak Authorization Demo - Run Script
# This script helps you get the demo running quickly

echo "🚀 Keycloak Authorization Demo - Quick Start"
echo "==========================================="

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo ""
echo "📁 Project Directory: $PROJECT_DIR"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17+ first."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

echo ""
echo "✅ Prerequisites check passed"

echo ""
echo "🔑 Step 1: Starting Keycloak with Docker Compose"
echo "------------------------------------------------"

cd "$PROJECT_DIR"
docker-compose up -d

echo "⏳ Waiting for Keycloak to be ready..."
sleep 30

# Check if Keycloak is running
if curl -s http://localhost:8081/realms/master > /dev/null; then
    echo "✅ Keycloak is running on http://localhost:8081"
else
    echo "❌ Keycloak failed to start. Please check Docker logs:"
    echo "   docker-compose logs keycloak"
    exit 1
fi

echo ""
echo "📦 Step 2: Importing Demo Realm"
echo "------------------------------"

# Import realm using Keycloak Admin API
echo "⏳ Importing demo-realm.json..."

# First, get admin token
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8081/realms/master/protocol/openid-connect/token \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=password" \
    -d "client_id=admin-cli" \
    -d "username=admin" \
    -d "password=admin" | jq -r '.access_token')

if [ "$ADMIN_TOKEN" = "null" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo "❌ Failed to get admin token"
    exit 1
fi

# Import the realm
IMPORT_RESULT=$(curl -s -X POST http://localhost:8081/admin/realms \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d @"$PROJECT_DIR/demo-realm.json")

if [ $? -eq 0 ]; then
    echo "✅ Demo realm imported successfully"
else
    echo "❌ Failed to import realm"
    echo "Response: $IMPORT_RESULT"
    exit 1
fi

echo ""
echo "🔧 Step 3: Starting Spring Boot Application"
echo "------------------------------------------"

# Start Spring Boot in background
cd "$PROJECT_DIR"
mvn spring-boot:run &
SPRING_PID=$!

echo "⏳ Waiting for Spring Boot to start..."
sleep 15

# Check if Spring Boot is running
if curl -s http://localhost:8080/public > /dev/null; then
    echo "✅ Spring Boot application is running on http://localhost:8080"
else
    echo "❌ Spring Boot application failed to start"
    echo "Check the logs above for errors"
    kill $SPRING_PID 2>/dev/null
    exit 1
fi

echo ""
echo "🎉 Setup Complete!"
echo "=================="
echo ""
echo "🌐 Services running:"
echo "   Keycloak:     http://localhost:8081"
echo "   Spring Boot:  http://localhost:8080"
echo "   H2 Console:   http://localhost:8080/h2-console"
echo ""
echo "🧪 Run the test script:"
echo "   ./test-demo.sh"
echo ""
echo "📖 Or test manually:"
echo "   curl http://localhost:8080/public"
echo ""
echo "🛑 To stop everything:"
echo "   docker-compose down"
echo "   pkill -f 'spring-boot:run'"
echo ""
echo "📚 Read README.md for detailed explanations and manual testing steps"