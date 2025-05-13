package DoctorAppointmentSystem;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class PatientFormController implements Initializable {
    
    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField ageField;
    
    @FXML
    private ComboBox<String> genderComboBox;
    
    @FXML
    private TextField contactNumberField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextArea medicalHistoryArea;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Label statusLabel;
    
    private DatabaseService databaseService;
    private Patient existingPatient;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize database service
        databaseService = DatabaseService.getInstance(
            Config.SUPABASE_URL,
            Config.SUPABASE_API_KEY
        );
        
        // Set welcome message
        User currentUser = Config.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername());
        } else {
            welcomeLabel.setText("Welcome, Guest");
        }
        
        // Configure gender dropdown
        genderComboBox.setItems(FXCollections.observableArrayList(
            "Male", "Female", "Other", "Prefer not to say"
        ));
        
        // Check if patient info already exists
        loadExistingPatientInfo();
    }
    
    private void loadExistingPatientInfo() {
        User currentUser = Config.getCurrentUser();
        if (currentUser != null) {
            try {
                // Get patient info if it exists
                Patient patient = databaseService.getPatientByUserId(currentUser.getId());
                
                if (patient != null) {
                    existingPatient = patient;
                    
                    // Populate form with existing data
                    fullNameField.setText(patient.getFullName());
                    ageField.setText(String.valueOf(patient.getAge()));
                    genderComboBox.setValue(patient.getGender());
                    contactNumberField.setText(patient.getContactNumber());
                    emailField.setText(patient.getEmail());
                    medicalHistoryArea.setText(patient.getMedicalHistory());
                    
                    // Update button text to indicate update
                    saveButton.setText("Update Information");
                }
            } catch (Exception e) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Error loading patient information: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleSaveButton(ActionEvent event) {
        // Validate input
        if (!validateInput()) {
            return;
        }
        
        try {
            User currentUser = Config.getCurrentUser();
            if (currentUser == null) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("You must be logged in to save patient information");
                return;
            }
            
            // Get form values
            String fullName = fullNameField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());
            String gender = genderComboBox.getValue();
            String contactNumber = contactNumberField.getText().trim();
            String email = emailField.getText().trim();
            String medicalHistory = medicalHistoryArea.getText().trim();
            
            boolean success;
            
            if (existingPatient != null) {
                // Update existing patient
                existingPatient.setFullName(fullName);
                existingPatient.setAge(age);
                existingPatient.setGender(gender);
                existingPatient.setContactNumber(contactNumber);
                existingPatient.setEmail(email);
                existingPatient.setMedicalHistory(medicalHistory);
                
                success = databaseService.updatePatient(existingPatient);
            } else {
                // Create new patient
                Patient newPatient = new Patient(
                    currentUser.getId(), fullName, age, gender, contactNumber, email, medicalHistory
                );
                
                success = databaseService.insertPatient(newPatient);
            }
            
            if (success) {
                statusLabel.setStyle("-fx-text-fill: #4caf50;");
                statusLabel.setText("Patient information saved successfully");
                
                // If this was a new patient, reload to get the ID
                if (existingPatient == null) {
                    loadExistingPatientInfo();
                }
            } else {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Failed to save patient information");
            }
        } catch (NumberFormatException e) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please enter a valid age (numbers only)");
        } catch (Exception e) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Error saving patient information: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearButton(ActionEvent event) {
        // Clear all form fields
        fullNameField.clear();
        ageField.clear();
        genderComboBox.setValue(null);
        contactNumberField.clear();
        emailField.clear();
        medicalHistoryArea.clear();
        statusLabel.setText("");
    }
    
    @FXML
    private void handleBackButton(ActionEvent event) {
        try {
            // Load the dashboard view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
            Parent dashboardView = loader.load();
            
            // Get the current stage
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            
            // Set the dashboard scene
            currentStage.setScene(new Scene(dashboardView, 800, 600));
            currentStage.setTitle("Doctor Appointment System - Dashboard");
            
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error returning to dashboard: " + e.getMessage());
        }
    }
    
    private boolean validateInput() {
        // Validate full name
        if (fullNameField.getText().trim().isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please enter your full name");
            fullNameField.requestFocus();
            return false;
        }
        
        // Validate age
        if (ageField.getText().trim().isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please enter your age");
            ageField.requestFocus();
            return false;
        }
        
        try {
            int age = Integer.parseInt(ageField.getText().trim());
            if (age <= 0 || age > 120) {
                statusLabel.setStyle("-fx-text-fill: red;");
                statusLabel.setText("Please enter a valid age (1-120)");
                ageField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please enter a valid age (numbers only)");
            ageField.requestFocus();
            return false;
        }
        
        // Validate gender
        if (genderComboBox.getValue() == null) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please select your gender");
            genderComboBox.requestFocus();
            return false;
        }
        
        // Validate contact number
        if (contactNumberField.getText().trim().isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please enter your contact number");
            contactNumberField.requestFocus();
            return false;
        }
        
        // Validate email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please enter your email address");
            emailField.requestFocus();
            return false;
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please enter a valid email address");
            emailField.requestFocus();
            return false;
        }
        
        return true;
    }
} 