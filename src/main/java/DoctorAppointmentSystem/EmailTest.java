package DoctorAppointmentSystem;

/**
 * A simple test class to test email functionality without requiring JavaFX
 */
public class EmailTest {
    public static void main(String[] args) {
        System.out.println("Doctor Appointment System - Email Test");
        System.out.println("=====================================");
        
        // Initialize the system
        System.out.println("Initializing system...");
        
        // Generate a random OTP
        String testEmail = "test@example.com";
        String otp = EmailService.generateOTP();
        
        System.out.println("Generated OTP: " + otp);
        
        // Create a dummy user for testing
        User dummyUser = new User();
        dummyUser.setEmail(testEmail);
        dummyUser.setUsername("testuser");
        dummyUser.setPassword("password123");
        dummyUser.setRole("patient");
        
        // Store the OTP
        System.out.println("Storing OTP for verification...");
        EmailService.storeOTP(testEmail, otp, dummyUser);
        
        // Send verification email
        System.out.println("Sending verification email to: " + testEmail);
        boolean emailSent = EmailService.sendVerificationEmail(testEmail, otp);
        
        if (emailSent) {
            System.out.println("Email sent successfully!");
            
            // Verify the OTP
            System.out.println("Verifying OTP...");
            User verifiedUser = EmailService.verifyOTP(testEmail, otp);
            
            if (verifiedUser != null) {
                System.out.println("OTP verification successful for user: " + verifiedUser.getUsername());
            } else {
                System.out.println("OTP verification failed");
            }
        } else {
            System.out.println("Failed to send email");
        }
        
        System.out.println("Email test completed.");
    }
} 