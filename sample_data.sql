-- =======================================================
-- Sample Data for Inventory Management System
-- =======================================================

USE inventory_management;

-- Clear previous test data (safely in dependency order)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sales;
TRUNCATE TABLE products;
TRUNCATE TABLE customers;
TRUNCATE TABLE suppliers;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Insert Suppliers
INSERT INTO suppliers (supplier_id, supplier_name, email, phone, address) VALUES
(1, 'Apex Tech Distribution', 'contact@apextech.com', '+1-555-0199', '100 Silicon Blvd, San Jose, CA'),
(2, 'Global Office Supplies', 'sales@globaloffice.com', '+1-555-0142', '45 Industrial Pkwy, Chicago, IL'),
(3, 'Prime Logistics & Gadgets', 'support@primegadgets.com', '+1-555-0188', '78 Warehouse Rd, Dallas, TX'),
(4, 'NextGen Component Corp', 'info@nextgencomponents.com', '+1-555-0177', '22 Innovation Way, Austin, TX');

-- 2. Insert Customers
INSERT INTO customers (customer_id, customer_name, email, phone, address) VALUES
(1, 'Johnathan Miller', 'john.miller@example.com', '+1-555-1122', '12 Elm Street, Boston, MA'),
(2, 'Sarah Jenkins', 'sarah.j@example.com', '+1-555-2233', '88 Maple Ave, Seattle, WA'),
(3, 'David Kim', 'david.kim@example.com', '+1-555-3344', '304 Birch Lane, Denver, CO'),
(4, 'Emily Watson', 'emily.w@example.com', '+1-555-4455', '512 Oak Blvd, Atlanta, GA');

-- 3. Insert Products (with varied categories, and low stock <= 5 items for demo)
INSERT INTO products (product_id, product_name, category, price, quantity, supplier_id, description) VALUES
(1, 'Dell UltraSharp 27" 4K Monitor', 'Electronics', 399.99, 15, 1, '4K UHD USB-C Hub monitor with high color accuracy'),
(2, 'Logitech MX Master 3S Wireless Mouse', 'Electronics', 99.99, 25, 1, 'Ergonomic performance mouse with quiet clicks and 8K DPI sensor'),
(3, 'Keychron Q1 Pro Mechanical Keyboard', 'Electronics', 189.50, 4, 3, 'Custom wireless mechanical keyboard (Low Stock Alert)'),
(4, 'Ergonomic Mesh Office Chair', 'Furniture', 249.00, 8, 2, 'High-back ergonomic task chair with lumbar support'),
(5, 'Adjustable Standing Desk (55x28)', 'Furniture', 329.00, 3, 2, 'Dual motor electric height adjustable workstation (Low Stock Alert)'),
(6, 'USB-C Multi-Port Hub (8-in-1)', 'Accessories', 45.00, 40, 3, 'Aluminum Type-C adapter with HDMI 4K, SD reader, and PD charging'),
(7, 'Anker 65W GaN Fast Charger', 'Accessories', 35.99, 18, 4, 'Compact 3-port wall charger for laptops and smartphones'),
(8, 'Seagate 2TB Portable External SSD', 'Storage', 159.99, 2, 4, 'Ultra-fast NVMe external solid state drive (Critical Low Stock)');

-- 4. Insert Initial Sales
INSERT INTO sales (sale_id, product_id, customer_id, quantity, total_amount, sale_date) VALUES
(1, 1, 1, 1, 399.99, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 2, 2, 2, 199.98, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 6, 3, 3, 135.00, NOW()),
(4, 7, 4, 1, 35.99, NOW());
