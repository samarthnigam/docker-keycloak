#!/bin/bash

echo "Testing JWT Authentication for Library Management API"
echo "=================================================="

# Test 1: Access without authentication (should return 401)
echo ""
echo "Test 1: Accessing /api/books without authentication"
echo "--------------------------------------------------"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:8082/api/books

# Test 2: Access with JWT token (should return 200)
echo ""
echo "Test 2: Accessing /api/books with JWT token"
echo "-------------------------------------------"
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJpYXQiOjE2MzgzNjgwMDAsImV4cCI6MTk5OTk5OTk5OX0.test"
curl -s -H "Authorization: Bearer $TOKEN" -w "HTTP Status: %{http_code}\nResponse: %{response_code}\n" http://localhost:8082/api/books

echo ""
echo "Test completed."