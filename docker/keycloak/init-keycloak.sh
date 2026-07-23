#!/bin/bash

echo "========================================="
echo "Keycloak Roles - ADMIN → SYSTEM_ADMIN"
echo "========================================="

# Цвета
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

KEYCLOAK_URL="http://localhost:8080"
ADMIN_USER="admin"
ADMIN_PASS="admin"
REALM="car-dealership"

# Получение токена администратора
echo -e "${YELLOW}Getting admin token...${NC}"
TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASS" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "Failed to get admin token"
    exit 1
fi
echo -e "${GREEN}Admin token obtained${NC}"

# 1. Получить ID роли ADMIN
echo -e "${YELLOW}Getting ADMIN role ID...${NC}"
ADMIN_ROLE_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM/roles/ADMIN" \
  -H "Authorization: Bearer $TOKEN" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)

echo "ADMIN role ID: $ADMIN_ROLE_ID"

# 2. Создать роль SYSTEM_ADMIN
echo -e "${YELLOW}Creating SYSTEM_ADMIN role...${NC}"
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM/roles" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "SYSTEM_ADMIN",
    "description": "System Administrator"
  }'
echo -e "${GREEN}SYSTEM_ADMIN role created${NC}"

# 3. Получить ID пользователя admin1
echo -e "${YELLOW}Getting admin1 user ID...${NC}"
ADMIN_USER_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM/users?username=admin1" \
  -H "Authorization: Bearer $TOKEN" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)

echo "Admin user ID: $ADMIN_USER_ID"

# 4. Добавить пользователю роль SYSTEM_ADMIN
echo -e "${YELLOW}Assigning SYSTEM_ADMIN role to admin1...${NC}"
SYSTEM_ADMIN_ROLE_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM/roles/SYSTEM_ADMIN" \
  -H "Authorization: Bearer $TOKEN" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)

curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM/users/$ADMIN_USER_ID/role-mappings/realm" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "[{\"id\": \"$SYSTEM_ADMIN_ROLE_ID\", \"name\": \"SYSTEM_ADMIN\"}]"
echo -e "${GREEN}SYSTEM_ADMIN role assigned to admin1${NC}"

# 5. (Опционально) Удалить старую роль ADMIN у admin1
echo -e "${YELLOW}Removing old ADMIN role from admin1...${NC}"
curl -s -X DELETE "$KEYCLOAK_URL/admin/realms/$REALM/users/$ADMIN_USER_ID/role-mappings/realm" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "[{\"id\": \"$ADMIN_ROLE_ID\", \"name\": \"ADMIN\"}]"
echo -e "${GREEN}ADMIN role removed from admin1${NC}"

echo "========================================="
echo -e "${GREEN}Done!${NC}"
echo "User admin1 now has SYSTEM_ADMIN role"
echo "========================================="