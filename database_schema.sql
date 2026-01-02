-- 0. Configure Connection User (As per Project Requirement)
-- Ensures 'myuser@localhost' exists with password '1234'
CREATE USER IF NOT EXISTS 'myuser'@'localhost' IDENTIFIED BY '1234';

-- 1. Create Database
DROP DATABASE IF EXISTS greengrocer_db;
CREATE DATABASE greengrocer_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant privileges to the user for this specific DB
GRANT ALL PRIVILEGES ON greengrocer_db.* TO 'myuser'@'localhost';
FLUSH PRIVILEGES;

USE greengrocer_db;

-- 2. Create UserInfo Table
CREATE TABLE UserInfo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER', 'CARRIER', 'OWNER') NOT NULL, 
    address TEXT,
    phone_number VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Create ProductInfo Table
CREATE TABLE ProductInfo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category ENUM('FRUIT', 'VEGETABLE') NOT NULL,
    type VARCHAR(50), 
    price DECIMAL(10, 2) NOT NULL,
    stock DECIMAL(10, 2) NOT NULL,
    threshold DECIMAL(10, 2) NOT NULL DEFAULT 5.00, 
    imagelocation BLOB, 
    unit VARCHAR(10) DEFAULT 'kg'
);

-- 7. Create Coupons Table
CREATE TABLE Coupons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    discount_amount DECIMAL(10, 2) NOT NULL,
    expiry_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);



-- 4. Create OrderInfo Table
CREATE TABLE OrderInfo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,  
    carrier_id INT,            
    ordertime TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
    deliverytime TIMESTAMP NULL, 
    
    -- Status Enum Updated:
    status ENUM('AVAILABLE', 'SELECTED', 'COMPLETED', 'CANCELLED') DEFAULT 'AVAILABLE',
    
    totalcost DECIMAL(10, 2) NOT NULL, 
    
    -- Customer's requested delivery time (NEW FIELD)
    requested_delivery_date TIMESTAMP NULL,

    -- Track used coupon
    used_coupon_id INT DEFAULT NULL,
    
    -- Invoice stored as CLOB (LONGTEXT)
    invoice LONGTEXT, 
    
    FOREIGN KEY (customer_id) REFERENCES UserInfo(id),
    FOREIGN KEY (carrier_id) REFERENCES UserInfo(id),
    FOREIGN KEY (used_coupon_id) REFERENCES Coupons(id)
);

-- 5. Create OrderItems Table 
CREATE TABLE OrderItems (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    price_at_purchase DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES OrderInfo(id),
    FOREIGN KEY (product_id) REFERENCES ProductInfo(id),
    UNIQUE KEY unique_order_item (order_id, product_id)
);

-- 6. Create Messages Table
CREATE TABLE Messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL, 
    content TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id) REFERENCES UserInfo(id),
    FOREIGN KEY (receiver_id) REFERENCES UserInfo(id)
);


-- 8. Create CarrierRatings Table 
CREATE TABLE CarrierRatings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    customer_id INT NOT NULL,
    carrier_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES OrderInfo(id),
    FOREIGN KEY (customer_id) REFERENCES UserInfo(id),
    FOREIGN KEY (carrier_id) REFERENCES UserInfo(id),
    UNIQUE KEY unique_order_rating (order_id)
);

-- 9. Create ProductRatings Table
CREATE TABLE ProductRatings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    customer_id INT NOT NULL,
    product_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES OrderInfo(id),
    FOREIGN KEY (customer_id) REFERENCES UserInfo(id),
    FOREIGN KEY (product_id) REFERENCES ProductInfo(id),
    UNIQUE KEY unique_order_product_rating (order_id, product_id)
);

-- ==========================================
--        DUMMY DATA (Min 25 records)
-- ==========================================

INSERT INTO UserInfo (username, password, role, address, phone_number) VALUES
('own', 'own', 'OWNER', 'HQ Address', '555-0000'),       
('carr', 'carr', 'CARRIER', 'Carrier Hub 1', '555-0001'), 
('cust', 'cust', 'CUSTOMER', 'Customer Address 1', '555-1001'), 
('carrier2', '1234', 'CARRIER', 'Carrier Hub 2', '555-0002'),
('alice', '1234', 'CUSTOMER', 'Wonderland St.', '555-1002'),
('bob', '1234', 'CUSTOMER', 'Builder Ave.', '555-1003'),
('charlie', '1234', 'CUSTOMER', 'Chocolate Factory', '555-1004'),
('david', '1234', 'CUSTOMER', 'David St.', '555-1005'),
('eve', '1234', 'CUSTOMER', 'Eve Ln.', '555-1006'),
('frank', '1234', 'CUSTOMER', 'Frank Ct.', '555-1007'),
('grace', '1234', 'CUSTOMER', 'Grace Blvd.', '555-1008'),
('heidi', '1234', 'CUSTOMER', 'Alps Rd.', '555-1009'),
('ivan', '1234', 'CUSTOMER', 'Ivan Sq.', '555-1010'),
('judy', '1234', 'CUSTOMER', 'Court St.', '555-1011'),
('karl', '1234', 'CUSTOMER', 'Capital Blvd.', '555-1012'),
('leo', '1234', 'CUSTOMER', 'Lion St.', '555-1013'),
('mallory', '1234', 'CUSTOMER', 'Malicious Dr.', '555-1014'),
('nia', '1234', 'CUSTOMER', 'Neon Way', '555-1015'),
('oscar', '1234', 'CUSTOMER', 'Oscar Ln.', '555-1016'),
('peggy', '1234', 'CUSTOMER', 'Pegasus Rd.', '555-1017'),
('quentin', '1234', 'CUSTOMER', 'Queen St.', '555-1018'),
('rupert', '1234', 'CUSTOMER', 'Rupert Way', '555-1019'),
('sybil', '1234', 'CUSTOMER', 'Sybil Ct.', '555-1020'),
('ted', '1234', 'CUSTOMER', 'Talks Rd.', '555-1021'),
('ursula', '1234', 'CUSTOMER', 'Unity St.', '555-1022'),
('victor', '1234', 'CUSTOMER', 'Victory Ln.', '555-1023');

INSERT INTO ProductInfo (name, category, type, price, stock, threshold) VALUES
('Tomato', 'VEGETABLE', 'Vine', 15.00, 100.00, 10.00),
('Potato', 'VEGETABLE', 'Russet', 10.00, 200.00, 20.00),
('Onion', 'VEGETABLE', 'Yellow', 8.00, 150.00, 15.00),
('Cucumber', 'VEGETABLE', 'English', 12.00, 80.00, 5.00),
('Pepper', 'VEGETABLE', 'Bell', 20.00, 60.00, 5.00),
('Carrot', 'VEGETABLE', 'Orange', 9.00, 120.00, 10.00),
('Lettuce', 'VEGETABLE', 'Romaine', 15.00, 50.00, 5.00),
('Spinach', 'VEGETABLE', 'Baby', 25.00, 40.00, 5.00),
('Broccoli', 'VEGETABLE', 'Crowns', 30.00, 45.00, 5.00),
('Cauliflower', 'VEGETABLE', 'White', 28.00, 40.00, 5.00),
('Eggplant', 'VEGETABLE', 'Italian', 18.00, 70.00, 8.00),
('Zucchini', 'VEGETABLE', 'Green', 16.00, 65.00, 8.00),
('Garlic', 'VEGETABLE', 'White', 40.00, 30.00, 5.00), 
('Apple', 'FRUIT', 'Gala', 25.0, 100.0, 10.0),
('Banana', 'FRUIT', 'Cavendish', 35.0, 120.0, 15.0),
('Orange', 'FRUIT', 'Navel', 20.0, 90.0, 10.0),
('Strawberry', 'FRUIT', 'Red', 50.0, 30.0, 5.0),
('Grapes', 'FRUIT', 'Green', 40.0, 50.0, 5.0),
('Watermelon', 'FRUIT', 'Seedless', 10.0, 200.0, 20.0),
('Melon', 'FRUIT', 'Cantaloupe', 12.0, 150.0, 15.0),
('Peach', 'FRUIT', 'Yellow', 30.0, 60.0, 5.0),
('Pear', 'FRUIT', 'Bartlett', 28.0, 70.0, 8.0),
('Cherry', 'FRUIT', 'Bing', 60.0, 25.0, 5.0),
('Kiwi', 'FRUIT', 'Fuzzy', 45.0, 40.0, 5.0),
('Pineapple', 'FRUIT', 'Gold', 55.0, 35.0, 5.0),
('Mango', 'FRUIT', 'Tommy', 70.0, 20.0, 3.0);

-- Updated OrderInfo inserts with requested_delivery_date
INSERT INTO OrderInfo (customer_id, carrier_id, status, totalcost, requested_delivery_date) VALUES
(4, 2, 'COMPLETED', 150.00, DATE_ADD(NOW(), INTERVAL 1 DAY)), 
(5, 2, 'COMPLETED', 200.00, DATE_ADD(NOW(), INTERVAL 2 DAY)), 
(6, NULL, 'AVAILABLE', 50.00, DATE_ADD(NOW(), INTERVAL 1 DAY)),
(7, NULL, 'AVAILABLE', 80.00, DATE_ADD(NOW(), INTERVAL 1 DAY)), 
(8, 2, 'SELECTED', 120.00, DATE_ADD(NOW(), INTERVAL 1 DAY)), 
(9, 3, 'COMPLETED', 300.00, DATE_ADD(NOW(), INTERVAL 1 DAY)),
(10, NULL, 'AVAILABLE', 45.00, DATE_ADD(NOW(), INTERVAL 2 DAY)), 
(11, 2, 'COMPLETED', 90.00, DATE_ADD(NOW(), INTERVAL 1 DAY)), 
(12, 3, 'SELECTED', 110.00, DATE_ADD(NOW(), INTERVAL 2 DAY)),
(13, NULL, 'AVAILABLE', 60.00, DATE_ADD(NOW(), INTERVAL 1 DAY)), 
(14, 2, 'COMPLETED', 75.00, DATE_ADD(NOW(), INTERVAL 1 DAY)), 
(15, 3, 'COMPLETED', 180.00, DATE_ADD(NOW(), INTERVAL 1 DAY)),
(16, NULL, 'AVAILABLE', 55.00, DATE_ADD(NOW(), INTERVAL 1 DAY)), 
(17, NULL, 'AVAILABLE', 130.00, DATE_ADD(NOW(), INTERVAL 2 DAY)), 
(18, 2, 'SELECTED', 160.00, DATE_ADD(NOW(), INTERVAL 1 DAY)),
(19, 3, 'COMPLETED', 220.00, DATE_ADD(NOW(), INTERVAL 1 DAY)), 
(20, NULL, 'AVAILABLE', 40.00, DATE_ADD(NOW(), INTERVAL 1 DAY)), 
(21, 2, 'COMPLETED', 95.00, DATE_ADD(NOW(), INTERVAL 3 DAY)),
(22, 3, 'SELECTED', 115.00, DATE_ADD(NOW(), INTERVAL 1 DAY)), 
(23, NULL, 'AVAILABLE', 65.00, DATE_ADD(NOW(), INTERVAL 1 DAY)), 
(24, 2, 'COMPLETED', 85.00, DATE_ADD(NOW(), INTERVAL 1 DAY)),
(25, 3, 'COMPLETED', 190.00, DATE_ADD(NOW(), INTERVAL 1 DAY)), 
(4, NULL, 'AVAILABLE', 70.00, DATE_ADD(NOW(), INTERVAL 1 DAY)), 
(5, NULL, 'AVAILABLE', 140.00, DATE_ADD(NOW(), INTERVAL 1 DAY)),
(6, 2, 'SELECTED', 170.00, DATE_ADD(NOW(), INTERVAL 1 DAY));

INSERT INTO CarrierRatings (order_id, customer_id, carrier_id, rating, comment) VALUES
(1, 4, 2, 5, 'Great!'), (2, 5, 2, 4, 'Good'), (6, 9, 3, 3, 'Average'),
(8, 11, 2, 5, 'Fast'), (11, 14, 2, 4, 'Nice'), (12, 15, 3, 5, 'Perfect');
