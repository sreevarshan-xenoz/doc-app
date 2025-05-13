package DoctorAppointmentSystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientProfileController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(PatientProfileController.class.getName());
    private DatabaseService databaseService;
    private User currentUser;
    private Patient patientProfile;

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    
    // Personal Information
    @FXML private TextField fullNameField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private ComboBox<String> bloodGroupComboBox;
    
    // Contact Information
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private TextField emergencyContactField;
    
    // Medical Information
    @FXML private TextField heightField;
    @FXML private TextField weightField;
    @FXML private TextArea allergiesField;
    @FXML private TextArea medicationsField;
    @FXML private TextArea medicalHistoryField;
    
    // Insurance Information
    @FXML private TextField insuranceProviderField;
    @FXML private TextField policyNumberField;
    
    // Buttons
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            databaseService = new DatabaseService();
            setupFormControls();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing controller", e);
            showAlert("Error", "Could not initialize the form. Please try again later.");
        }
    }
    
    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername());
            loadPatientProfile();
        }
    }
    
    private void setupFormControls() {
        // Setup gender ComboBox
        ObservableList<String> genderOptions = FXCollections.observableArrayList(
                "Male", "Female", "Other", "Prefer not to say"
        );
        genderComboBox.setItems(genderOptions);
        
        // Setup blood group ComboBox
        ObservableList<String> bloodGroupOptions = FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"
        );
        bloodGroupComboBox.setItems(bloodGroupOptions);
    }
    
    private void loadPatientProfile() {
        if (currentUser == null) {
            return;
        }
        
        try {
            patientProfile = databaseService.getPatientProfile(currentUser.getId());
            
            if (patientProfile != null) {
                displayPatientProfile();
            } else {
                // Create a new empty profile for this user
                patientProfile = new Patient();
                patientProfile.setUserId(currentUser.getId());
                
                // Pre-fill with any known information from the user account
                fullNameField.setText(currentUser.getUsername());
                emailField.setText(currentUser.getEmail());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading patient profile", e);
            showAlert("Error", "Could not load patient profile. " + e.getMessage());
        }
    }
    
    private void displayPatientProfile() {
        if (patientProfile == null) {
            return;
        }
        
        // Personal Information
        fullNameField.setText(patientProfile.getFullName());
        if (patientProfile.getDateOfBirth() != null && !patientProfile.getDateOfBirth().isEmpty()) {
            dobPicker.setValue(LocalDate.parse(patientProfile.getDateOfBirth()));
        }
        genderComboBox.setValue(patientProfile.getGender());
        bloodGroupComboBox.setValue(patientProfile.getBloodGroup());
        
        // Contact Information
        emailField.setText(patientProfile.getEmail());
        phoneField.setText(patientProfile.getPhoneNumber());
        addressField.setText(patientProfile.getAddress());
        emergencyContactField.setText(patientProfile.getEmergencyContact());
        
        // Medical Information
        heightField.setText(patientProfile.getHeight());
        weightField.setText(patientProfile.getWeight());
        allergiesField.setText(patientProfile.getAllergies());
        medicationsField.setText(patientProfile.getCurrentMedications());
        medicalHistoryField.setText(patientProfile.getMedicalHistory());
        
        // Insurance Information
        insuranceProviderField.setText(patientProfile.getInsuranceProvider());
        policyNumberField.setText(patientProfile.getPolicyNumber());
    }

    @FXML
    void handleSaveButton(ActionEvent event) {
        if (validateForm()) {
            try {
                savePatientProfile();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error saving patient profile", e);
                showAlert("Error", "Failed to save patient profile. " + e.getMessage());
            }
        }
    }
    
    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (fullNameField.getText().trim().isEmpty()) {
            errorMessage.append("- Full name is required.\n");
        }
        
        if (emailField.getText().trim().isEmpty() || !isValidEmail(emailField.getText().trim())) {
            errorMessage.append("- Valid email address is required.\n");
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            errorMessage.append("- Phone number is required.\n");
        }
        
        if (!heightField.getText().trim().isEmpty() && !isNumeric(heightField.getText().trim())) {
            errorMessage.append("- Height must be a number.\n");
        }
        
        if (!weightField.getText().trim().isEmpty() && !isNumeric(weightField.getText().trim())) {
            errorMessage.append("- Weight must be a number.\n");
        }
        
        if (errorMessage.length() > 0) {
            showAlert("Validation Error", "Please correct the following errors:\n" + errorMessage.toString());
            return false;
        }
        
        return true;
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
    
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private void savePatientProfile() {
        if (patientProfile == null) {
            patientProfile = new Patient();
        }
        
        // Set user ID
        if (currentUser != null) {
            patientProfile.setUserId(currentUser.getId());
        }
        
        // Personal Information
        patientProfile.setFullName(fullNameField.getText().trim());
        patientProfile.setDateOfBirth(dobPicker.getValue() != null ? dobPicker.getValue().toString() : "");
        patientProfile.setGender(genderComboBox.getValue());
        patientProfile.setBloodGroup(bloodGroupComboBox.getValue());
        
        // Contact Information
        patientProfile.setEmail(emailField.getText().trim());
        patientProfile.setPhoneNumber(phoneField.getText().trim());
        patientProfile.setAddress(addressField.getText().trim());
        patientProfile.setEmergencyContact(emergencyContactField.getText().trim());
        
        // Medical Information
        patientProfile.setHeight(heightField.getText().trim());
        patientProfile.setWeight(weightField.getText().trim());
        patientProfile.setAllergies(allergiesField.getText().trim());
        patientProfile.setCurrentMedications(medicationsField.getText().trim());
        patientProfile.setMedicalHistory(medicalHistoryField.getText().trim());
        
        // Insurance Information
        patientProfile.setInsuranceProvider(insuranceProviderField.getText().trim());
        patientProfile.setPolicyNumber(policyNumberField.getText().trim());
        
        // Save to database
        boolean success = databaseService.savePatientProfile(patientProfile);
        
        if (success) {
            statusLabel.setText("Patient profile saved successfully!");
        } else {
            showAlert("Error", "Failed to save patient profile. Please try again later.");
        }
    }
    
    @FXML
    void handleClearButton(ActionEvent event) {
        clearForm();
    }
    
    private void clearForm() {
        // Personal Information
        fullNameField.clear();
        dobPicker.setValue(null);
        genderComboBox.setValue(null);
        bloodGroupComboBox.setValue(null);
        
        // Contact Information
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        emergencyContactField.clear();
        
        // Medical Information
        heightField.clear();
        weightField.clear();
        allergiesField.clear();
        medicationsField.clear();
        medicalHistoryField.clear();
        
        // Insurance Information
        insuranceProviderField.clear();
        policyNumberField.clear();
        
        statusLabel.setText("");
    }
    
    @FXML
    void handleBackButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/appointment_form.fxml"));
            Parent root = loader.load();
            
            // Pass user back to appointment form
            AppointmentFormController appointmentController = loader.getController();
            if (currentUser != null) {
                appointmentController.setUser(currentUser);
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading appointment form", e);
            showAlert("Error", "Could not return to appointment form. " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 