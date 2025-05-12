-- Users table for authentication
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username TEXT NOT NULL UNIQUE,
  password TEXT NOT NULL,
  role TEXT NOT NULL CHECK (role IN ('admin', 'patient')),
  email TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Sample data
INSERT INTO users (username, password, role, email) VALUES
('admin', 'admin', 'admin', 'admin@example.com'),
('patient1', 'patient1', 'patient', 'patient1@example.com'),
('patient2', 'patient2', 'patient', 'patient2@example.com');

-- Appointments table (existing)
CREATE TABLE IF NOT EXISTS appointments (
  id SERIAL PRIMARY KEY,
  patient_name TEXT NOT NULL,
  appointment_date TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  user_id INTEGER REFERENCES users(id)
);

-- Add user_id to existing appointments if table already exists
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS user_id INTEGER REFERENCES users(id); 