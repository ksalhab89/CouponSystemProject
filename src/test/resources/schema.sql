-- H2 Test Schema for CouponSystem
-- Updated to match PostgreSQL production schema

-- Categories table (must be created first for foreign key)
CREATE TABLE IF NOT EXISTS categories (
    id INT PRIMARY KEY,
    name VARCHAR(48)
);

-- Insert category data (use MERGE to avoid duplicate key errors on reruns)
MERGE INTO categories (id, name) VALUES (10, 'SKYING');
MERGE INTO categories (id, name) VALUES (20, 'SKY_DIVING');
MERGE INTO categories (id, name) VALUES (30, 'FANCY_RESTAURANT');
MERGE INTO categories (id, name) VALUES (40, 'ALL_INCLUSIVE_VACATION');

CREATE TABLE IF NOT EXISTS companies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(48),
    password VARCHAR(60),
    failed_login_attempts INT DEFAULT 0 NOT NULL,
    account_locked BOOLEAN DEFAULT FALSE NOT NULL,
    locked_until TIMESTAMP NULL,
    last_failed_login TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(48),
    last_name VARCHAR(48),
    email VARCHAR(48),
    password VARCHAR(60),
    failed_login_attempts INT DEFAULT 0 NOT NULL,
    account_locked BOOLEAN DEFAULT FALSE NOT NULL,
    locked_until TIMESTAMP NULL,
    last_failed_login TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS coupons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    company_id INT,
    category_id INT,
    title VARCHAR(48),
    description VARCHAR(255),
    start_date DATE,
    end_date DATE,
    amount INT,
    price DOUBLE,
    image VARCHAR(255),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS customers_vs_coupons (
    customer_id INT NOT NULL,
    coupon_id INT NOT NULL,
    PRIMARY KEY (customer_id, coupon_id),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE
);
