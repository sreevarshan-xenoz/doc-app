-- Rename appointment columns to match our Java code naming convention

-- Rename appointment_date to date if it exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'appointments' AND column_name = 'appointment_date') THEN
        ALTER TABLE appointments RENAME COLUMN appointment_date TO date;
    END IF;
END $$;

-- Rename appointment_time to time if it exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'appointments' AND column_name = 'appointment_time') THEN
        ALTER TABLE appointments RENAME COLUMN appointment_time TO time;
    END IF;
END $$;

-- Rename appointment_id to id if it exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'appointments' AND column_name = 'appointment_id') THEN
        ALTER TABLE appointments RENAME COLUMN appointment_id TO id;
    END IF;
END $$;

-- Add any missing columns

-- Add time column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'appointments' AND column_name = 'time') THEN
        ALTER TABLE appointments ADD COLUMN time TEXT;
    END IF;
END $$;

-- Add doctor_name column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'appointments' AND column_name = 'doctor_name') THEN
        ALTER TABLE appointments ADD COLUMN doctor_name TEXT;
    END IF;
END $$;

-- Add department column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'appointments' AND column_name = 'department') THEN
        ALTER TABLE appointments ADD COLUMN department TEXT;
    END IF;
END $$;

-- Add appointment_type column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'appointments' AND column_name = 'appointment_type') THEN
        ALTER TABLE appointments ADD COLUMN appointment_type TEXT;
    END IF;
END $$;

-- Add status column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'appointments' AND column_name = 'status') THEN
        ALTER TABLE appointments ADD COLUMN status TEXT DEFAULT 'Scheduled';
    END IF;
END $$;
