package DoctorAppointmentSystem;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

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
    
    // Email verification with JavaMail API
    public static boolean sendVerificationEmail(String toEmail, String otp) {
        try {
            // Try to send a real email with JavaMail API
            String emailHost = Config.EMAIL_HOST;
            int emailPort = Config.EMAIL_PORT;
            final String emailUsername = Config.EMAIL_USERNAME;
            final String emailPassword = Config.EMAIL_PASSWORD;
            
            // Check if email settings are available
            if (emailHost == null || emailHost.isEmpty() ||
                emailUsername == null || emailUsername.isEmpty() ||
                emailPassword == null || emailPassword.isEmpty()) {
                throw new Exception("Email settings not properly configured");
            }
            
            // Set up mail server properties
            Properties properties = new Properties();
            properties.put("mail.smtp.host", emailHost);
            properties.put("mail.smtp.port", emailPort);
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            
            // Create session with authenticator
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailUsername, emailPassword);
                }
            });
            
            // Create a new message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailUsername, "Doctor Appointment System"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Doctor Appointment System - Email Verification");
            
            // HTML content with styling
            String htmlContent = 
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }" +
                ".header { background-color: #4285f4; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                ".content { padding: 20px; }" +
                ".code { font-size: 24px; font-weight: bold; text-align: center; padding: 10px; margin: 20px 0; background-color: #f0f0f0; border-radius: 5px; }" +
                ".footer { font-size: 12px; text-align: center; margin-top: 20px; color: #777; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'><h2>Doctor Appointment System</h2></div>" +
                "<div class='content'>" +
                "<p>Dear User,</p>" +
                "<p>Thank you for registering with Doctor Appointment System.</p>" +
                "<p>Your verification code is:</p>" +
                "<div class='code'>" + otp + "</div>" +
                "<p>This code will expire in " + OTP_EXPIRY_MINUTES + " minutes.</p>" +
                "<p>If you did not request this verification, please ignore this email.</p>" +
                "<p>Regards,<br>Doctor Appointment System Team</p>" +
                "</div>" +
                "<div class='footer'>This is an automated message. Please do not reply to this email.</div>" +
                "</div>" +
                "</body>" +
                "</html>";
            
            // Set the message content
            message.setContent(htmlContent, "text/html");
            
            // Send the message
            Transport.send(message);
            
            System.out.println("Verification email sent successfully to: " + toEmail);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to the enhanced simulation if real email fails
            try {
                // Display the verification code prominently
                System.out.println("\n");
                System.out.println("*******************************************************************");
                System.out.println("**                   VERIFICATION CODE EMAIL                     **");
                System.out.println("*******************************************************************");
                System.out.println("** To: " + toEmail);
                System.out.println("** Subject: Doctor Appointment System - Email Verification");
                System.out.println("**");
                System.out.println("** Dear User,");
                System.out.println("**");
                System.out.println("** Thank you for registering with Doctor Appointment System.");
                System.out.println("**");
                System.out.println("** YOUR VERIFICATION CODE IS: " + otp);
                System.out.println("**");
                System.out.println("** This code will expire in " + OTP_EXPIRY_MINUTES + " minutes.");
                System.out.println("**");
                System.out.println("** If you did not request this verification, please ignore this email.");
                System.out.println("**");
                System.out.println("** Regards,");
                System.out.println("** Doctor Appointment System Team");
                System.out.println("*******************************************************************");
                System.out.println("\n");
                
                // Save the verification code to a file for convenient access
                try {
                    java.io.File tempDir = new java.io.File("temp");
                    if (!tempDir.exists()) {
                        tempDir.mkdir();
                    }
                    
                    java.io.File otpFile = new java.io.File("temp/verification_code.txt");
                    java.io.FileWriter writer = new java.io.FileWriter(otpFile);
                    writer.write("Verification code for " + toEmail + ": " + otp);
                    writer.close();
                    
                    System.out.println("Verification code also saved to: " + otpFile.getAbsolutePath());
                } catch (Exception ex) {
                    System.err.println("Failed to save verification code to file: " + ex.getMessage());
                }
                
                return true;
            } catch (Exception fallbackError) {
                System.err.println("Failed to send verification email (even fallback): " + fallbackError.getMessage());
                fallbackError.printStackTrace();
                return false;
            }
        }
    }
} 