-- ============================================================
-- Coupon System - Sample Data Population Script (PostgreSQL)
-- ============================================================
-- Password for all users: "password123"
-- BCrypt hash: $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyFUHBla4YnC
--
-- Usage:
--   docker exec -i coupon-system-postgres psql -U appuser -d couponsystem < populate-sample-data-postgres.sql
-- ============================================================

-- Clear existing data (in correct order due to foreign keys)
TRUNCATE TABLE customers_vs_coupons CASCADE;
TRUNCATE TABLE coupons CASCADE;
TRUNCATE TABLE customers RESTART IDENTITY CASCADE;
TRUNCATE TABLE companies RESTART IDENTITY CASCADE;

-- ============================================================
-- COMPANIES
-- ============================================================
-- Password: password123 (BCrypt strength 12)
INSERT INTO companies (name, email, password, failed_login_attempts, account_locked, locked_until, last_failed_login) VALUES
('Sky Adventures Ltd', 'contact@skyadventures.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyFUHBla4YnC', 0, FALSE, NULL, NULL),
('Mountain Sports Co', 'info@mountainsports.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyFUHBla4YnC', 0, FALSE, NULL, NULL),
('Gourmet Dining Group', 'reservations@gourmetdining.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyFUHBla4YnC', 0, FALSE, NULL, NULL),
('Paradise Resorts International', 'bookings@paradiseresorts.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyFUHBla4YnC', 0, FALSE, NULL, NULL),
('Extreme Sports Inc', 'extreme@sports.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyFUHBla4YnC', 0, FALSE, NULL, NULL);

-- ============================================================
-- CUSTOMERS
-- ============================================================
-- Password: password123 (BCrypt strength 12)
INSERT INTO customers (first_name, last_name, email, password, failed_login_attempts, account_locked, locked_until, last_failed_login) VALUES
('John', 'Smith', 'john.smith@email.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyFUHBla4YnC', 0, FALSE, NULL, NULL),
('Sarah', 'Johnson', 'sarah.j@email.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyFUHBla4YnC', 0, FALSE, NULL, NULL),
('Michael', 'Brown', 'mbrown@email.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyFUHBla4YnC', 0, FALSE, NULL, NULL),
('Emily', 'Davis', 'emily.davis@email.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyFUHBla4YnC', 0, FALSE, NULL, NULL),
('David', 'Wilson', 'd.wilson@email.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyFUHBla4YnC', 0, FALSE, NULL, NULL);

-- ============================================================
-- COUPONS
-- ============================================================
-- Category IDs: 10=SKYING, 20=SKY_DIVING, 30=FANCY_RESTAURANT, 40=ALL_INCLUSIVE_VACATION

-- Sky Adventures Ltd (Company ID: 1) - Skiing & Sky Diving coupons
INSERT INTO coupons (company_id, category_id, title, description, start_date, end_date, amount, price, image) VALUES
(1, 10, 'Weekend Ski Pass', 'Weekend ski slopes + equipment rental', '2026-01-15', '2026-03-31', 50, 149.99, 'https://placehold.co/400x300'),
(1, 10, 'Family Ski Package', '4 passes + equipment + lessons', '2026-01-20', '2026-04-15', 20, 499.99, 'https://placehold.co/400x300'),
(1, 20, 'Tandem Skydive', 'Tandem jump from 10,000 feet', '2026-02-01', '2026-12-31', 30, 299.99, 'https://placehold.co/400x300');

-- Mountain Sports Co (Company ID: 2) - Skiing coupons
INSERT INTO coupons (company_id, category_id, title, description, start_date, end_date, amount, price, image) VALUES
(2, 10, 'Advanced Ski Training', '3-day intensive training camp', '2026-02-10', '2026-03-20', 15, 699.99, 'https://placehold.co/400x300'),
(2, 10, 'Night Skiing', 'Night skiing with lit slopes', '2026-01-25', '2026-02-28', 40, 89.99, 'https://placehold.co/400x300');

-- Extreme Sports Inc (Company ID: 5) - Sky Diving coupons
INSERT INTO coupons (company_id, category_id, title, description, start_date, end_date, amount, price, image) VALUES
(5, 20, 'Solo Skydive Cert', '8 training jumps + certification', '2026-03-01', '2026-09-30', 10, 1499.99, 'https://placehold.co/400x300'),
(5, 20, 'Group Skydive', 'Book 4+ and save! Team building', '2026-02-15', '2026-12-31', 25, 899.99, 'https://placehold.co/400x300');

-- Gourmet Dining Group (Company ID: 3) - Restaurant coupons
INSERT INTO coupons (company_id, category_id, title, description, start_date, end_date, amount, price, image) VALUES
(3, 30, 'Michelin Tasting Menu', '7-course tasting + premium wines', '2026-01-15', '2026-06-30', 30, 249.99, 'https://placehold.co/400x300'),
(3, 30, 'Valentine Special', 'Dinner for 2 + champagne + roses', '2026-02-01', '2026-02-14', 50, 199.99, 'https://placehold.co/400x300'),
(3, 30, 'Sunday Brunch', 'Buffet + bottomless mimosas', '2026-01-20', '2026-12-31', 100, 59.99, 'https://placehold.co/400x300'),
(3, 30, 'Chef Table', '10-course meal by head chef', '2026-02-01', '2026-05-31', 12, 399.99, 'https://placehold.co/400x300');

-- Paradise Resorts International (Company ID: 4) - Vacation coupons
INSERT INTO coupons (company_id, category_id, title, description, start_date, end_date, amount, price, image) VALUES
(4, 40, 'Caribbean Resort 7D', 'All-inclusive ocean view resort', '2026-03-01', '2026-11-30', 20, 2499.99, 'https://placehold.co/400x300'),
(4, 40, 'Mountain Lodge', '5-night lodge + spa + meals', '2026-02-15', '2026-04-30', 15, 1799.99, 'https://placehold.co/400x300'),
(4, 40, 'Mediterranean Cruise', '10-day Greece, Italy, Spain', '2026-05-01', '2026-09-30', 8, 3999.99, 'https://placehold.co/400x300'),
(4, 40, 'Tropical Getaway', '3-night Maldives overwater bungalow', '2026-01-25', '2026-12-15', 25, 1999.99, 'https://placehold.co/400x300');

-- ============================================================
-- CUSTOMER PURCHASES
-- ============================================================
-- John Smith purchases
INSERT INTO customers_vs_coupons (customer_id, coupon_id) VALUES
(1, 1),  -- Weekend Ski Pass
(1, 8);  -- Michelin Tasting Menu

-- Sarah Johnson purchases
INSERT INTO customers_vs_coupons (customer_id, coupon_id) VALUES
(2, 10),  -- Sunday Brunch
(2, 12); -- Caribbean Resort

-- Michael Brown purchases
INSERT INTO customers_vs_coupons (customer_id, coupon_id) VALUES
(3, 3),  -- Tandem Skydive
(3, 6);  -- Solo Skydive Cert

-- Emily Davis purchases
INSERT INTO customers_vs_coupons (customer_id, coupon_id) VALUES
(4, 9),  -- Valentine Special
(4, 15); -- Tropical Getaway

-- David Wilson purchases
INSERT INTO customers_vs_coupons (customer_id, coupon_id) VALUES
(5, 2),  -- Family Ski Package
(5, 13); -- Mountain Lodge

-- Update coupon amounts to reflect purchases
UPDATE coupons SET amount = amount - 1 WHERE id IN (1, 2, 3, 6, 8, 9, 10, 12, 13, 15);

-- ============================================================
-- VERIFICATION QUERIES
-- ============================================================
\echo '=== COMPANIES ==='
SELECT id, name, email FROM companies;

\echo '=== CUSTOMERS ==='
SELECT id, first_name, last_name, email FROM customers;

\echo '=== COUPONS ==='
SELECT id, company_id, title, price, amount,
       CASE category_id
         WHEN 10 THEN 'SKIING'
         WHEN 20 THEN 'SKY_DIVING'
         WHEN 30 THEN 'FANCY_RESTAURANT'
         WHEN 40 THEN 'ALL_INCLUSIVE_VACATION'
       END AS category
FROM coupons ORDER BY company_id, category_id;

\echo '=== CUSTOMER PURCHASES ==='
SELECT c.first_name, c.last_name, co.title, co.price
FROM customers c
JOIN customers_vs_coupons cvc ON c.id = cvc.customer_id
JOIN coupons co ON cvc.coupon_id = co.id
ORDER BY c.last_name, c.first_name;

\echo '=== SUMMARY ==='
SELECT
  (SELECT COUNT(*) FROM companies) AS total_companies,
  (SELECT COUNT(*) FROM customers) AS total_customers,
  (SELECT COUNT(*) FROM coupons) AS total_coupons,
  (SELECT COUNT(*) FROM customers_vs_coupons) AS total_purchases;
