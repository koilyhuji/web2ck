ALTER TABLE expense_categories
ADD COLUMN budget_amount DECIMAL(15, 0) NULL;

ALTER TABLE expenses
ADD COLUMN notes TEXT NULL;