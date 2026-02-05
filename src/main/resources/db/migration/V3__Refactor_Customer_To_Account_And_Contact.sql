-- V3: Refactor Customer to Account and Contact

-- 1. Create accounts table
CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_name VARCHAR(255) NOT NULL,
    industry VARCHAR(100),
    website VARCHAR(255),
    phone VARCHAR(50),
    billing_address TEXT,
    shipping_address TEXT,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- 2. Create contacts table
CREATE TABLE contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    job_title VARCHAR(100),
    is_primary_contact BOOLEAN NOT NULL DEFAULT FALSE,
    account_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_contacts_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE INDEX idx_contacts_email ON contacts(email);
CREATE INDEX idx_contacts_account_id ON contacts(account_id);

-- 3. Migrate existing customer data
-- We'll assume each customer was a "Company" for now, or create an account based on their email domain/name.
-- Since the original 'customers' table didn't have a company name, we'll use a placeholder or their name.
INSERT INTO accounts (account_name, phone, status, created_at, updated_at)
SELECT CONCAT(first_name, ' ', last_name, ' Account'), phone, status, created_at, updated_at
FROM customers;

-- Now migrate contacts using the newly created accounts
INSERT INTO contacts (first_name, last_name, email, phone, is_primary_contact, account_id, created_at, updated_at)
SELECT c.first_name, c.last_name, c.email, c.phone, TRUE, a.id, c.created_at, c.updated_at
FROM customers c
JOIN accounts a ON a.account_name = CONCAT(c.first_name, ' ', c.last_name, ' Account');

-- 4. Update foreign keys in other tables
-- First, add columns for the new relationships
ALTER TABLE opportunities ADD COLUMN account_id BIGINT;
ALTER TABLE activities ADD COLUMN account_id BIGINT;
ALTER TABLE activities ADD COLUMN contact_id BIGINT;

-- Map existing opportunity data to accounts (since it was linked to customers)
UPDATE opportunities o
JOIN customers c ON o.customer_id = c.id
JOIN accounts a ON a.account_name = CONCAT(c.first_name, ' ', c.last_name, ' Account')
SET o.account_id = a.id;

-- Map existing activity data to accounts and contacts
UPDATE activities act
JOIN customers c ON act.customer_id = c.id
JOIN accounts a ON a.account_name = CONCAT(c.first_name, ' ', c.last_name, ' Account')
JOIN contacts con ON con.account_id = a.id
SET act.account_id = a.id, act.contact_id = con.id;

-- 5. Drop old constraints and columns, and add new ones
-- Update the check constraint for activities FIRST to release customer_id
ALTER TABLE activities DROP CONSTRAINT check_only_one_parent;
ALTER TABLE activities ADD CONSTRAINT check_only_one_parent CHECK (
    (lead_id IS NOT NULL AND opportunity_id IS NULL AND account_id IS NULL AND contact_id IS NULL) OR
    (lead_id IS NULL AND opportunity_id IS NOT NULL AND account_id IS NULL AND contact_id IS NULL) OR
    (lead_id IS NULL AND opportunity_id IS NULL AND account_id IS NOT NULL AND contact_id IS NULL) OR
    (lead_id IS NULL AND opportunity_id IS NULL AND account_id IS NULL AND contact_id IS NOT NULL)
);

ALTER TABLE opportunities DROP FOREIGN KEY fk_opportunities_customer;
ALTER TABLE opportunities DROP COLUMN customer_id;
ALTER TABLE opportunities MODIFY COLUMN account_id BIGINT NOT NULL;
ALTER TABLE opportunities ADD CONSTRAINT fk_opportunities_account FOREIGN KEY (account_id) REFERENCES accounts(id);

ALTER TABLE activities DROP FOREIGN KEY fk_activities_customer;
ALTER TABLE activities DROP COLUMN customer_id;
ALTER TABLE activities ADD CONSTRAINT fk_activities_account FOREIGN KEY (account_id) REFERENCES accounts(id);
ALTER TABLE activities ADD CONSTRAINT fk_activities_contact FOREIGN KEY (contact_id) REFERENCES contacts(id);

-- Update the check constraint for activities
ALTER TABLE activities DROP CONSTRAINT check_only_one_parent;
ALTER TABLE activities ADD CONSTRAINT check_only_one_parent CHECK (
    (lead_id IS NOT NULL AND opportunity_id IS NULL AND account_id IS NULL AND contact_id IS NULL) OR
    (lead_id IS NULL AND opportunity_id IS NOT NULL AND account_id IS NULL AND contact_id IS NULL) OR
    (lead_id IS NULL AND opportunity_id IS NULL AND account_id IS NOT NULL AND contact_id IS NULL) OR
    (lead_id IS NULL AND opportunity_id IS NULL AND account_id IS NULL AND contact_id IS NOT NULL)
);

-- 6. Finally drop the customers table
DROP TABLE customers;
