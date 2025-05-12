package DoctorAppointmentSystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Label errorLabel;
    
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
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        // For development/demo - allow local admin login
        boolean isLocalAdminLogin = username.equals(Config.ADMIN_USERNAME) && 
                                  password.equals(Config.ADMIN_PASSWORD);
        
        // Try to authenticate with Supabase
        boolean isSupabaseLogin = false;
        try {
            isSupabaseLogin = databaseService.authenticateUser(username, password);
        } catch (Exception e) {
            System.err.println("Supabase authentication error: " + e.getMessage());
            // Continue with local authentication if Supabase fails
        }
        
        if (isLocalAdminLogin || isSupabaseLogin) {
            try {
                // Load the dashboard view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
                Parent dashboardView = loader.load();
                
                // Get the current stage
                Stage currentStage = (Stage) loginButton.getScene().getWindow();
                
                // Set the dashboard scene
                currentStage.setScene(new Scene(dashboardView, 800, 600));
                currentStage.setTitle("Doctor Appointment System - Dashboard");
                
            } catch (Exception e) {
                e.printStackTrace();
                errorLabel.setText("Error loading dashboard: " + e.getMessage());
            }
        } else {
            // Show error message
            errorLabel.setText("Invalid username or password");
            passwordField.clear();
        }
    }
} 