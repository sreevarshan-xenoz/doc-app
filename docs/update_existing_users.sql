-- Mark all existing users as verified
UPDATE users SET verified = TRUE WHERE verified IS NULL OR verified = FALSE; 