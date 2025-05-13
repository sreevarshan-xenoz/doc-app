package DoctorAppointmentSystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    
    // Patient Information fields
    @FXML
    private TextField patientNameField;
    
    @FXML
    private TextField patientIdField;
    
    @FXML
    private TextField ageField;
    
    @FXML
    private ComboBox<String> genderComboBox;
    
    @FXML
    private TextField contactNumberField;
    
    @FXML
    private TextField emailField;
    
    // Appointment Details fields
    @FXML
    private DatePicker appointmentDatePicker;
    
    @FXML
    private ComboBox<String> timeComboBox;
    
    @FXML
    private ComboBox<String> doctorComboBox;
    
    @FXML
    private ComboBox<String> departmentComboBox;
    
    @FXML
    private ComboBox<String> appointmentTypeComboBox;
    
    // Additional Details fields
    @FXML
    private TextArea symptomsField;
    
    @FXML
    private TextArea medicalHistoryField;
    
    @FXML
    private ComboBox<String> modeComboBox;
    
    @FXML
    private TextField roomField;
    
    @FXML
    private ComboBox<String> paymentStatusComboBox;
    
    @FXML
    private TextArea notesField;
    
    // Buttons and other controls
    @FXML
    private Button bookAppointmentButton;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private TableView<Appointment> appointmentsTableView;
    
    @FXML
    private TableColumn<Appointment, String> idColumn;
    
    @FXML
    private TableColumn<Appointment, String> patientNameColumn;
    
    @FXML
    private TableColumn<Appointment, String> dateColumn;
    
    @FXML
    private TableColumn<Appointment, String> timeColumn;
    
    @FXML
    private TableColumn<Appointment, String> doctorColumn;
    
    @FXML
    private TableColumn<Appointment, String> departmentColumn;
    
    @FXML
    private TableColumn<Appointment, String> typeColumn;
    
    @FXML
    private TableColumn<Appointment, String> statusColumn;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private VBox adminControls;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Button manageUsersButton;
    
    // Observable list to store appointments
    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    
    // Database service
    private DatabaseService databaseService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize database service
        databaseService = DatabaseService.getInstance(
            Config.SUPABASE_URL,
            Config.SUPABASE_API_KEY
        );
        
        // Initialize table columns
        setupTableColumns();
        
        // Set the appointments list as the data source for the TableView
        appointmentsTableView.setItems(appointments);
        
        // Set default values for ComboBoxes
        setDefaultValues();
        
        // Configure UI based on user role
        configureUIForUserRole();
        
        // Load appointments from the database
        loadAppointmentsFromDatabase();
        
        // Set up context menu for appointment deletion and detail view
        setupContextMenu();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentType"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Add row color based on status
        appointmentsTableView.setRowFactory(tv -> new TableRow<Appointment>() {
            @Override
            protected void updateItem(Appointment appointment, boolean empty) {
                super.updateItem(appointment, empty);
                if (appointment == null || empty) {
                    setStyle("");
                } else {
                    if ("Cancelled".equals(appointment.getStatus())) {
                        setStyle("-fx-background-color: #ffcccc;"); // Light red for cancelled
                    } else if ("Completed".equals(appointment.getStatus())) {
                        setStyle("-fx-background-color: #ccffcc;"); // Light green for completed
                    } else if ("No-show".equals(appointment.getStatus())) {
                        setStyle("-fx-background-color: #ccccff;"); // Light blue for no-show
                    } else {
                        setStyle(""); // Default for scheduled
                    }
                }
            }
        });
    }
    
    private void setDefaultValues() {
        // Set default selection for ComboBoxes
        if (timeComboBox.getItems().size() > 0) {
            timeComboBox.setValue("9:00 AM");
        }
        
        if (doctorComboBox.getItems().size() > 0) {
            doctorComboBox.setValue("Dr. Smith");
        }
        
        if (departmentComboBox.getItems().size() > 0) {
            departmentComboBox.setValue("General");
        }
        
        if (appointmentTypeComboBox.getItems().size() > 0) {
            appointmentTypeComboBox.setValue("Consultation");
        }
        
        if (modeComboBox.getItems().size() > 0) {
            modeComboBox.setValue("In-person");
        }
        
        if (paymentStatusComboBox.getItems().size() > 0) {
            paymentStatusComboBox.setValue("Unpaid");
        }
        
        if (genderComboBox.getItems().size() > 0) {
            genderComboBox.setValue("Prefer not to say");
        }
    }
    
    private void configureUIForUserRole() {
        User currentUser = Config.getCurrentUser();
        
        if (currentUser != null) {
            // Set welcome message
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
            
            // For patient role, prefill the patient name
            if (currentUser.isPatient()) {
                patientNameField.setText(currentUser.getUsername());
                patientNameField.setEditable(false);
                
                // Only show admin controls to admins
                adminControls.setVisible(false);
                adminControls.setManaged(false);
            }
        } else {
            welcomeLabel.setText("Welcome, Guest");
        }
    }
    
    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem viewItem = new MenuItem("View Details");
        MenuItem editItem = new MenuItem("Edit Appointment");
        MenuItem cancelItem = new MenuItem("Cancel Appointment");
        MenuItem deleteItem = new MenuItem("Delete Appointment");
        
        viewItem.setOnAction(event -> {
            Appointment selectedAppointment = appointmentsTableView.getSelectionModel().getSelectedItem();
            if (selectedAppointment != null) {
                showAppointmentDetails(selectedAppointment);
            }
        });
        
        editItem.setOnAction(event -> {
            Appointment selectedAppointment = appointmentsTableView.getSelectionModel().getSelectedItem();
            if (selectedAppointment != null) {
                loadAppointmentForEdit(selectedAppointment);
            }
        });
        
        cancelItem.setOnAction(event -> {
            Appointment selectedAppointment = appointmentsTableView.getSelectionModel().getSelectedItem();
            if (selectedAppointment != null) {
                cancelAppointment(selectedAppointment);
            }
        });
        
        deleteItem.setOnAction(event -> {
            Appointment selectedAppointment = appointmentsTableView.getSelectionModel().getSelectedItem();
            if (selectedAppointment != null) {
                deleteAppointment(selectedAppointment);
            }
        });
        
        contextMenu.getItems().addAll(viewItem, editItem, cancelItem, deleteItem);
        
        // Only show context menu for admins or if the appointment belongs to the current patient
        appointmentsTableView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                Appointment selectedAppointment = appointmentsTableView.getSelectionModel().getSelectedItem();
                
                // Allow operations if admin or if patient and it's their appointment
                boolean canModify = Config.isAdmin() || 
                                   (Config.isPatient() && 
                                    selectedAppointment != null && 
                                    selectedAppointment.getPatientName().equals(Config.getCurrentUser().getUsername()));
                
                if (canModify && selectedAppointment != null) {
                    // Only show edit/cancel/delete if not already cancelled or completed
                    boolean isModifiable = !"Cancelled".equals(selectedAppointment.getStatus()) && 
                                         !"Completed".equals(selectedAppointment.getStatus());
                    
                    editItem.setDisable(!isModifiable);
                    cancelItem.setDisable(!isModifiable || "Cancelled".equals(selectedAppointment.getStatus()));
                    deleteItem.setDisable(!Config.isAdmin()); // Only admins can delete
                    
                    contextMenu.show(appointmentsTableView, event.getScreenX(), event.getScreenY());
                }
            } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                // Double-click to view details
                Appointment selectedAppointment = appointmentsTableView.getSelectionModel().getSelectedItem();
                if (selectedAppointment != null) {
                    showAppointmentDetails(selectedAppointment);
                }
            }
        });
    }
    
    private void showAppointmentDetails(Appointment appointment) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Appointment Details");
        alert.setHeaderText("Appointment ID: " + appointment.getAppointmentId());
        
        // Create content with all appointment details
        StringBuilder content = new StringBuilder();
        content.append("Patient: ").append(appointment.getPatientName()).append("\n");
        
        if (appointment.getPatientId() != null && !appointment.getPatientId().isEmpty()) {
            content.append("Patient ID: ").append(appointment.getPatientId()).append("\n");
        }
        
        if (appointment.getAge() > 0) {
            content.append("Age: ").append(appointment.getAge()).append("\n");
        }
        
        if (appointment.getGender() != null && !appointment.getGender().isEmpty()) {
            content.append("Gender: ").append(appointment.getGender()).append("\n");
        }
        
        if (appointment.getContactNumber() != null && !appointment.getContactNumber().isEmpty()) {
            content.append("Contact: ").append(appointment.getContactNumber()).append("\n");
        }
        
        if (appointment.getEmail() != null && !appointment.getEmail().isEmpty()) {
            content.append("Email: ").append(appointment.getEmail()).append("\n");
        }
        
        content.append("\nDate: ").append(appointment.getDate()).append("\n");
        content.append("Time: ").append(appointment.getTime()).append("\n");
        content.append("Doctor: ").append(appointment.getDoctorName()).append("\n");
        content.append("Department: ").append(appointment.getDepartment()).append("\n");
        content.append("Type: ").append(appointment.getAppointmentType()).append("\n");
        content.append("Status: ").append(appointment.getStatus()).append("\n");
        content.append("Mode: ").append(appointment.getPreferredMode()).append("\n");
        
        if (appointment.getConsultationRoom() != null && !appointment.getConsultationRoom().isEmpty()) {
            content.append("Room/Link: ").append(appointment.getConsultationRoom()).append("\n");
        }
        
        content.append("Payment: ").append(appointment.getPaymentStatus()).append("\n");
        
        if (appointment.getSymptoms() != null && !appointment.getSymptoms().isEmpty()) {
            content.append("\nSymptoms: ").append(appointment.getSymptoms()).append("\n");
        }
        
        if (appointment.getMedicalHistory() != null && !appointment.getMedicalHistory().isEmpty()) {
            content.append("Medical History: ").append(appointment.getMedicalHistory()).append("\n");
        }
        
        if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
            content.append("Notes: ").append(appointment.getNotes()).append("\n");
        }
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    private void loadAppointmentForEdit(Appointment appointment) {
        // Populate form fields with appointment data
        patientNameField.setText(appointment.getPatientName());
        patientIdField.setText(appointment.getPatientId());
        
        if (appointment.getAge() > 0) {
            ageField.setText(String.valueOf(appointment.getAge()));
        }
        
        genderComboBox.setValue(appointment.getGender());
        contactNumberField.setText(appointment.getContactNumber());
        emailField.setText(appointment.getEmail());
        
        // Parse the date string to set in DatePicker
        try {
            appointmentDatePicker.setValue(java.time.LocalDate.parse(appointment.getDate()));
        } catch (Exception e) {
            System.err.println("Error parsing date: " + e.getMessage());
        }
        
        timeComboBox.setValue(appointment.getTime());
        doctorComboBox.setValue(appointment.getDoctorName());
        departmentComboBox.setValue(appointment.getDepartment());
        appointmentTypeComboBox.setValue(appointment.getAppointmentType());
        
        symptomsField.setText(appointment.getSymptoms());
        medicalHistoryField.setText(appointment.getMedicalHistory());
        modeComboBox.setValue(appointment.getPreferredMode());
        roomField.setText(appointment.getConsultationRoom());
        paymentStatusComboBox.setValue(appointment.getPaymentStatus());
        notesField.setText(appointment.getNotes());
        
        // Change button text to indicate editing
        bookAppointmentButton.setText("Update Appointment");
        statusLabel.setText("Editing appointment " + appointment.getAppointmentId());
    }
    
    private void cancelAppointment(Appointment appointment) {
        // Confirm cancellation
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Cancel Appointment");
        confirmDialog.setHeaderText("Are you sure you want to cancel this appointment?");
        confirmDialog.setContentText("Appointment ID: " + appointment.getAppointmentId() + "\nPatient: " + appointment.getPatientName());
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Set status to cancelled
            appointment.setStatus("Cancelled");
            
            // Update in database
            boolean success = databaseService.updateAppointmentStatus(appointment, "Cancelled");
            
            if (success) {
                statusLabel.setText("Appointment cancelled successfully");
                loadAppointmentsFromDatabase(); // Refresh list
            } else {
                statusLabel.setText("Failed to cancel appointment");
            }
        }
    }
    
    private void loadAppointmentsFromDatabase() {
        try {
            // Clear current appointments
            appointments.clear();
            
            // Load appointments from Supabase
            List<Appointment> dbAppointments = databaseService.getAllAppointments();
            
            // If patient, filter to only show their appointments
            if (Config.isPatient()) {
                String patientName = Config.getCurrentUser().getUsername();
                for (Appointment appointment : dbAppointments) {
                    if (appointment.getPatientName().equals(patientName)) {
                        appointments.add(appointment);
                    }
                }
            } else {
                // Add all appointments for admin
                appointments.addAll(dbAppointments);
            }
            
            statusLabel.setText("Appointments loaded successfully");
        } catch (Exception e) {
            statusLabel.setText("Error loading appointments: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deleteAppointment(Appointment appointment) {
        try {
            // Confirm deletion
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Delete Appointment");
            confirmDialog.setHeaderText("Are you sure you want to delete this appointment?");
            confirmDialog.setContentText("This action cannot be undone!");
            
            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = databaseService.deleteAppointment(
                    appointment.getPatientName(), 
                    appointment.getDate()
                );
                
                if (success) {
                    appointments.remove(appointment);
                    statusLabel.setText("Appointment deleted successfully");
                } else {
                    statusLabel.setText("Failed to delete appointment");
                }
            }
        } catch (Exception e) {
            statusLabel.setText("Error deleting appointment: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBookAppointmentButton(ActionEvent event) {
        try {
            // Get input values - Patient Details
            String patientName = patientNameField.getText().trim();
            String patientId = patientIdField.getText().trim();
            int age = 0;
            try {
                if (!ageField.getText().trim().isEmpty()) {
                    age = Integer.parseInt(ageField.getText().trim());
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Please enter a valid age");
                return;
            }
            String gender = genderComboBox.getValue();
            String contactNumber = contactNumberField.getText().trim();
            String email = emailField.getText().trim();
            
            // Appointment Details
            if (appointmentDatePicker.getValue() == null) {
                statusLabel.setText("Please select an appointment date");
                return;
            }
            String formattedDate = appointmentDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String time = timeComboBox.getValue();
            String doctorName = doctorComboBox.getValue();
            String department = departmentComboBox.getValue();
            String appointmentType = appointmentTypeComboBox.getValue();
            
            // Additional Details
            String symptoms = symptomsField.getText().trim();
            String medicalHistory = medicalHistoryField.getText().trim();
            String preferredMode = modeComboBox.getValue();
            String consultationRoom = roomField.getText().trim();
            String paymentStatus = paymentStatusComboBox.getValue();
            String notes = notesField.getText().trim();
            
            // Validate required fields
            if (patientName.isEmpty()) {
                statusLabel.setText("Please enter a patient name");
                return;
            }
            
            if (time == null || time.isEmpty()) {
                statusLabel.setText("Please select an appointment time");
                return;
            }
            
            if (doctorName == null || doctorName.isEmpty()) {
                statusLabel.setText("Please select a doctor");
                return;
            }
            
            if (department == null || department.isEmpty()) {
                statusLabel.setText("Please select a department");
                return;
            }
            
            if (appointmentType == null || appointmentType.isEmpty()) {
                statusLabel.setText("Please select an appointment type");
                return;
            }
            
            // Create new appointment with all details
            Appointment newAppointment = new Appointment(
                patientName, patientId, age, gender, contactNumber, email, 
                formattedDate, time, doctorName, department, appointmentType,
                symptoms, medicalHistory, preferredMode, consultationRoom, 
                paymentStatus, notes
            );
            
            statusLabel.setText("Sending appointment to database...");
            
            // Save appointment to Supabase - with current user if logged in
            boolean success = databaseService.insertAppointment(newAppointment, Config.getCurrentUser());
            
            if (success) {
                // Add to local list
                appointments.add(newAppointment);
                
                // Clear input fields
                if (Config.isAdmin()) {
                    clearForm();
                } else {
                    // For patients, only clear non-personal info
                    clearAppointmentFields();
                }
            
            // Update status
            statusLabel.setText("Appointment booked successfully!");
                
                // Reset button text if it was changed for editing
                bookAppointmentButton.setText("Book Appointment");
            } else {
                statusLabel.setText("Failed to save appointment to database. Check console for details.");
                System.err.println("API Key length: " + Config.SUPABASE_API_KEY.length());
                System.err.println("Database URL: " + Config.SUPABASE_URL);
            }
            
        } catch (Exception e) {
            statusLabel.setText("Error booking appointment: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void clearForm() {
        patientNameField.clear();
        patientIdField.clear();
        ageField.clear();
        genderComboBox.getSelectionModel().clearSelection();
        contactNumberField.clear();
        emailField.clear();
        
        clearAppointmentFields();
    }
    
    private void clearAppointmentFields() {
        appointmentDatePicker.setValue(null);
        timeComboBox.getSelectionModel().clearSelection();
        doctorComboBox.getSelectionModel().clearSelection();
        departmentComboBox.getSelectionModel().clearSelection();
        appointmentTypeComboBox.getSelectionModel().clearSelection();
        
        symptomsField.clear();
        medicalHistoryField.clear();
        modeComboBox.getSelectionModel().clearSelection();
        roomField.clear();
        paymentStatusComboBox.getSelectionModel().clearSelection();
        notesField.clear();
        
        // Reset defaults
        setDefaultValues();
    }
    
    @FXML
    private void handleRefreshButton(ActionEvent event) {
        loadAppointmentsFromDatabase();
    }
    
    @FXML
    private void handleLogoutButton(ActionEvent event) {
        // Clear current user
        Config.setCurrentUser(null);
        
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent loginView = loader.load();
            
            // Get the current stage
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            
            // Set the login scene
            currentStage.setScene(new Scene(loginView, 600, 400));
            currentStage.setTitle("Doctor Appointment System - Login");
            
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error logging out: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleManageUsersButton(ActionEvent event) {
        // This would open a user management screen
        statusLabel.setText("User management functionality coming soon!");
    }
} 