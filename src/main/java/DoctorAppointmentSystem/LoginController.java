package DoctorAppointmentSystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController {
    
    // Login form
    @FXML
    private VBox loginForm;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Label loginErrorLabel;
    
    // Registration form
    @FXML
    private VBox registerForm;
    
    @FXML
    private TextField regUsernameField;
    
    @FXML
    private PasswordField regPasswordField;
    
    @FXML
    private PasswordField regConfirmPasswordField;
    
    @FXML
    private TextField regEmailField;
    
    @FXML
    private RadioButton patientRoleRadio;
    
    @FXML
    private RadioButton adminRoleRadio;
    
    @FXML
    private ToggleGroup roleGroup;
    
    @FXML
    private Label registerErrorLabel;
    
    @FXML
    private StackPane contentStack;
    
    private DatabaseService databaseService;
    
    @FXML
    private void initialize() {
        // Initialize database service
        databaseService = DatabaseService.getInstance(
            Config.SUPABASE_URL,
            Config.SUPABASE_API_KEY
        );
    }
    
    @FXML
    private void handleLoginButton(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            loginErrorLabel.setText("Please enter both username and password");
            return;
        }
        
        loginErrorLabel.setText("Authenticating...");
        
        System.out.println("Attempting login with username: " + username);
        
        // Try to authenticate with Supabase
        User user = databaseService.authenticateUser(username, password);
        
        System.out.println("Authentication result: " + (user != null ? "Success" : "Failed"));
        
        if (user != null) {
            System.out.println("User authenticated as: " + user.getUsername() + " with role: " + user.getRole());
            
            // Store the current user in Config
            Config.setCurrentUser(user);
            
            try {
                // Load the dashboard view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
                Parent dashboardView = loader.load();
                
                // Get the controller
                DashboardController dashboardController = loader.getController();
                
                // Get the current stage
                Stage currentStage = (Stage) loginButton.getScene().getWindow();
                
                // Set the dashboard scene
                currentStage.setScene(new Scene(dashboardView, 800, 600));
                currentStage.setTitle("Doctor Appointment System - Dashboard (" + user.getRole() + ")");
                
            } catch (Exception e) {
                e.printStackTrace();
                loginErrorLabel.setText("Error loading dashboard: " + e.getMessage());
            }
        } else {
            // Show error message
            loginErrorLabel.setText("Invalid username or password");
            passwordField.clear();
        }
    }
    
    @FXML
    private void handleShowRegisterButton(ActionEvent event) {
        // Switch to registration form
        loginForm.setVisible(false);
        registerForm.setVisible(true);
    }
    
    @FXML
    private void handleBackToLoginButton(ActionEvent event) {
        // Switch back to login form
        registerForm.setVisible(false);
        loginForm.setVisible(true);
    }
    
    @FXML
    private void handleRegisterButton(ActionEvent event) {
        // Get input values
        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();
        String email = regEmailField.getText().trim();
        String role = patientRoleRadio.isSelected() ? "patient" : "admin";
        
        // Validate input
        if (username.isEmpty()) {
            registerErrorLabel.setText("Please enter a username");
            return;
        }
        
        if (password.isEmpty()) {
            registerErrorLabel.setText("Please enter a password");
            return;
        }
        
        if (password.length() < 6) {
            registerErrorLabel.setText("Password must be at least 6 characters");
            regPasswordField.clear();
            regConfirmPasswordField.clear();
            return;
        }
        
        if (email.isEmpty()) {
            registerErrorLabel.setText("Please enter an email address");
            return;
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            registerErrorLabel.setText("Please enter a valid email address");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            registerErrorLabel.setText("Passwords do not match");
            regPasswordField.clear();
            regConfirmPasswordField.clear();
            return;
        }
        
        registerErrorLabel.setText("Creating account...");
        
        // Create new user
        User newUser = new User(0, username, password, role, email);
        
        // Register user
        boolean success = databaseService.registerUser(newUser);
        
        if (success) {
            // Show success message and switch back to login
            loginErrorLabel.setText("Registration successful! Please login.");
            handleBackToLoginButton(null);
            
            // Pre-fill the login fields
            usernameField.setText(username);
            passwordField.clear();
        } else {
            registerErrorLabel.setText("Registration failed. Username may be taken.");
        }
    }
} 