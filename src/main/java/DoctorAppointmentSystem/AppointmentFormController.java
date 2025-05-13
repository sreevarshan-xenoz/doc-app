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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppointmentFormController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AppointmentFormController.class.getName());
    private DatabaseService databaseService;
    private User currentUser;

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    
    // Patient information
    @FXML private TextField patientNameField;
    @FXML private Button managePatientButton;
    
    // Schedule information
    @FXML private DatePicker appointmentDatePicker;
    @FXML private ComboBox<String> appointmentTimeComboBox;
    
    // Doctor information
    @FXML private TextField doctorNameField;
    @FXML private ComboBox<String> departmentComboBox;
    
    // Appointment details
    @FXML private ComboBox<String> appointmentTypeComboBox;
    @FXML private ComboBox<String> appointmentModeComboBox;
    @FXML private TextArea symptomsTextArea;
    @FXML private TextArea notesTextArea;
    
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
            
            // If the user already has patient profile, pre-fill the fields
            if (user.getRole().equals("patient")) {
                patientNameField.setText(user.getUsername());
                patientNameField.setEditable(false);
            }
        }
    }
    
    private void setupFormControls() {
        // Setup appointment times (8 AM to 5 PM with 30-minute slots)
        ObservableList<String> times = FXCollections.observableArrayList();
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        
        while (startTime.isBefore(endTime)) {
            times.add(startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            startTime = startTime.plusMinutes(30);
        }
        appointmentTimeComboBox.setItems(times);
        
        // Setup departments
        ObservableList<String> departments = FXCollections.observableArrayList(
                "Cardiology", "Dermatology", "Endocrinology", "Gastroenterology", 
                "Hematology", "Neurology", "Obstetrics", "Oncology", "Ophthalmology", 
                "Orthopedics", "Pediatrics", "Psychiatry", "Urology"
        );
        departmentComboBox.setItems(departments);
        
        // Setup appointment types
        ObservableList<String> appointmentTypes = FXCollections.observableArrayList(
                "Regular Checkup", "Follow-up", "Emergency", "Consultation", "Procedure"
        );
        appointmentTypeComboBox.setItems(appointmentTypes);
        
        // Setup appointment modes
        ObservableList<String> appointmentModes = FXCollections.observableArrayList(
                "In-person", "Virtual", "Home Visit"
        );
        appointmentModeComboBox.setItems(appointmentModes);
        
        // Set default date to today
        appointmentDatePicker.setValue(LocalDate.now());
    }

    @FXML
    void handleSaveButton(ActionEvent event) {
        if (validateForm()) {
            try {
                saveAppointment();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error saving appointment", e);
                showAlert("Error", "Failed to save appointment. " + e.getMessage());
            }
        }
    }
    
    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (patientNameField.getText().trim().isEmpty()) {
            errorMessage.append("- Patient name is required.\n");
        }
        
        if (appointmentDatePicker.getValue() == null) {
            errorMessage.append("- Appointment date is required.\n");
        } else if (appointmentDatePicker.getValue().isBefore(LocalDate.now())) {
            errorMessage.append("- Appointment date cannot be in the past.\n");
        }
        
        if (appointmentTimeComboBox.getValue() == null) {
            errorMessage.append("- Appointment time is required.\n");
        }
        
        if (doctorNameField.getText().trim().isEmpty()) {
            errorMessage.append("- Doctor name is required.\n");
        }
        
        if (departmentComboBox.getValue() == null) {
            errorMessage.append("- Department is required.\n");
        }
        
        if (appointmentTypeComboBox.getValue() == null) {
            errorMessage.append("- Appointment type is required.\n");
        }
        
        if (appointmentModeComboBox.getValue() == null) {
            errorMessage.append("- Appointment mode is required.\n");
        }
        
        if (errorMessage.length() > 0) {
            showAlert("Validation Error", "Please correct the following errors:\n" + errorMessage.toString());
            return false;
        }
        
        return true;
    }
    
    private void saveAppointment() {
        Appointment appointment = new Appointment();
        
        // Set basic appointment details
        appointment.setPatientName(patientNameField.getText().trim());
        appointment.setAppointmentDate(appointmentDatePicker.getValue().toString());
        appointment.setAppointmentTime(appointmentTimeComboBox.getValue());
        appointment.setDoctorName(doctorNameField.getText().trim());
        appointment.setDepartment(departmentComboBox.getValue());
        
        // Set additional appointment details
        appointment.setAppointmentType(appointmentTypeComboBox.getValue());
        appointment.setAppointmentMode(appointmentModeComboBox.getValue());
        appointment.setSymptoms(symptomsTextArea.getText().trim());
        appointment.setNotes(notesTextArea.getText().trim());
        
        // Set user ID if logged in
        if (currentUser != null) {
            appointment.setUserId(currentUser.getId());
        }
        
        // Save to database
        boolean success = databaseService.saveAppointment(appointment);
        
        if (success) {
            statusLabel.setText("Appointment booked successfully!");
            clearForm();
        } else {
            showAlert("Error", "Failed to save appointment. Please try again later.");
        }
    }
    
    @FXML
    void handleClearButton(ActionEvent event) {
        clearForm();
    }
    
    private void clearForm() {
        // Don't clear patient name if user is a patient
        if (currentUser == null || !currentUser.getRole().equals("patient")) {
            patientNameField.clear();
        }
        
        appointmentDatePicker.setValue(LocalDate.now());
        appointmentTimeComboBox.setValue(null);
        doctorNameField.clear();
        departmentComboBox.setValue(null);
        appointmentTypeComboBox.setValue(null);
        appointmentModeComboBox.setValue(null);
        symptomsTextArea.clear();
        notesTextArea.clear();
        statusLabel.setText("");
    }
    
    @FXML
    void handleBackButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
            Parent root = loader.load();
            
            // Pass user to dashboard
            DashboardController dashboardController = loader.getController();
            if (currentUser != null) {
                dashboardController.setUser(currentUser);
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading dashboard", e);
            showAlert("Error", "Could not return to dashboard. " + e.getMessage());
        }
    }
    
    @FXML
    void handleManagePatientButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/patient_profile.fxml"));
            Parent root = loader.load();
            
            // Pass user to patient profile controller
            PatientProfileController patientController = loader.getController();
            if (currentUser != null) {
                patientController.setUser(currentUser);
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading patient profile", e);
            showAlert("Error", "Could not open patient profile. " + e.getMessage());
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