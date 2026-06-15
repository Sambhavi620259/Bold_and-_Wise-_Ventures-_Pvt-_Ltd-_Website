-- V2__add_admin_audit_fields.sql
-- Adds audit and session fields required for admin management in a safe idempotent way

-- Note: Uses MySQL/MariaDB "IF NOT EXISTS" for adding columns and indexes. Ensure your DB version supports this.

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(150) NULL,
  ADD COLUMN IF NOT EXISTS updated_by VARCHAR(150) NULL,
  ADD COLUMN IF NOT EXISTS last_login_at DATETIME NULL,
  ADD COLUMN IF NOT EXISTS is_active TINYINT(1) NOT NULL DEFAULT 1,
  ADD COLUMN IF NOT EXISTS token_version INT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN IF NOT EXISTS updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Ensure index on admin_role exists (helps admin listing queries)
CREATE INDEX IF NOT EXISTS idx_user_role ON users (admin_role);

-- Add index on email for faster lookups (usually exists, this is idempotent)
CREATE INDEX IF NOT EXISTS idx_user_email ON users (email);

