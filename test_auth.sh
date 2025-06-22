#!/bin/bash

echo "=== Testing Spring Security with Database-Backed Users ==="
echo

# Test public endpoint (should work without authentication)
echo "1. Testing PUBLIC endpoint (no auth required):"
curl -s http://localhost:8080/public/hello | jq .
echo

# Test private endpoint with user credentials
echo "2. Testing PRIVATE endpoint with 'user' credentials:"
curl -s -u user:password http://localhost:8080/private/user-info | jq .
echo

# Test admin endpoint with user credentials (should fail)
echo "3. Testing ADMIN endpoint with 'user' credentials (should fail):"
curl -s -u user:password http://localhost:8080/admin/dashboard | jq .
echo

# Test admin endpoint with admin credentials
echo "4. Testing ADMIN endpoint with 'admin' credentials:"
curl -s -u admin:admin123 http://localhost:8080/admin/dashboard | jq .
echo

# Test private endpoint with admin credentials
echo "5. Testing PRIVATE endpoint with 'admin' credentials:"
curl -s -u admin:admin123 http://localhost:8080/private/user-info | jq .
echo

echo "=== Test Complete ===" 