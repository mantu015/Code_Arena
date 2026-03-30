-- Add role column if it doesn't exist yet (for existing DBs)
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'USER';

-- Upsert admin user
MERGE INTO users (username, password, points, role) KEY(username) VALUES ('admin', 'admin123', 0, 'ADMIN');

-- Ensure existing rows have a role value
UPDATE users SET role = 'USER' WHERE role IS NULL;

-- Add claimed_user_ids column to daily_challenges if not exists
ALTER TABLE daily_challenges ADD COLUMN IF NOT EXISTS claimed_user_ids VARCHAR(2000) DEFAULT '';
