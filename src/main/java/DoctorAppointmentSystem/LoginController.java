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
    
    @FXML
    private void handleLoginButton(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        // Simple authentication (default: admin/admin)
        if (username.equals("admin") && password.equals("admin")) {
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