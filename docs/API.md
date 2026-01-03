# Coupon System REST API Documentation

**Base URL:** `http://localhost:8080/api/v1`
**Authentication:** JWT Bearer Token
**API Version:** 1.0.0

## 📑 Table of Contents
- [Authentication](#authentication)
- [Admin Endpoints](#admin-endpoints)
- [Company Endpoints](#company-endpoints)
- [Customer Endpoints](#customer-endpoints)
- [Public Endpoints](#public-endpoints)
- [Error Responses](#error-responses)
- [Rate Limiting](#rate-limiting)

---

## Authentication

### Login
Authenticate and receive JWT tokens.

**Endpoint:** `POST /auth/login`
**Auth Required:** No
**Rate Limit:** 5 requests/minute per IP

#### Request
```json
{
  "email": "admin@admin.com",
  "password": "admin",
  "clientType": "admin"  // "admin", "company", or "customer"
}
```

#### Response (200 OK)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userInfo": {
    "userId": 1,
    "email": "admin@admin.com",
    "clientType": "admin",
    "name": "Administrator"
  }
}
```

#### Error Responses
- `401 Unauthorized` - Invalid credentials
- `403 Forbidden` - Account locked (too many failed attempts)
- `429 Too Many Requests` - Rate limit exceeded

---

### Refresh Token
Get new access token using refresh token.

**Endpoint:** `POST /auth/refresh`
**Auth Required:** No

#### Request
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Response (200 OK)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userInfo": { ... }
}
```

---

## Admin Endpoints

> **Required Role:** `ADMIN`
> **Header:** `Authorization: Bearer {accessToken}`

### Companies

#### Get All Companies
**GET** `/admin/companies`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Acme Corp",
    "email": "contact@acme.com",
    "coupons": []
  }
]
```

---

#### Get Company by ID
**GET** `/admin/companies/{id}`

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Acme Corp",
  "email": "contact@acme.com",
  "coupons": [...]
}
```

---

#### Create Company
**POST** `/admin/companies`

**Request:**
```json
{
  "name": "New Company",
  "email": "company@example.com",
  "password": "SecurePass123"
}
```

**Response (201 Created):**
```json
{
  "id": 2,
  "name": "New Company",
  "email": "company@example.com"
}
```

---

#### Update Company
**PUT** `/admin/companies/{id}`

**Request:**
```json
{
  "name": "Updated Company Name",
  "email": "newemail@example.com",
  "password": "NewSecurePass123"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Updated Company Name",
  "email": "newemail@example.com"
}
```

---

#### Delete Company
**DELETE** `/admin/companies/{id}`

**Response:** `204 No Content`

**Error:** `409 Conflict` - Company has active coupons

---

### Customers

#### Get All Customers
**GET** `/admin/customers`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "coupons": []
  }
]
```

---

#### Create Customer
**POST** `/admin/customers`

**Request:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane@example.com",
  "password": "SecurePass123"
}
```

**Response (201 Created):**
```json
{
  "id": 2,
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane@example.com"
}
```

---

#### Update Customer
**PUT** `/admin/customers/{id}`

#### Delete Customer
**DELETE** `/admin/customers/{id}`

**Response:** `204 No Content`

---

### Account Management

#### Unlock Company Account
**POST** `/admin/companies/{email}/unlock`

**Response:** `204 No Content`

---

#### Unlock Customer Account
**POST** `/admin/customers/{email}/unlock`

**Response:** `204 No Content`

---

## Company Endpoints

> **Required Role:** `COMPANY`
> **Header:** `Authorization: Bearer {accessToken}`

### Coupons

#### Get Company Details
**GET** `/company/details`

**Response (200 OK):**
```json
{
  "id": 5,
  "name": "My Company",
  "email": "company@example.com",
  "coupons": [...]
}
```

---

#### Get All Company Coupons
**GET** `/company/coupons`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "companyId": 5,
    "category": "FOOD",
    "title": "Pizza Deal",
    "description": "Buy 1 Get 1 Free",
    "startDate": "2026-01-01",
    "endDate": "2026-12-31",
    "amount": 100,
    "price": 9.99,
    "image": "pizza.jpg"
  }
]
```

---

#### Get Coupons by Category
**GET** `/company/coupons/category/{category}`

**Path Parameters:**
- `category`: FOOD, ELECTRICITY, RESTAURANT, VACATION, etc.

---

#### Create Coupon
**POST** `/company/coupons`

**Request:**
```json
{
  "category": "FOOD",
  "title": "Summer Sale",
  "description": "50% off all items",
  "startDate": "2026-06-01",
  "endDate": "2026-08-31",
  "amount": 500,
  "price": 19.99,
  "image": "summer-sale.jpg"
}
```

**Response (201 Created):**
```json
{
  "id": 10,
  "companyId": 5,
  "category": "FOOD",
  "title": "Summer Sale",
  ...
}
```

---

#### Update Coupon
**PUT** `/company/coupons/{id}`

**Request:**
```json
{
  "category": "FOOD",
  "title": "Updated Title",
  "description": "Updated description",
  "startDate": "2026-06-01",
  "endDate": "2026-08-31",
  "amount": 450,
  "price": 17.99,
  "image": "updated.jpg"
}
```

**Response (200 OK):**
```json
{
  "id": 10,
  "companyId": 5,
  ...
}
```

---

#### Delete Coupon
**DELETE** `/company/coupons/{id}`

**Response:** `204 No Content`

**Error:** `409 Conflict` - Coupon has been purchased by customers

---

## Customer Endpoints

> **Required Role:** `CUSTOMER`
> **Header:** `Authorization: Bearer {accessToken}`

### Profile

#### Get Customer Details
**GET** `/customer/details`

**Response (200 OK):**
```json
{
  "id": 100,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "coupons": [...]
}
```

---

### Purchases

#### Purchase Coupon
**POST** `/customer/coupons/{couponId}/purchase`

**Response (200 OK):**
```json
{
  "message": "Coupon purchased successfully",
  "couponId": 10
}
```

**Error Responses:**
- `409 Conflict` - Coupon already purchased by customer
- `409 Conflict` - Coupon out of stock
- `404 Not Found` - Coupon not found or expired

---

#### Get Purchased Coupons
**GET** `/customer/coupons`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "companyId": 5,
    "category": "FOOD",
    "title": "Pizza Deal",
    ...
  }
]
```

---

#### Get Purchased Coupons by Category
**GET** `/customer/coupons/category/{category}`

---

#### Get Coupons by Max Price
**GET** `/customer/coupons/price/{maxPrice}`

**Path Parameters:**
- `maxPrice`: Maximum price (e.g., 50.00)

---

## Public Endpoints

> **Auth Required:** No

### Browse Coupons

#### Get All Available Coupons
**GET** `/public/coupons`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "companyId": 5,
    "category": "FOOD",
    "title": "Pizza Deal",
    "description": "Buy 1 Get 1 Free",
    "startDate": "2026-01-01",
    "endDate": "2026-12-31",
    "amount": 95,
    "price": 9.99,
    "image": "pizza.jpg"
  }
]
```

---

#### Get Coupon by ID
**GET** `/public/coupons/{id}`

**Response (200 OK):**
```json
{
  "id": 1,
  "companyId": 5,
  "category": "FOOD",
  ...
}
```

---

## Error Responses

### Standard Error Format
```json
{
  "timestamp": "2026-01-03T14:30:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/admin/companies",
  "validationErrors": [
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "message": "Email must be valid"
    }
  ]
}
```

### Common HTTP Status Codes
- **200 OK** - Request successful
- **201 Created** - Resource created successfully
- **204 No Content** - Request successful, no response body
- **400 Bad Request** - Invalid request data
- **401 Unauthorized** - Invalid or missing authentication
- **403 Forbidden** - Insufficient permissions or account locked
- **404 Not Found** - Resource not found
- **409 Conflict** - Resource conflict (duplicate, dependencies exist)
- **429 Too Many Requests** - Rate limit exceeded
- **500 Internal Server Error** - Server error

---

## Rate Limiting

### Rate Limit Headers
All responses include rate limiting headers:

```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
```

### Rate Limit Response (429)
When rate limit is exceeded:

```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again in 45 seconds."
}
```

**Headers:**
```http
X-RateLimit-Retry-After-Seconds: 45
```

### Rate Limits
- **Authentication endpoints** (`/auth/*`): 5 requests/minute
- **All other endpoints**: 100 requests/minute
- **Per IP address** (uses `X-Forwarded-For` if behind proxy)

---

## Interactive Documentation

### Swagger UI
Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI Spec
JSON specification available at:
```
http://localhost:8080/v3/api-docs
```

---

## Example: Complete Workflow

### 1. Admin Creates Company
```bash
# Login as admin
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@admin.com","password":"admin","clientType":"admin"}'

# Create company
curl -X POST http://localhost:8080/api/v1/admin/companies \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{"name":"Pizza Palace","email":"pizza@example.com","password":"SecurePass123"}'
```

### 2. Company Creates Coupon
```bash
# Login as company
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"pizza@example.com","password":"SecurePass123","clientType":"company"}'

# Create coupon
curl -X POST http://localhost:8080/api/v1/company/coupons \
  -H "Authorization: Bearer {company_token}" \
  -H "Content-Type: application/json" \
  -d '{"category":"FOOD","title":"Lunch Special","description":"50% off","startDate":"2026-01-01","endDate":"2026-12-31","amount":100,"price":5.99,"image":"lunch.jpg"}'
```

### 3. Customer Purchases Coupon
```bash
# Login as customer
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"customer@example.com","password":"password","clientType":"customer"}'

# Purchase coupon
curl -X POST http://localhost:8080/api/v1/customer/coupons/1/purchase \
  -H "Authorization: Bearer {customer_token}"
```

---

## Security Best Practices

1. **Store tokens securely** - Use httpOnly cookies or secure storage
2. **Refresh tokens before expiry** - Access tokens expire after 1 hour
3. **Handle 401 responses** - Implement automatic token refresh
4. **Use HTTPS in production** - Never send tokens over unencrypted connections
5. **Implement CORS properly** - Configure allowed origins in production
6. **Monitor rate limits** - Check `X-RateLimit-*` headers to avoid throttling

---

## Support
- **GitHub Issues:** https://github.com/ksalhab89/CouponSystemProject/issues
- **Documentation:** See README.md for setup and deployment guides
