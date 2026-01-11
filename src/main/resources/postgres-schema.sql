-- PostgreSQL Schema for Coupon System
-- Converted from MySQL 8.4
-- Date: 2026-01-10

-- Note: PostgreSQL databases are created by docker-entrypoint-initdb.d
-- so we just connect to the database

-- Table: categories
CREATE TABLE IF NOT EXISTS categories (
  id INT PRIMARY KEY,
  name VARCHAR(48)
);

INSERT INTO categories (id, name) VALUES
(10, 'SKYING'),
(20, 'SKY_DIVING'),
(30, 'FANCY_RESTAURANT'),
(40, 'ALL_INCLUSIVE_VACATION')
ON CONFLICT (id) DO NOTHING;

-- Table: companies
CREATE TABLE IF NOT EXISTS companies (
  id SERIAL PRIMARY KEY,
  name VARCHAR(48),
  email VARCHAR(48),
  password VARCHAR(60),
  failed_login_attempts INT DEFAULT 0 NOT NULL,
  account_locked BOOLEAN DEFAULT FALSE NOT NULL,
  locked_until TIMESTAMP,
  last_failed_login TIMESTAMP
);

-- Table: customers
CREATE TABLE IF NOT EXISTS customers (
  id SERIAL PRIMARY KEY,
  first_name VARCHAR(48),
  last_name VARCHAR(48),
  email VARCHAR(48),
  password VARCHAR(60),
  failed_login_attempts INT DEFAULT 0 NOT NULL,
  account_locked BOOLEAN DEFAULT FALSE NOT NULL,
  locked_until TIMESTAMP,
  last_failed_login TIMESTAMP
);

-- Table: coupons
CREATE TABLE IF NOT EXISTS coupons (
  id SERIAL PRIMARY KEY,
  company_id INT,
  category_id INT,
  title VARCHAR(48),
  description VARCHAR(255),
  start_date DATE,
  end_date DATE,
  amount INT,
  price NUMERIC(10, 2),
  image VARCHAR(255),
  CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Table: customers_vs_coupons (junction table)
CREATE TABLE IF NOT EXISTS customers_vs_coupons (
  customer_id INT NOT NULL,
  coupon_id INT NOT NULL,
  PRIMARY KEY (customer_id, coupon_id),
  CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
  CONSTRAINT fk_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_companies_name ON companies(name);
CREATE INDEX IF NOT EXISTS idx_companies_account_locked ON companies(account_locked, locked_until);
CREATE INDEX IF NOT EXISTS idx_customers_account_locked ON customers(account_locked, locked_until);
CREATE INDEX IF NOT EXISTS idx_coupons_end_date ON coupons(end_date);
CREATE INDEX IF NOT EXISTS idx_coupons_company_category ON coupons(company_id, category_id);
CREATE INDEX IF NOT EXISTS idx_coupons_company_price ON coupons(company_id, price);
CREATE INDEX IF NOT EXISTS idx_coupons_title_company ON coupons(company_id, title);
CREATE INDEX IF NOT EXISTS idx_customers_vs_coupons_coupon ON customers_vs_coupons(coupon_id);
