-- Add all missing columns for appointments

-- Add patient_id column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'appointments' AND column_name = 'patient_id') THEN
        ALTER TABLE appointments ADD COLUMN patient_id TEXT;
    END IF;
END $$;

-- Add age column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'appointments' AND column_name = 'age') THEN
        ALTER TABLE appointments ADD COLUMN age INTEGER;
    END IF;
END $$;

-- Add gender column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'appointments' AND column_name = 'gender') THEN
        ALTER TABLE appointments ADD COLUMN gender TEXT;
    END IF;
END $$;

-- Add contact_number column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'appointments' AND column_name = 'contact_number') THEN
        ALTER TABLE appointments ADD COLUMN contact_number TEXT;
    END IF;
END $$;

-- Add email column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'appointments' AND column_name = 'email') THEN
        ALTER TABLE appointments ADD COLUMN email TEXT;
    END IF;
END $$;

-- Add symptoms column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'appointments' AND column_name = 'symptoms') THEN
        ALTER TABLE appointments ADD COLUMN symptoms TEXT;
    END IF;
END $$;

-- Add medical_history column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'appointments' AND column_name = 'medical_history') THEN
        ALTER TABLE appointments ADD COLUMN medical_history TEXT;
    END IF;
END $$;

-- Add preferred_mode column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'appointments' AND column_name = 'preferred_mode') THEN
        ALTER TABLE appointments ADD COLUMN preferred_mode TEXT;
    END IF;
END $$;

-- Add consultation_room column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'appointments' AND column_name = 'consultation_room') THEN
        ALTER TABLE appointments ADD COLUMN consultation_room TEXT;
    END IF;
END $$;

-- Add payment_status column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'appointments' AND column_name = 'payment_status') THEN
        ALTER TABLE appointments ADD COLUMN payment_status TEXT;
    END IF;
END $$;

-- Add notes column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'appointments' AND column_name = 'notes') THEN
        ALTER TABLE appointments ADD COLUMN notes TEXT;
    END IF;
END $$;

-- Add created_at column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'appointments' AND column_name = 'created_at') THEN
        ALTER TABLE appointments ADD COLUMN created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW();
    END IF;
END $$;

-- Create an index on patient_name and date for faster queries
CREATE INDEX IF NOT EXISTS idx_appointments_patient_date ON appointments(patient_name, date);

-- Create an index on user_id for faster queries
CREATE INDEX IF NOT EXISTS idx_appointments_user_id ON appointments(user_id);
