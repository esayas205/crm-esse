-- Migration to add auditing columns to all CRM tables
ALTER TABLE accounts ADD COLUMN created_by VARCHAR(200);
ALTER TABLE accounts ADD COLUMN updated_by VARCHAR(200);

ALTER TABLE contacts ADD COLUMN created_by VARCHAR(200);
ALTER TABLE contacts ADD COLUMN updated_by VARCHAR(200);

ALTER TABLE opportunities ADD COLUMN created_by VARCHAR(200);
ALTER TABLE opportunities ADD COLUMN updated_by VARCHAR(200);

ALTER TABLE leads ADD COLUMN created_by VARCHAR(200);
ALTER TABLE leads ADD COLUMN updated_by VARCHAR(200);

ALTER TABLE activities ADD COLUMN created_by VARCHAR(200);
ALTER TABLE activities ADD COLUMN updated_by VARCHAR(200);

-- Ensure created_at and updated_at are NOT NULL as required by AuditedEntity
-- Note: MySQL might need DEFAULT CURRENT_TIMESTAMP for NOT NULL columns during addition if data exists
ALTER TABLE accounts MODIFY COLUMN created_at DATETIME NOT NULL;
ALTER TABLE accounts MODIFY COLUMN updated_at DATETIME NOT NULL;

ALTER TABLE contacts MODIFY COLUMN created_at DATETIME NOT NULL;
ALTER TABLE contacts MODIFY COLUMN updated_at DATETIME NOT NULL;

ALTER TABLE opportunities MODIFY COLUMN created_at DATETIME NOT NULL;
ALTER TABLE opportunities MODIFY COLUMN updated_at DATETIME NOT NULL;

ALTER TABLE leads MODIFY COLUMN created_at DATETIME NOT NULL;
ALTER TABLE leads MODIFY COLUMN updated_at DATETIME NOT NULL;

ALTER TABLE activities MODIFY COLUMN created_at DATETIME NOT NULL;
ALTER TABLE activities MODIFY COLUMN updated_at DATETIME NOT NULL;
