-- Add role column if it doesn't exist yet (for existing DBs)
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'USER';

-- Upsert admin user
MERGE INTO users (username, password, points, role) KEY(username) VALUES ('admin', 'admin123', 0, 'ADMIN');

-- Ensure existing rows have a role value
UPDATE users SET role = 'USER' WHERE role IS NULL;
