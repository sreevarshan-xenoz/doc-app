-- Update the appointments table with all the fields we need
CREATE TABLE IF NOT EXISTS appointments (
    id TEXT PRIMARY KEY,
    patient_name TEXT NOT NULL,
    patient_id TEXT,
    age INTEGER,
    gender TEXT,
    contact_number TEXT,
    email TEXT,
    date TEXT NOT NULL,
    time TEXT,
    doctor_name TEXT,
    department TEXT,
    appointment_type TEXT,
    symptoms TEXT,
    medical_history TEXT,
    preferred_mode TEXT,
    consultation_room TEXT,
    payment_status TEXT,
    notes TEXT,
    status TEXT DEFAULT 'Scheduled',
    user_id UUID REFERENCES auth.users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- If the table already exists, we need to handle column renames and add any missing columns
DO $$
BEGIN
    -- Rename columns to match new naming convention if they exist
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'appointment_date') THEN
        ALTER TABLE appointments RENAME COLUMN appointment_date TO date;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'appointment_time') THEN
        ALTER TABLE appointments RENAME COLUMN appointment_time TO time;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'appointment_id') THEN
        ALTER TABLE appointments RENAME COLUMN appointment_id TO id;
    END IF;

    -- Fix user_id column type to UUID if it exists and is not already UUID
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'user_id') THEN
        -- Get the data type of the user_id column
        IF (SELECT data_type FROM information_schema.columns 
            WHERE table_name = 'appointments' AND column_name = 'user_id') != 'uuid' THEN
            
            -- Drop any existing foreign key constraints
            ALTER TABLE appointments DROP CONSTRAINT IF EXISTS appointments_user_id_fkey;
            
            -- Alter column type to UUID
            ALTER TABLE appointments 
                ALTER COLUMN user_id TYPE uuid 
                USING 
                CASE 
                    WHEN user_id IS NULL THEN NULL
                    ELSE user_id::text::uuid 
                END;
                
            -- Add foreign key constraint
            ALTER TABLE appointments 
                ADD CONSTRAINT appointments_user_id_fkey 
                FOREIGN KEY (user_id) REFERENCES auth.users(id);
        END IF;
    END IF;

    -- Add patient_id column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'patient_id') THEN
        ALTER TABLE appointments ADD COLUMN patient_id TEXT;
    END IF;

    -- Add age column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'age') THEN
        ALTER TABLE appointments ADD COLUMN age INTEGER;
    END IF;

    -- Add gender column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'gender') THEN
        ALTER TABLE appointments ADD COLUMN gender TEXT;
    END IF;

    -- Add contact_number column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'contact_number') THEN
        ALTER TABLE appointments ADD COLUMN contact_number TEXT;
    END IF;

    -- Add email column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'email') THEN
        ALTER TABLE appointments ADD COLUMN email TEXT;
    END IF;

    -- Add time column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'time') 
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'appointment_time') THEN
        ALTER TABLE appointments ADD COLUMN time TEXT;
    END IF;

    -- Add doctor_name column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'doctor_name') THEN
        ALTER TABLE appointments ADD COLUMN doctor_name TEXT;
    END IF;

    -- Add department column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'department') THEN
        ALTER TABLE appointments ADD COLUMN department TEXT;
    END IF;

    -- Add appointment_type column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'appointment_type') THEN
        ALTER TABLE appointments ADD COLUMN appointment_type TEXT;
    END IF;

    -- Add symptoms column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'symptoms') THEN
        ALTER TABLE appointments ADD COLUMN symptoms TEXT;
    END IF;

    -- Add medical_history column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'medical_history') THEN
        ALTER TABLE appointments ADD COLUMN medical_history TEXT;
    END IF;

    -- Add preferred_mode column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'preferred_mode') THEN
        ALTER TABLE appointments ADD COLUMN preferred_mode TEXT;
    END IF;

    -- Add consultation_room column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'consultation_room') THEN
        ALTER TABLE appointments ADD COLUMN consultation_room TEXT;
    END IF;

    -- Add payment_status column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'payment_status') THEN
        ALTER TABLE appointments ADD COLUMN payment_status TEXT;
    END IF;

    -- Add notes column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'notes') THEN
        ALTER TABLE appointments ADD COLUMN notes TEXT;
    END IF;

    -- Add status column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'status') THEN
        ALTER TABLE appointments ADD COLUMN status TEXT DEFAULT 'Scheduled';
    END IF;

    -- Add user_id column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'user_id') THEN
        ALTER TABLE appointments ADD COLUMN user_id UUID REFERENCES auth.users(id);
    END IF;

    -- Add created_at column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'appointments' AND column_name = 'created_at') THEN
        ALTER TABLE appointments ADD COLUMN created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW();
    END IF;
END $$;

-- Create an index on patient_name and date for faster queries
CREATE INDEX IF NOT EXISTS idx_appointments_patient_date ON appointments(patient_name, date);

-- Create an index on user_id for faster queries
CREATE INDEX IF NOT EXISTS idx_appointments_user_id ON appointments(user_id);

-- Add Row Level Security (RLS) policies
ALTER TABLE appointments ENABLE ROW LEVEL SECURITY;

-- Drop existing policies if they exist
DROP POLICY IF EXISTS "Users can view their own appointments" ON appointments;
DROP POLICY IF EXISTS "Users can insert their own appointments" ON appointments;
DROP POLICY IF EXISTS "Users can update their own appointments" ON appointments;
DROP POLICY IF EXISTS "Users can delete their own appointments" ON appointments;
DROP POLICY IF EXISTS "Admins can view all appointments" ON appointments;
DROP POLICY IF EXISTS "Admins can insert all appointments" ON appointments;
DROP POLICY IF EXISTS "Admins can update all appointments" ON appointments;
DROP POLICY IF EXISTS "Admins can delete all appointments" ON appointments;

-- Create policies for regular users
CREATE POLICY "Users can view their own appointments"
ON appointments FOR SELECT
USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own appointments"
ON appointments FOR INSERT
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own appointments"
ON appointments FOR UPDATE
USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own appointments"
ON appointments FOR DELETE
USING (auth.uid() = user_id);

-- Create policies for admins
CREATE POLICY "Admins can view all appointments"
ON appointments FOR SELECT
USING (EXISTS (
    SELECT 1 FROM users
    WHERE users.id = auth.uid() AND users.role = 'admin'
));

CREATE POLICY "Admins can insert all appointments"
ON appointments FOR INSERT
WITH CHECK (EXISTS (
    SELECT 1 FROM users
    WHERE users.id = auth.uid() AND users.role = 'admin'
));

CREATE POLICY "Admins can update all appointments"
ON appointments FOR UPDATE
USING (EXISTS (
    SELECT 1 FROM users
    WHERE users.id = auth.uid() AND users.role = 'admin'
));

CREATE POLICY "Admins can delete all appointments"
ON appointments FOR DELETE
USING (EXISTS (
    SELECT 1 FROM users
    WHERE users.id = auth.uid() AND users.role = 'admin'
));

-- Add comments to explain the table structure
COMMENT ON TABLE appointments IS 'Stores all doctor appointments with patient details';
COMMENT ON COLUMN appointments.id IS 'Primary key - unique identifier for each appointment';
COMMENT ON COLUMN appointments.patient_name IS 'Name of the patient';
COMMENT ON COLUMN appointments.patient_id IS 'Patient ID number if available';
COMMENT ON COLUMN appointments.age IS 'Age of the patient';
COMMENT ON COLUMN appointments.gender IS 'Gender of the patient';
COMMENT ON COLUMN appointments.contact_number IS 'Contact phone number of the patient';
COMMENT ON COLUMN appointments.email IS 'Email address of the patient';
COMMENT ON COLUMN appointments.date IS 'Date of the appointment in YYYY-MM-DD format';
COMMENT ON COLUMN appointments.time IS 'Time of the appointment';
COMMENT ON COLUMN appointments.doctor_name IS 'Name of the doctor';
COMMENT ON COLUMN appointments.department IS 'Medical department';
COMMENT ON COLUMN appointments.appointment_type IS 'Type of appointment (consultation, follow-up, etc.)';
COMMENT ON COLUMN appointments.symptoms IS 'Patient symptoms description';
COMMENT ON COLUMN appointments.medical_history IS 'Brief medical history of the patient';
COMMENT ON COLUMN appointments.preferred_mode IS 'Preferred mode of appointment (in-person, telemedicine, etc.)';
COMMENT ON COLUMN appointments.consultation_room IS 'Room number or video link for the appointment';
COMMENT ON COLUMN appointments.payment_status IS 'Payment status of the appointment';
COMMENT ON COLUMN appointments.notes IS 'Additional notes about the appointment';
COMMENT ON COLUMN appointments.status IS 'Status of the appointment (scheduled, completed, cancelled, etc.)';
COMMENT ON COLUMN appointments.user_id IS 'Reference to the auth.users table for the user who created the appointment';
COMMENT ON COLUMN appointments.created_at IS 'Timestamp when the appointment was created'; 