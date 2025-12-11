#!/bin/bash

# Keycloak Authorization Demo - Test Script
# This script demonstrates testing the Spring Boot application with Keycloak

echo "🔐 Keycloak Authorization Demo - Test Script"
echo "=========================================="

BASE_URL="http://localhost:8080/api"
KEYCLOAK_URL="http://localhost:8081"
REALM="demo-realm"
CLIENT_ID="spring-demo-app"
CLIENT_SECRET="demo-secret-key"

echo ""
echo "📋 Test Plan:"
echo "1. Test public endpoint (no auth required)"
echo "2. Get access token from Keycloak"
echo "3. Test user endpoint (requires authentication)"
echo "4. Test admin endpoint (requires ADMIN role - should fail)"
echo "5. Test token-info endpoint (shows JWT details)"
echo ""

# Function to check if a service is running
check_service() {
    local url=$1
    local service_name=$2

    if curl -s --head "$url" > /dev/null 2>&1; then
        echo "✅ $service_name is running"
        return 0
    else
        echo "❌ $service_name is not running at $url"
        echo "   Please make sure $service_name is started before running this script"
        return 1
    fi
}

# Check if services are running
echo "🔍 Checking services..."
check_service "$KEYCLOAK_URL/realms/$REALM" "Keycloak" || exit 1
check_service "$BASE_URL/public" "Spring Boot Application" || exit 1

echo ""
echo "🧪 Running Tests..."
echo ""

# Test 1: Public endpoint
echo "1️⃣ Testing public endpoint (no authentication required)"
echo "   GET $BASE_URL/public"
RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" "$BASE_URL/public")
HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS:" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_STATUS:/d')

if [ "$HTTP_STATUS" = "200" ]; then
    echo "   ✅ Success (HTTP $HTTP_STATUS)"
    echo "   Response: $(echo "$BODY" | jq -r '.message' 2>/dev/null || echo "$BODY")"
else
    echo "   ❌ Failed (HTTP $HTTP_STATUS)"
    echo "   Response: $BODY"
fi

echo ""

# Test 2: Get access token
echo "2️⃣ Getting access token from Keycloak"
echo "   POST $KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token"

TOKEN_RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=password" \
    -d "client_id=$CLIENT_ID" \
    -d "client_secret=$CLIENT_SECRET" \
    -d "username=testuser" \
    -d "password=password")

ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r '.access_token' 2>/dev/null)

if [ "$ACCESS_TOKEN" != "null" ] && [ -n "$ACCESS_TOKEN" ]; then
    echo "   ✅ Token obtained successfully"
    echo "   Token: ${ACCESS_TOKEN:0:50}..."
else
    echo "   ❌ Failed to get access token"
    echo "   Response: $TOKEN_RESPONSE"
    exit 1
fi

echo ""

# Test 3: User endpoint
echo "3️⃣ Testing user endpoint (requires authentication)"
echo "   GET $BASE_URL/user"

RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    "$BASE_URL/user")
HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS:" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_STATUS:/d')

if [ "$HTTP_STATUS" = "200" ]; then
    echo "   ✅ Success (HTTP $HTTP_STATUS)"
    USERNAME=$(echo "$BODY" | jq -r '.username' 2>/dev/null || echo "unknown")
    AUTHORITIES=$(echo "$BODY" | jq -r '.authorities | join(", ")' 2>/dev/null || echo "unknown")
    echo "   User: $USERNAME, Authorities: $AUTHORITIES"
else
    echo "   ❌ Failed (HTTP $HTTP_STATUS)"
    echo "   Response: $BODY"
fi

echo ""

# Test 4: Admin endpoint (should fail)
echo "4️⃣ Testing admin endpoint (requires ADMIN role - should fail)"
echo "   GET $BASE_URL/admin"

RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    "$BASE_URL/admin")
HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS:" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_STATUS:/d')

if [ "$HTTP_STATUS" = "403" ]; then
    echo "   ✅ Correctly denied access (HTTP $HTTP_STATUS)"
    echo "   Reason: testuser only has USER role, not ADMIN role"
else
    echo "   ❌ Unexpected response (HTTP $HTTP_STATUS)"
    echo "   Response: $BODY"
fi

echo ""

# Test 5: Token info endpoint
echo "5️⃣ Testing token-info endpoint (shows JWT details)"
echo "   GET $BASE_URL/token-info"

RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    "$BASE_URL/token-info")
HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS:" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_STATUS:/d')

if [ "$HTTP_STATUS" = "200" ]; then
    echo "   ✅ Success (HTTP $HTTP_STATUS)"
    echo "   📋 Token Information:"
    echo "   Subject: $(echo "$BODY" | jq -r '.identityClaims.sub' 2>/dev/null || echo "N/A")"
    echo "   Username: $(echo "$BODY" | jq -r '.identityClaims.preferred_username' 2>/dev/null || echo "N/A")"
    echo "   Roles: $(echo "$BODY" | jq -r '.authorizationClaims.realm_access.roles | join(", ")' 2>/dev/null || echo "N/A")"
    echo "   Issuer: $(echo "$BODY" | jq -r '.authorizationClaims.iss' 2>/dev/null || echo "N/A")"
else
    echo "   ❌ Failed (HTTP $HTTP_STATUS)"
    echo "   Response: $BODY"
fi

echo ""
echo "🎉 Test script completed!"
echo ""
echo "💡 Next steps:"
echo "   - Create an admin user in Keycloak to test /admin endpoint"
echo "   - Check the README.md for detailed explanations"
echo "   - Explore the H2 console at http://localhost:8080/h2-console"