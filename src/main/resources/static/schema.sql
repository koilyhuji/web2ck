
CREATE TABLE IF NOT EXISTS expense_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,   
    name VARCHAR(100) NOT NULL UNIQUE,      -- e.g., "Ăn uống" (Food & Drink), "Tiện ích" (Utilities)
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,   
    description VARCHAR(255) NOT NULL,      
    amount DECIMAL(15, 0) NOT NULL,         -- Max 15 digits, 0 digits after decimal. E.g., 123,456,789,123,456
                                            -- This can store up to hundreds of trillions, plenty for expenses.
                                            -- Some might use BIGINT if decimals are *never* expected,
                                            -- but DECIMAL is safer for monetary values.
    expense_date DATE NOT NULL,
    category_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- MySQL only
    -- For PostgreSQL: updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP (needs trigger or app logic)
    FOREIGN KEY (category_id) REFERENCES expense_categories(id) ON DELETE SET NULL
);

-- Sample Data (with example Vietnamese descriptions and VND amounts)
/*
INSERT INTO expense_categories (name, description) VALUES
('Ăn uống', 'Chi phí cho thực phẩm, đồ uống, ăn ngoài'),
('Tiện ích', 'Hóa đơn điện, nước, internet, điện thoại'),
('Di chuyển', 'Xăng xe, vé tàu xe, taxi/grab'),
('Giải trí', 'Xem phim, ca nhạc, sách báo');

INSERT INTO expenses (description, amount, expense_date, category_id) VALUES
('Bữa trưa văn phòng', 75000, '2023-10-26', (SELECT id from expense_categories WHERE name = 'Ăn uống')),
('Hóa đơn Internet tháng 11', 220000, '2023-10-25', (SELECT id from expense_categories WHERE name = 'Tiện ích')),
('Đổ xăng xe máy', 50000, '2023-10-24', (SELECT id from expense_categories WHERE name = 'Di chuyển')),
('Vé xem phim cuối tuần', 120000, '2023-10-22', (SELECT id from expense_categories WHERE name = 'Giải trí'));
*/