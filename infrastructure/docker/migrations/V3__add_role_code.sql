-- V3: Add code column to roles table
ALTER TABLE roles ADD COLUMN IF NOT EXISTS code VARCHAR(50) UNIQUE;

-- Update existing roles with code based on name
UPDATE roles SET code = UPPER(REPLACE(name, ' ', '_')) WHERE code IS NULL;

-- Make code NOT NULL after populating
ALTER TABLE roles ALTER COLUMN code SET NOT NULL;

-- Create index on code
CREATE INDEX IF NOT EXISTS idx_roles_code ON roles(code);
