package DoctorAppointmentSystem;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;

public class EmailService {
    // In-memory OTP storage (in a real app, use a database or Redis)
    private static Map<String, OtpData> otpMap = new HashMap<>();
    
    // OTP expiration time in minutes
    private static final int OTP_EXPIRY_MINUTES = 5;
    
    // Class to store OTP data
    private static class OtpData {
        private String otp;
        private long expiryTimeMillis;
        private User pendingUser;
        
        public OtpData(String otp, User pendingUser) {
            this.otp = otp;
            this.pendingUser = pendingUser;
            this.expiryTimeMillis = System.currentTimeMillis() + (OTP_EXPIRY_MINUTES * 60 * 1000);
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTimeMillis;
        }
    }
    
    // Generate a random 6-digit OTP
    public static String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    
    // Store OTP for verification later
    public static void storeOTP(String email, String otp, User pendingUser) {
        otpMap.put(email, new OtpData(otp, pendingUser));
    }
    
    // Verify the OTP
    public static User verifyOTP(String email, String otp) {
        OtpData otpData = otpMap.get(email);
        
        if (otpData == null) {
            System.out.println("No OTP found for email: " + email);
            return null;
        }
        
        // Special case for resend
        if ("GET_USER_ONLY".equals(otp)) {
            return otpData.pendingUser;
        }
        
        if (otpData.isExpired()) {
            System.out.println("OTP expired for email: " + email);
            otpMap.remove(email);
            return null;
        }
        
        if (!otpData.otp.equals(otp)) {
            System.out.println("Invalid OTP for email: " + email);
            return null;
        }
        
        // OTP verified, remove from map and return the pending user
        User pendingUser = otpData.pendingUser;
        otpMap.remove(email);
        return pendingUser;
    }
    
    // Simulated email sending for testing
    public static boolean sendVerificationEmail(String toEmail, String otp) {
        try {
            // In a real application, this would send an actual email
            // For this example, we'll just print to console
            System.out.println("\n========== SIMULATED EMAIL ==========");
            System.out.println("To: " + toEmail);
            System.out.println("Subject: Doctor Appointment System - Email Verification");
            System.out.println("\nDear User,");
            System.out.println("\nThank you for registering with Doctor Appointment System.");
            System.out.println("\nYour verification code is: " + otp);
            System.out.println("\nThis code will expire in " + OTP_EXPIRY_MINUTES + " minutes.");
            System.out.println("\nIf you did not request this verification, please ignore this email.");
            System.out.println("\nRegards,");
            System.out.println("Doctor Appointment System Team");
            System.out.println("===================================\n");
            
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 