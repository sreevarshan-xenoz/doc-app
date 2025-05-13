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

-- Sample data
INSERT INTO users (username, password, role, email) VALUES
('admin', 'admin', 'admin', 'admin@example.com'),
('patient1', 'patient1', 'patient', 'patient1@example.com'),
('patient2', 'patient2', 'patient', 'patient2@example.com');

-- Enhanced Appointments table with additional fields
CREATE TABLE IF NOT EXISTS appointments (
  id SERIAL PRIMARY KEY,
  appointment_id TEXT NOT NULL UNIQUE,
  -- Patient Details
  patient_name TEXT NOT NULL,
  patient_id TEXT,
  age INTEGER,
  gender TEXT CHECK (gender IN ('Male', 'Female', 'Other', 'Prefer not to say')),
  contact_number TEXT,
  email TEXT,
  -- Appointment Details
  appointment_date TEXT NOT NULL,
  appointment_time TEXT NOT NULL,
  doctor_name TEXT NOT NULL,
  department TEXT NOT NULL,
  appointment_type TEXT CHECK (appointment_type IN ('Consultation', 'Follow-up', 'Emergency', 'Routine Check-up')),
  status TEXT DEFAULT 'Scheduled' CHECK (status IN ('Scheduled', 'Completed', 'Cancelled', 'No-show')),
  -- Additional Fields
  symptoms TEXT,
  medical_history TEXT,
  preferred_mode TEXT CHECK (preferred_mode IN ('In-person', 'Online')),
  consultation_room TEXT,
  payment_status TEXT DEFAULT 'Unpaid' CHECK (payment_status IN ('Paid', 'Unpaid', 'Insurance')),
  notes TEXT,
  -- Metadata
  created_at TIMESTAMPTZ DEFAULT NOW(),
  user_id INTEGER REFERENCES users(id)
);

-- Add user_id to existing appointments if table already exists
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS user_id INTEGER REFERENCES users(id);

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_appointments_user_id ON appointments(user_id);
CREATE INDEX IF NOT EXISTS idx_appointments_patient_name ON appointments(patient_name);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor_name ON appointments(doctor_name);
CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointments(appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointments(status);

-- Note: Sample data has been removed. You'll need to register users through the application
-- to ensure passwords are properly hashed and salted. 