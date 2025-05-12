package DoctorAppointmentSystem;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

public class VerificationController {
    
    @FXML
    private TextField otpField;
    
    @FXML
    private Label timerLabel;
    
    @FXML
    private Button verifyButton;
    
    @FXML
    private Button resendButton;
    
    @FXML
    private Button backToLoginButton;
    
    @FXML
    private Label statusLabel;
    
    private DatabaseService databaseService;
    private String userEmail;
    private Timeline timeline;
    private int timeRemaining;
    
    // Initialize with user's email
    public void initialize(String email) {
        this.userEmail = email;
        databaseService = DatabaseService.getInstance(
            Config.SUPABASE_URL,
            Config.SUPABASE_API_KEY
        );
        startTimer();
    }
    
    // Start countdown timer
    private void startTimer() {
        // Initial time is 5 minutes (300 seconds)
        timeRemaining = 300;
        
        // Stop existing timer if any
        if (timeline != null) {
            timeline.stop();
        }
        
        // Update timer label
        updateTimerLabel();
        
        // Create new timer
        timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> {
                timeRemaining--;
                updateTimerLabel();
                
                if (timeRemaining <= 0) {
                    timeline.stop();
                    statusLabel.setText("Verification code expired. Please request a new one.");
                    verifyButton.setDisable(true);
                }
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private void updateTimerLabel() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        timerLabel.setText(String.format("Expires in: %02d:%02d", minutes, seconds));
    }
    
    @FXML
    private void handleVerifyButton(ActionEvent event) {
        String otpCode = otpField.getText().trim();
        
        if (otpCode.isEmpty()) {
            statusLabel.setText("Please enter the verification code");
            return;
        }
        
        if (otpCode.length() != 6 || !otpCode.matches("\\d+")) {
            statusLabel.setText("Please enter a valid 6-digit code");
            return;
        }
        
        // Verify OTP
        User verifiedUser = EmailService.verifyOTP(userEmail, otpCode);
        
        if (verifiedUser != null) {
            // Stop the timer
            if (timeline != null) {
                timeline.stop();
            }
            
            // Set user as verified
            verifiedUser.setVerified(true);
            
            // Register the verified user
            boolean success = databaseService.registerUser(verifiedUser);
            
            if (success) {
                statusLabel.setText("Email verified successfully!");
                
                // Navigate to login screen
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                    Parent loginView = loader.load();
                    
                    // Get controller and pre-fill username
                    LoginController loginController = loader.getController();
                    loginController.prefillUsername(verifiedUser.getUsername());
                    
                    // Get the current stage
                    Stage currentStage = (Stage) verifyButton.getScene().getWindow();
                    
                    // Set the login scene
                    currentStage.setScene(new Scene(loginView, 600, 400));
                    currentStage.setTitle("Doctor Appointment System - Login");
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    statusLabel.setText("Error navigating to login: " + e.getMessage());
                }
            } else {
                statusLabel.setText("Error registering user after verification");
            }
        } else {
            statusLabel.setText("Invalid or expired verification code");
        }
    }
    
    @FXML
    private void handleResendButton(ActionEvent event) {
        // Send a new OTP
        String newOtp = EmailService.generateOTP();
        
        // Get pending user from existing OTP
        User pendingUser = EmailService.verifyOTP(userEmail, "GET_USER_ONLY");
        
        if (pendingUser != null) {
            // Send verification email
            boolean emailSent = EmailService.sendVerificationEmail(userEmail, newOtp);
            
            if (emailSent) {
                // Store new OTP
                EmailService.storeOTP(userEmail, newOtp, pendingUser);
                
                // Reset timer
                startTimer();
                
                // Enable verify button
                verifyButton.setDisable(false);
                
                statusLabel.setText("A new verification code has been sent to your email");
            } else {
                statusLabel.setText("Failed to send verification email. Please try again.");
            }
        } else {
            statusLabel.setText("Your session has expired. Please register again.");
        }
    }
    
    @FXML
    private void handleBackToLoginButton(ActionEvent event) {
        try {
            // Stop the timer
            if (timeline != null) {
                timeline.stop();
            }
            
            // Navigate back to login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent loginView = loader.load();
            
            // Get the current stage
            Stage currentStage = (Stage) backToLoginButton.getScene().getWindow();
            
            // Set the login scene
            currentStage.setScene(new Scene(loginView, 600, 400));
            currentStage.setTitle("Doctor Appointment System - Login");
            
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error navigating to login: " + e.getMessage());
        }
    }
} 