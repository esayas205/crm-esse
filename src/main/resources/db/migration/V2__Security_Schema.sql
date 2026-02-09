-- Security Schema: RBAC Tables

CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Seed Data

-- Permissions
INSERT INTO permissions (name) VALUES ('ACCOUNT_READ'), ('ACCOUNT_WRITE'), ('CONTACT_READ'), ('CONTACT_WRITE'), ('DEAL_READ'), ('DEAL_WRITE'), ('ACTIVITY_READ'), ('ACTIVITY_WRITE'), ('LEAD_READ'), ('LEAD_WRITE'), ('USER_MANAGE');

-- Roles
INSERT INTO roles (name) VALUES ('ADMIN'), ('SALES'), ('SUPPORT');

-- Role-Permission Mapping
-- ADMIN has all permissions
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ADMIN';

-- SALES has specific permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'SALES' AND p.name IN ('ACCOUNT_READ', 'ACCOUNT_WRITE', 'CONTACT_READ', 'CONTACT_WRITE', 'DEAL_READ', 'DEAL_WRITE', 'LEAD_READ', 'LEAD_WRITE', 'ACTIVITY_READ', 'ACTIVITY_WRITE');

-- SUPPORT has specific permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'SUPPORT' AND p.name IN ('ACCOUNT_READ', 'CONTACT_READ', 'DEAL_READ', 'LEAD_READ', 'ACTIVITY_READ', 'ACTIVITY_WRITE');

-- One Admin User (Password: Admin@123)
-- BCrypt hashed password for 'Admin@123'
INSERT INTO users (username, password, email) VALUES ('admin', '$2a$10$8.VAgL9xVq6JNoYf9qg.M.f9JgS3S4U8S6S4U8S6S4U8S6S4U8S6S', 'admin@example.com');

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.name = 'ADMIN';
