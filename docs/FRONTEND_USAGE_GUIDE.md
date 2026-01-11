# Coupon System - Frontend Usage Guide

## Table of Contents
- [Quick Start](#quick-start)
- [Test Accounts](#test-accounts)
- [User Roles & Features](#user-roles--features)
- [How to Use Each Portal](#how-to-use-each-portal)
- [API Documentation](#api-documentation)
- [Troubleshooting](#troubleshooting)

---

## Quick Start

### Access the Application
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:9090/metrics

### System Architecture
```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│  React Frontend │─────▶│  Spring Backend │─────▶│  MySQL Database │
│  (Port 3000)    │      │  (Port 8080)    │      │  (Port 3306)    │
└─────────────────┘      └─────────────────┘      └─────────────────┘
```

---

## Test Accounts

The database has been pre-populated with sample data. All accounts use the same password for testing purposes.

### Default Password
**Password for all users**: `password123`

### Admin Account
- **Email**: `admin@yourcompany.com`
- **Password**: `your_secure_admin_password_min_16_chars`
- **Role**: Administrator
- **Capabilities**: Full system management

### Company Accounts
| Company Name | Email | Coupons |
|--------------|-------|---------|
| Sky Adventures Ltd | contact@skyadventures.com | 3 coupons (Skiing & Skydiving) |
| Mountain Sports Co | info@mountainsports.com | 2 coupons (Skiing) |
| Gourmet Dining Group | reservations@gourmetdining.com | 4 coupons (Restaurants) |
| Paradise Resorts International | bookings@paradiseresorts.com | 4 coupons (Vacations) |
| Extreme Sports Inc | extreme@sports.com | 2 coupons (Skydiving) |

### Customer Accounts
| Name | Email | Purchased Coupons |
|------|-------|------------------|
| John Smith | john.smith@email.com | 2 purchases |
| Sarah Johnson | sarah.j@email.com | 2 purchases |
| Michael Brown | mbrown@email.com | 2 purchases |
| Emily Davis | emily.davis@email.com | 2 purchases |
| David Wilson | d.wilson@email.com | 2 purchases |

---

## User Roles & Features

### 1. Public (No Login Required)
**Access**: Homepage at http://localhost:3000

**Features**:
- Browse all available coupons
- Filter coupons by category (Skiing, Sky Diving, Restaurants, Vacations)
- Filter coupons by maximum price
- View coupon details (title, description, price, availability)
- Navigate to login page

**Limitations**:
- Cannot purchase coupons
- Cannot view purchased coupons
- Cannot access user-specific features

---

### 2. Customer Portal
**Access**: After login with customer credentials

**Dashboard**: http://localhost:3000/customer

#### Features:

##### Browse & Purchase Coupons
1. **Browse All Coupons**
   - View all active coupons from all companies
   - Filter by category (4 categories available)
   - Filter by maximum price
   - See stock availability

2. **Purchase Coupons**
   - Click "Purchase" button on any coupon
   - System validates:
     - Coupon is in stock (amount > 0)
     - Customer hasn't already purchased this coupon
     - Coupon is within valid date range
   - Successful purchase reduces stock by 1
   - Purchase is recorded in customer's history

3. **View Purchase History**
   - Navigate to "My Purchases" page
   - See all coupons you've purchased
   - Filter purchased coupons by:
     - Category
     - Maximum price
   - View total purchases count

#### Navigation:
- Dashboard: Main overview
- Browse Coupons: Shop for new coupons
- My Purchases: View your purchase history
- Logout: Return to homepage

---

### 3. Company Portal
**Access**: After login with company credentials

**Dashboard**: http://localhost:3000/company

#### Features:

##### Manage Coupons
1. **View All Company Coupons**
   - See only coupons created by your company
   - Filter by category
   - Filter by maximum price
   - View stock levels and pricing

2. **Create New Coupon**
   - Click "Create New Coupon" button
   - Fill in required fields:
     - **Title**: Coupon name (max 48 characters)
     - **Description**: Brief description (max 48 characters)
     - **Category**: Choose from dropdown
       - Skiing
       - Sky Diving
       - Fancy Restaurant
       - All Inclusive Vacation
     - **Start Date**: When coupon becomes valid
     - **End Date**: Expiration date (must be after start date)
     - **Amount**: Number of coupons available (must be > 0)
     - **Price**: Coupon price (must be > 0)
     - **Image**: Image filename (max 48 characters)
   - System validates:
     - No duplicate coupon titles for the same company
     - End date is after start date
     - Price and amount are positive numbers

3. **Edit Existing Coupon**
   - Click "Edit" button on any coupon card
   - Modify any field except company ID
   - System re-validates all constraints
   - Changes saved immediately

4. **Delete Coupon**
   - Click "Delete" button on any coupon card
   - Confirmation dialog appears
   - Once confirmed, coupon is permanently deleted
   - Warning: This action cannot be undone

#### Navigation:
- Dashboard: Company overview
- My Coupons: Manage your coupons
- Create Coupon: Add new coupon
- Logout: Return to homepage

---

### 4. Admin Portal
**Access**: After login with admin credentials

**Dashboard**: http://localhost:3000/admin

#### Features:

##### Company Management
1. **View All Companies**
   - See complete list of registered companies
   - View company details (name, email, account status)
   - Check account lockout status

2. **Add New Company**
   - Click "Add Company" button
   - Enter:
     - Company name (must be unique)
     - Email address (must be unique)
     - Password (min 8 characters)
   - System creates account with BCrypt password hash

3. **Edit Company**
   - Click "Edit" on company row
   - Modify name, email, or password
   - Save changes

4. **Delete Company**
   - Click "Delete" on company row
   - Confirmation required
   - Deletes company and ALL its coupons (cascade delete)

5. **Unlock Locked Accounts**
   - If a company account is locked (too many failed logins)
   - Admin can manually unlock
   - Resets failed login attempts counter

##### Customer Management
1. **View All Customers**
   - See complete list of registered customers
   - View customer details (name, email, purchase count)
   - Check account lockout status

2. **Add New Customer**
   - Click "Add Customer" button
   - Enter:
     - First name
     - Last name
     - Email address (must be unique)
     - Password (min 8 characters)

3. **Edit Customer**
   - Click "Edit" on customer row
   - Modify name, email, or password
   - Save changes

4. **Delete Customer**
   - Click "Delete" on customer row
   - Confirmation required
   - Removes customer and their purchase history

5. **Unlock Locked Accounts**
   - Same as company unlock feature

##### Dashboard Statistics
- Total number of companies
- Total number of customers
- Total number of coupons in system
- Total purchases made
- Recent activity overview

#### Navigation:
- Dashboard: System overview with stats
- Manage Companies: Company CRUD operations
- Manage Customers: Customer CRUD operations
- Logout: Return to homepage

---

## How to Use Each Portal

### Step-by-Step: Customer Journey

#### 1. Browse Coupons (No Login)
1. Go to http://localhost:3000
2. Click "Browse Coupons" in navigation
3. Use filters to narrow down options:
   - Select category from dropdown
   - Enter maximum price
4. View coupon details

#### 2. Create Customer Account & Login
1. Contact admin to create an account, OR
2. Use existing test account: `john.smith@email.com` / `password123`
3. Click "Login" in navigation
4. Select "Customer" as role
5. Enter email and password
6. Click "Sign In"

#### 3. Purchase Coupons
1. After login, navigate to "Browse Coupons"
2. Find a coupon you want
3. Check stock availability (must be > 0)
4. Click "Purchase" button
5. System validates and confirms purchase
6. Success message appears
7. Stock count decreases by 1

#### 4. View Purchase History
1. Navigate to "My Purchases"
2. See all your purchased coupons
3. Use filters if needed
4. Total purchase count displayed

---

### Step-by-Step: Company Journey

#### 1. Company Login
1. Go to http://localhost:3000
2. Click "Login"
3. Select "Company" as role
4. Enter email: `contact@skyadventures.com`
5. Password: `password123`
6. Click "Sign In"

#### 2. Create a Coupon
1. Click "Create New Coupon" button
2. Fill in the form:
   ```
   Title: Summer Beach Special
   Description: 7-day tropical getaway with meals
   Category: All Inclusive Vacation
   Start Date: 2026-02-01
   End Date: 2026-08-31
   Amount: 50
   Price: 1999.99
   Image: beach-vacation.jpg
   ```
3. Click "Create Coupon"
4. Success message appears
5. New coupon appears in "My Coupons" list

#### 3. Edit a Coupon
1. Go to "My Coupons"
2. Find the coupon to edit
3. Click "Edit" button
4. Modify fields (e.g., increase price, adjust dates)
5. Click "Save Changes"
6. Updated coupon reflects new values

#### 4. Delete a Coupon
1. Go to "My Coupons"
2. Find the coupon to delete
3. Click "Delete" button
4. Confirm deletion in dialog
5. Coupon removed immediately

---

### Step-by-Step: Admin Journey

#### 1. Admin Login
1. Go to http://localhost:3000
2. Click "Login"
3. Select "Administrator" as role
4. Enter admin credentials
5. Click "Sign In"

#### 2. Add a New Company
1. Navigate to "Manage Companies"
2. Click "Add Company"
3. Fill in form:
   ```
   Company Name: Tech Gadgets Inc
   Email: info@techgadgets.com
   Password: SecurePass123!
   ```
4. Click "Create Company"
5. New company appears in table

#### 3. Add a New Customer
1. Navigate to "Manage Customers"
2. Click "Add Customer"
3. Fill in form:
   ```
   First Name: Alice
   Last Name: Cooper
   Email: alice.cooper@email.com
   Password: SecurePass123!
   ```
4. Click "Create Customer"
5. New customer appears in table

#### 4. Unlock a Locked Account
1. Go to "Manage Companies" or "Manage Customers"
2. Look for accounts with "Locked" status
3. Click "Unlock" button
4. Confirm action
5. Account is immediately unlocked
6. User can now login again

---

## Advanced Features

### Category System
The system uses 4 predefined categories:

| Category ID | Category Name | Used For |
|-------------|---------------|----------|
| 10 | SKIING | Ski resorts, passes, lessons |
| 20 | SKY_DIVING | Skydiving experiences, certifications |
| 30 | FANCY_RESTAURANT | Fine dining, tasting menus |
| 40 | ALL_INCLUSIVE_VACATION | Resorts, cruises, getaways |

### Account Lockout Security
- **Trigger**: 5 consecutive failed login attempts
- **Duration**: 30 minutes automatic lockout
- **Admin Override**: Admins can unlock accounts immediately
- **Scope**: Applies to companies and customers (admin accounts exempt by default)

### Rate Limiting
The backend enforces rate limits to prevent abuse:

**Authentication Endpoints** (`/api/v1/auth/*`):
- **Limit**: 5 requests per minute
- **Applies to**: Login, token refresh

**General API Endpoints**:
- **Limit**: 100 requests per minute
- **Applies to**: All CRUD operations

If you exceed the limit, you'll receive a `429 Too Many Requests` error with a retry-after header.

### JWT Token Authentication
- **Access Token**: Valid for 1 hour
- **Refresh Token**: Valid for 24 hours
- **Auto-Refresh**: Frontend automatically refreshes expired tokens
- **Storage**: Tokens stored in browser localStorage
- **Logout**: Clears all tokens from storage

---

## Sample Data Overview

### Companies by Category

**Skiing**:
- Sky Adventures Ltd (2 coupons)
- Mountain Sports Co (2 coupons)

**Sky Diving**:
- Sky Adventures Ltd (1 coupon)
- Extreme Sports Inc (2 coupons)

**Fancy Restaurant**:
- Gourmet Dining Group (4 coupons)

**All Inclusive Vacation**:
- Paradise Resorts International (4 coupons)

### Price Ranges
- **Budget** (< $100): Night Skiing ($89.99), Sunday Brunch ($59.99)
- **Mid-Range** ($100-$500): Weekend Ski Pass ($149.99), Valentine Special ($199.99), Family Ski Package ($499.99)
- **Premium** ($500-$2000): Advanced Ski Training ($699.99), Group Skydive ($899.99), Solo Skydive Cert ($1,499.99), Mountain Lodge ($1,799.99), Tropical Getaway ($1,999.99)
- **Luxury** (> $2000): Caribbean Resort ($2,499.99), Mediterranean Cruise ($3,999.99)

---

## API Documentation

### Swagger UI
Access the interactive API documentation at:
**http://localhost:8080/swagger-ui/index.html**

Features:
- Test all endpoints directly from browser
- View request/response schemas
- See authentication requirements
- Download OpenAPI specification

### Key Endpoints

#### Authentication
```
POST /api/v1/auth/login
POST /api/v1/auth/refresh
```

#### Admin Operations
```
GET    /api/v1/admin/companies
POST   /api/v1/admin/companies
PUT    /api/v1/admin/companies/{id}
DELETE /api/v1/admin/companies/{id}
POST   /api/v1/admin/companies/{id}/unlock

GET    /api/v1/admin/customers
POST   /api/v1/admin/customers
PUT    /api/v1/admin/customers/{id}
DELETE /api/v1/admin/customers/{id}
POST   /api/v1/admin/customers/{id}/unlock
```

#### Company Operations
```
GET    /api/v1/companies/coupons
POST   /api/v1/companies/coupons
PUT    /api/v1/companies/coupons/{id}
DELETE /api/v1/companies/coupons/{id}
GET    /api/v1/companies/coupons/category/{categoryId}
GET    /api/v1/companies/coupons/price/{maxPrice}
```

#### Customer Operations
```
POST   /api/v1/customers/coupons/purchase/{couponId}
GET    /api/v1/customers/coupons
GET    /api/v1/customers/coupons/category/{categoryId}
GET    /api/v1/customers/coupons/price/{maxPrice}
```

#### Public Operations
```
GET    /api/v1/public/coupons
GET    /api/v1/public/coupons/{id}
```

---

## Troubleshooting

### Frontend Issues

**Problem: "Connection Refused" at localhost:3000**
- Solution: Ensure frontend container is running
  ```bash
  docker ps | grep frontend
  docker compose up -d frontend
  ```

**Problem: API calls returning 401 Unauthorized**
- Solution: Token expired or invalid
  - Logout and login again
  - Clear browser localStorage
  - Check backend is running

**Problem: Blank page or loading forever**
- Solution: Check browser console for errors
  - Open DevTools (F12)
  - Look for CORS errors or network failures
  - Verify backend is healthy: http://localhost:8080/actuator/health

### Backend Issues

**Problem: Backend returning 500 errors**
- Solution: Check backend logs
  ```bash
  docker logs coupon-system-app --tail 50
  ```

**Problem: Database connection errors**
- Solution: Check MySQL container
  ```bash
  docker ps | grep mysql
  docker logs coupon-system-mysql --tail 30
  ```

### Authentication Issues

**Problem: Login fails with valid credentials**
- Solution: Account may be locked
  - Wait 30 minutes for auto-unlock, OR
  - Login as admin and unlock the account

**Problem: "Rate limit exceeded" error**
- Solution: Too many requests
  - Wait 1 minute and try again
  - Reduce request frequency

### Data Issues

**Problem: No coupons visible**
- Solution: Re-populate database
  ```bash
  cat populate-sample-data.sql | docker exec -i coupon-system-mysql mysql -u root -pYOUR_ROOT_PASSWORD couponsystem
  ```

**Problem: Coupon purchase fails**
- Solution: Check:
  - Coupon is in stock (amount > 0)
  - You haven't already purchased this coupon
  - Coupon dates are valid (within start/end range)

---

## Tips & Best Practices

### For Customers
1. **Filter First**: Use category and price filters to narrow down options
2. **Check Stock**: Verify amount > 0 before attempting purchase
3. **Check Dates**: Ensure coupon is valid for your planned usage dates
4. **View History**: Regularly check "My Purchases" to track your coupons

### For Companies
1. **Clear Titles**: Keep titles concise (max 48 characters)
2. **Descriptive Text**: Make descriptions informative but brief
3. **Competitive Pricing**: Research similar coupons before setting prices
4. **Stock Management**: Monitor stock levels and create new coupons when needed
5. **Date Ranges**: Set realistic validity periods

### For Administrators
1. **Regular Monitoring**: Check dashboard statistics regularly
2. **Account Management**: Unlock accounts promptly to maintain user satisfaction
3. **Data Integrity**: Verify company and customer data is accurate
4. **Security**: Monitor for unusual login patterns or rate limit violations

---

## Database Management

### Viewing Current Data
```bash
# Connect to MySQL
docker exec -it coupon-system-mysql mysql -u root -p

# View companies
USE couponsystem;
SELECT id, NAME, EMAIL FROM companies;

# View coupons
SELECT id, COMPANY_ID, TITLE, PRICE, AMOUNT FROM coupons;

# View customers
SELECT id, FIRST_NAME, LAST_NAME, EMAIL FROM customers;

# View purchases
SELECT c.FIRST_NAME, c.LAST_NAME, co.TITLE
FROM customers c
JOIN customers_vs_coupons cvc ON c.id = cvc.CUSTOMER_ID
JOIN coupons co ON cvc.COUPON_ID = co.id;
```

### Resetting Sample Data
```bash
# Run the population script again
cat populate-sample-data.sql | docker exec -i coupon-system-mysql mysql -u root -pyour_secure_root_password_min_16_chars couponsystem
```

---

## Support & Resources

### Health Monitoring
- **Application Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:9090/metrics
- **Container Status**: `docker ps`
- **Container Logs**: `docker logs <container-name>`

### Useful Commands
```bash
# View all containers
docker compose ps

# Restart all services
docker compose restart

# Stop all services
docker compose down

# Start all services
docker compose up -d

# View logs
docker compose logs -f

# Rebuild and restart
docker compose up -d --build
```

---

## Security Notes

### Development vs Production

**Current Setup (Development)**:
- Simple passwords for ease of testing
- MySQL port exposed on localhost
- CORS allows localhost origins
- Detailed error messages

**For Production**:
1. Change all default passwords (16+ characters minimum)
2. Generate unique JWT secret (32+ characters)
3. Remove MySQL port exposure from docker-compose
4. Update CORS to only allow your frontend domain
5. Use HTTPS/TLS
6. Enable stricter rate limits
7. Use pre-hashed BCrypt passwords in .env
8. Set logging level to INFO (not DEBUG)
9. Regular security scans with OWASP dependency check
10. Implement database backups

---

## Next Steps

1. **Explore the UI**: Login with different roles and test features
2. **Test Workflows**: Complete full customer purchase journey
3. **Customize Data**: Add your own companies, customers, and coupons
4. **Review API**: Explore Swagger documentation
5. **Monitor Health**: Check actuator endpoints and metrics
6. **Production Prep**: Review security checklist before deployment

---

**Enjoy using the Coupon System!** 🎟️

For issues or questions, check the logs and troubleshooting section above.
