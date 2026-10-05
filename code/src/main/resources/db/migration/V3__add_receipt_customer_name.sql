-- V3: receipts keep the customer name at the time the receipt is issued
ALTER TABLE receipts ADD COLUMN customer_name VARCHAR(255);