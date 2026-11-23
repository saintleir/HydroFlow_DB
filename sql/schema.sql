-- HydroFlow Water Refilling Station - database schema (MySQL 5.7 / 8.x)
-- run this whole file ONCE (MySQL Workbench: Database > Connect to Database > File > Open SQL Script > lightning button).

DROP DATABASE IF EXISTS hydroflow_db;
CREATE DATABASE hydroflow_db CHARACTER SET utf8mb4;
USE hydroflow_db;

-- ADMIN ACCOUNT for the LOGIN screen
CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash CHAR(64)     NOT NULL          -- SHA-256 hex
);

-- CUSTOMER LIST (Dashboard)
CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    address     VARCHAR(200) NOT NULL,
    contact_no  VARCHAR(20)  NOT NULL DEFAULT ''
);

-- SETTINGS popup (one row per setting)
CREATE TABLE settings (
    setting_key   VARCHAR(50)  PRIMARY KEY,
    setting_value VARCHAR(50)  NOT NULL
);

-- ORDER LIST
CREATE TABLE orders (
    order_no       INT AUTO_INCREMENT PRIMARY KEY,
    customer_id    INT            NOT NULL,
    order_type     VARCHAR(10)    NOT NULL,        -- Delivery / Walk-in
    water_type     VARCHAR(10)    NOT NULL,        -- Alkaline / Mineral
    container_type VARCHAR(20)    NOT NULL,        -- Round Cylinder / Slim Rectangular
    quantity       INT            NOT NULL,
    empties_in     INT            NOT NULL DEFAULT 0,
    unit_price     DECIMAL(10,2)  NOT NULL,        -- price per gallon when the order was made
    delivery_fee   DECIMAL(10,2)  NOT NULL DEFAULT 0,
    discount       DECIMAL(10,2)  NOT NULL DEFAULT 0,
    deposit        DECIMAL(10,2)  NOT NULL DEFAULT 0,
    amount_due     DECIMAL(10,2)  NOT NULL,        -- price x qty + delivery fee - discount
    order_status   VARCHAR(10)    NOT NULL DEFAULT 'Pending',   -- Pending / Delivered / Cancelled
    payment_status VARCHAR(10)    NOT NULL DEFAULT 'Unpaid',    -- Unpaid / Paid
    amount_paid    DECIMAL(10,2)  NOT NULL DEFAULT 0,
    change_amount  DECIMAL(10,2)  NOT NULL DEFAULT 0,
    order_datetime DATETIME       NOT NULL,
    remarks        VARCHAR(255)   NOT NULL DEFAULT '',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- EMPTIES LEGER: one row per DELIVERED order
CREATE TABLE empties_ledger (
    order_no       INT PRIMARY KEY,
    ledger_date    DATE           NOT NULL,
    customer_id    INT            NOT NULL,
    water_type     VARCHAR(10)    NOT NULL,
    container_type VARCHAR(20)    NOT NULL,
    qty_out        INT            NOT NULL,
    empties_in     INT            NOT NULL DEFAULT 0,
    deposit        DECIMAL(10,2)  NOT NULL DEFAULT 0,
    FOREIGN KEY (order_no)    REFERENCES orders(order_no),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- ---------------------------------------------------------------
-- STARTING DATA
-- ---------------------------------------------------------------

-- LOGIN: admin / admin123
INSERT INTO users (username, password_hash)
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9');

INSERT INTO settings (setting_key, setting_value) VALUES
    ('delivery_fee',        '20.00'),
    ('deposit_per_container','100.00'),
    ('price_alkaline_round','40.00'),
    ('price_alkaline_slim', '40.00'),
    ('price_mineral_round', '25.00'),
    ('price_mineral_slim',  '25.00'),
    ('total_containers',    '100'),
    ('daily_sales_quota',   '1000.00');

INSERT INTO customers (name, address, contact_no) VALUES
    ('Cyrlle',    '123 uncle dawgs', '09121212121'),
    ('Julyannah', '456 skusta',      '09131313131'),
    ('Orlo',      '789 flow g',      '09141414141'),
    ('Napthalie', '100 hev abi',     '09151515151'),
    ('Pauline',   '200 shanti dope', '09161616161'),
    ('Kristine',  '300 al james',    '09171717171'),
    ('Ciel',      '400 just hush',   '09181818181');
