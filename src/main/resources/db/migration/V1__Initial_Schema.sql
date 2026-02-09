-- Initial Schema: Consolidated Migration
-- Contains all tables: leads, accounts, contacts, account_contacts, opportunities, activities

CREATE TABLE leads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source VARCHAR(50) NOT NULL,
    company VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    owner_user VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE INDEX idx_leads_email ON leads(email);
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_owner_user ON leads(owner_user);

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

CREATE TABLE contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    job_title VARCHAR(100),
    is_primary_contact BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE INDEX idx_contacts_email ON contacts(email);

CREATE TABLE account_contacts (
    account_id BIGINT NOT NULL,
    contact_id BIGINT NOT NULL,
    PRIMARY KEY (account_id, contact_id),
    CONSTRAINT fk_account_contacts_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_account_contacts_contact FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE
);

CREATE TABLE opportunities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    stage VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    close_date DATE,
    probability INT,
    account_id BIGINT NOT NULL,
    primary_lead_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_opportunities_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_opportunities_lead FOREIGN KEY (primary_lead_id) REFERENCES leads(id)
);

CREATE INDEX idx_opportunities_stage ON opportunities(stage);
CREATE INDEX idx_opportunities_account_id ON opportunities(account_id);
CREATE INDEX idx_opportunities_close_date ON opportunities(close_date);

CREATE TABLE activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    description TEXT,
    due_at DATETIME,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    outcome VARCHAR(255),
    lead_id BIGINT,
    opportunity_id BIGINT,
    account_id BIGINT,
    contact_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_activities_lead FOREIGN KEY (lead_id) REFERENCES leads(id),
    CONSTRAINT fk_activities_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities(id),
    CONSTRAINT fk_activities_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_activities_contact FOREIGN KEY (contact_id) REFERENCES contacts(id),
    CONSTRAINT check_only_one_parent CHECK (
        (lead_id IS NOT NULL AND opportunity_id IS NULL AND account_id IS NULL AND contact_id IS NULL) OR
        (lead_id IS NULL AND opportunity_id IS NOT NULL AND account_id IS NULL AND contact_id IS NULL) OR
        (lead_id IS NULL AND opportunity_id IS NULL AND account_id IS NOT NULL AND contact_id IS NULL) OR
        (lead_id IS NULL AND opportunity_id IS NULL AND account_id IS NULL AND contact_id IS NOT NULL)
    )
);

CREATE INDEX idx_activities_lead_id ON activities(lead_id);
CREATE INDEX idx_activities_opportunity_id ON activities(opportunity_id);
CREATE INDEX idx_activities_account_id ON activities(account_id);
CREATE INDEX idx_activities_contact_id ON activities(contact_id);
CREATE INDEX idx_activities_completed ON activities(completed);
CREATE INDEX idx_activities_type ON activities(type);
CREATE INDEX idx_activities_due_at ON activities(due_at);
