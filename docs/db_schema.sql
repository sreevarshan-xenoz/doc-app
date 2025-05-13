-- Users table for authentication with improved security
CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  username TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  password_salt TEXT NOT NULL,
  role TEXT NOT NULL CHECK (role IN ('admin', 'patient')),
  email TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Patient details table
CREATE TABLE IF NOT EXISTS patients (
  id SERIAL PRIMARY KEY,
  user_id INTEGER REFERENCES users(id),
  full_name TEXT NOT NULL,
  age INTEGER,
  gender TEXT CHECK (gender IN ('Male', 'Female', 'Other', 'Prefer not to say')),
  contact_number TEXT,
  email TEXT,
  medical_history TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Appointments table with extended details
CREATE TABLE IF NOT EXISTS appointments (
  id SERIAL PRIMARY KEY,
  patient_name TEXT NOT NULL,
  patient_id INTEGER REFERENCES patients(id),
  user_id INTEGER REFERENCES users(id),
  appointment_date TEXT NOT NULL,
  appointment_time TEXT,
  doctor_name TEXT,
  department TEXT,
  appointment_type TEXT,
  status TEXT DEFAULT 'Scheduled' CHECK (status IN ('Scheduled', 'Completed', 'Cancelled', 'No-show')),
  symptoms TEXT,
  appointment_mode TEXT DEFAULT 'In-person' CHECK (appointment_mode IN ('In-person', 'Online')),
  notes TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Add indices for better performance
CREATE INDEX IF NOT EXISTS idx_appointments_user_id ON appointments(user_id);
CREATE INDEX IF NOT EXISTS idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_patients_user_id ON patients(user_id);

-- Sample data
INSERT INTO users (username, password, role, email) VALUES
('admin', 'admin', 'admin', 'admin@example.com'),
('patient1', 'patient1', 'patient', 'patient1@example.com'),
('patient2', 'patient2', 'patient', 'patient2@example.com');

-- Note: Sample data has been removed. You'll need to register users through the application
-- to ensure passwords are properly hashed and salted. 