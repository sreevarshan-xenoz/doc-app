-- Fix user_id column type from integer to UUID
-- This is a targeted migration file for use with Supabase CLI

-- First drop the existing foreign key constraint if it exists
ALTER TABLE appointments DROP CONSTRAINT IF EXISTS appointments_user_id_fkey;

-- Then alter the column type to UUID
-- Handle possible NULL values and data conversion
ALTER TABLE appointments 
  ALTER COLUMN user_id TYPE uuid 
  USING 
    CASE 
      WHEN user_id IS NULL THEN NULL
      ELSE user_id::text::uuid 
    END;

-- Add the foreign key constraint back
ALTER TABLE appointments 
  ADD CONSTRAINT appointments_user_id_fkey 
  FOREIGN KEY (user_id) REFERENCES auth.users(id); 