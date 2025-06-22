#!/bin/bash

echo "=== Testing JWT Authentication ==="

# Base URL
BASE_URL="http://localhost:8080"

echo "1. Testing Registration..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com"
  }')

echo "Register Response: $REGISTER_RESPONSE"

# Extract tokens
ACCESS_TOKEN=$(echo $REGISTER_RESPONSE | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)
REFRESH_TOKEN=$(echo $REGISTER_RESPONSE | grep -o '"refresh_token":"[^"]*"' | cut -d'"' -f4)

echo "Access Token: $ACCESS_TOKEN"
echo "Refresh Token: $REFRESH_TOKEN"

echo "2. Testing Protected Endpoint..."
PROTECTED_RESPONSE=$(curl -s -X GET "$BASE_URL/private/hello" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "Protected Response: $PROTECTED_RESPONSE"

echo "3. Testing Token Refresh..."
REFRESH_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/refresh" \
  -H "Authorization: Bearer $REFRESH_TOKEN")

echo "Refresh Response: $REFRESH_RESPONSE"

echo "4. Testing Invalid Token..."
INVALID_RESPONSE=$(curl -s -X GET "$BASE_URL/private/hello" \
  -H "Authorization: Bearer invalid-token")

echo "Invalid Token Response: $INVALID_RESPONSE"

echo "=== JWT Testing Complete ===" 