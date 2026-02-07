-- V4: Change Account and Contact relationship to Many-to-Many

-- 1. Create join table
CREATE TABLE account_contacts (
    account_id BIGINT NOT NULL,
    contact_id BIGINT NOT NULL,
    PRIMARY KEY (account_id, contact_id),
    CONSTRAINT fk_account_contacts_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_account_contacts_contact FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE
);

-- 2. Migrate existing data
INSERT INTO account_contacts (account_id, contact_id)
SELECT account_id, id FROM contacts WHERE account_id IS NOT NULL;

-- 3. Drop foreign key and column from contacts
ALTER TABLE contacts DROP FOREIGN KEY fk_contacts_account;
ALTER TABLE contacts DROP COLUMN account_id;
