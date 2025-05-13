package DoctorAppointmentSystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Insets;
import javafx.scene.Node;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.time.LocalDate;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {
    
    @FXML
    private TextField patientNameField;
    
    @FXML
    private DatePicker appointmentDatePicker;
    
    @FXML
    private Button bookAppointmentButton;
    
    @FXML
    private ListView<Appointment> appointmentsListView;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private VBox adminControls;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Button createAppointmentButton;
    
    @FXML
    private Button manageProfileButton;
    
    @FXML
    private Button viewCalendarButton;
    
    @FXML
    private Label dbStatusLabel;
    
    @FXML
    private Label userRoleLabel;
    
    @FXML
    private Label lastLoginLabel;
    
    @FXML
    private TableView<Appointment> appointmentsTable;
    
    @FXML
    private TableColumn<Appointment, String> patientColumn;
    
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
    private ComboBox<String> filterComboBox;
    
    @FXML
    private Button viewDetailsButton;
    
    @FXML
    private Button cancelAppointmentButton;
    
    // Observable list to store appointments
    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    
    // Database service
    private DatabaseService databaseService;
    
    // Update class variables for new UI components
    private User currentUser;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            databaseService = new DatabaseService();
            
            // Setup filter options
            ObservableList<String> filterOptions = FXCollections.observableArrayList(
                    "All Appointments", "Upcoming", "Past", "Cancelled"
            );
            filterComboBox.setItems(filterOptions);
            filterComboBox.setValue("All Appointments");
            
            // Setup table columns
            setupTableColumns();
            
            // Initial db status
            updateDatabaseStatus();
            
        } catch (Exception e) {
            System.out.println("Error initializing dashboard: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not initialize dashboard. Please try again later.");
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
        MenuItem deleteItem = new MenuItem("Delete Appointment");
        
        deleteItem.setOnAction(event -> {
            Appointment selectedAppointment = appointmentsListView.getSelectionModel().getSelectedItem();
            if (selectedAppointment != null) {
                deleteAppointment(selectedAppointment);
            }
        });
        
        contextMenu.getItems().add(deleteItem);
        
        // Only show delete context menu for admins or if the appointment belongs to the current patient
        appointmentsListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                Appointment selectedAppointment = appointmentsListView.getSelectionModel().getSelectedItem();
                
                // Allow delete if admin or if patient and it's their appointment
                boolean canDelete = Config.isAdmin() || 
                                   (Config.isPatient() && 
                                    selectedAppointment != null && 
                                    selectedAppointment.getPatientName().equals(Config.getCurrentUser().getUsername()));
                
                if (canDelete) {
                    contextMenu.show(appointmentsListView, event.getScreenX(), event.getScreenY());
                }
            }
        });
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
        } catch (Exception e) {
            statusLabel.setText("Error deleting appointment: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleBookAppointmentButton(ActionEvent event) {
        try {
            // Get input values
            String patientName = patientNameField.getText().trim();
            
            // Validate patient name
            if (patientName.isEmpty()) {
                statusLabel.setText("Please enter a patient name");
                return;
            }
            
            // Validate date selection
            if (appointmentDatePicker.getValue() == null) {
                statusLabel.setText("Please select an appointment date");
                return;
            }
            
            // Format the selected date
            String formattedDate = appointmentDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            // Create new appointment
            Appointment newAppointment = new Appointment(patientName, formattedDate);
            
            statusLabel.setText("Sending appointment to database...");
            
            // Save appointment to Supabase - with current user if logged in
            boolean success = databaseService.insertAppointment(newAppointment, Config.getCurrentUser());
            
            if (success) {
                // Add to local list
                appointments.add(newAppointment);
                
                // Clear input fields
                if (Config.isAdmin()) {
                    patientNameField.clear();
                }
                appointmentDatePicker.setValue(null);
                
                // Update status
                statusLabel.setText("Appointment booked successfully!");
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
    
    private void setupTableColumns() {
        patientColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPatientName()));
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAppointmentDate()));
        timeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAppointmentTime()));
        doctorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDoctorName()));
        departmentColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDepartment()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAppointmentType()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Add status color
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Scheduled".equals(item)) {
                        setStyle("-fx-text-fill: #4285f4;"); // Blue
                    } else if ("Completed".equals(item)) {
                        setStyle("-fx-text-fill: #0f9d58;"); // Green
                    } else if ("Cancelled".equals(item)) {
                        setStyle("-fx-text-fill: #ea4335;"); // Red
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        // Add row selection listener
        appointmentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        viewDetailsButton.setDisable(false);
                        cancelAppointmentButton.setDisable("Cancelled".equals(newSelection.getStatus()) || 
                                                          "Completed".equals(newSelection.getStatus()));
                    } else {
                        viewDetailsButton.setDisable(true);
                        cancelAppointmentButton.setDisable(true);
                    }
                });
        
        // Initially disable detail/cancel buttons
        viewDetailsButton.setDisable(true);
        cancelAppointmentButton.setDisable(true);
    }
    
    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername());
            userRoleLabel.setText(user.getRole());
            lastLoginLabel.setText(LocalDate.now().toString());
            
            // Load appointments for this user
            loadUserAppointments();
        }
    }
    
    private void loadUserAppointments() {
        if (currentUser == null) {
            return;
        }
        
        try {
            // Get appointments for the current user
            List<Appointment> userAppointments = databaseService.getAppointments(currentUser.getId());
            
            // Apply filter
            String filterValue = filterComboBox.getValue();
            List<Appointment> filteredAppointments = filterAppointments(userAppointments, filterValue);
            
            // Update table
            appointmentsTable.setItems(FXCollections.observableArrayList(filteredAppointments));
            
        } catch (Exception e) {
            System.out.println("Error loading appointments: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not load appointments. Please try again later.");
        }
    }
    
    private List<Appointment> filterAppointments(List<Appointment> appointments, String filter) {
        if ("All Appointments".equals(filter)) {
            return appointments;
        }
        
        LocalDate today = LocalDate.now();
        
        return appointments.stream().filter(appointment -> {
            if ("Upcoming".equals(filter)) {
                LocalDate appointmentDate = LocalDate.parse(appointment.getAppointmentDate());
                return appointmentDate.isEqual(today) || appointmentDate.isAfter(today) && 
                       !"Cancelled".equals(appointment.getStatus());
            } else if ("Past".equals(filter)) {
                LocalDate appointmentDate = LocalDate.parse(appointment.getAppointmentDate());
                return appointmentDate.isBefore(today) || 
                       "Completed".equals(appointment.getStatus());
            } else if ("Cancelled".equals(filter)) {
                return "Cancelled".equals(appointment.getStatus());
            }
            return true;
        }).collect(Collectors.toList());
    }
    
    private void updateDatabaseStatus() {
        // Check database connection
        boolean isConnected = databaseService.testConnection();
        if (isConnected) {
            dbStatusLabel.setText("Connected");
            dbStatusLabel.setStyle("-fx-text-fill: #0f9d58;"); // Green
        } else {
            dbStatusLabel.setText("Disconnected");
            dbStatusLabel.setStyle("-fx-text-fill: #ea4335;"); // Red
        }
    }
    
    @FXML
    void handleCreateAppointment(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/appointment_form.fxml"));
            Parent root = loader.load();
            
            // Pass user to appointment form
            AppointmentFormController appointmentController = loader.getController();
            if (currentUser != null) {
                appointmentController.setUser(currentUser);
            }
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.out.println("Error loading appointment form: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open appointment form. Please try again later.");
        }
    }
    
    @FXML
    void handleManageProfile(ActionEvent event) {
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
            System.out.println("Error loading patient profile: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open patient profile. Please try again later.");
        }
    }
    
    @FXML
    void handleViewCalendar(ActionEvent event) {
        showAlert("Feature Coming Soon", "Calendar view will be available in a future update.");
    }
    
    @FXML
    void handleFilterChange(ActionEvent event) {
        loadUserAppointments();
    }
    
    @FXML
    void handleRefresh(ActionEvent event) {
        loadUserAppointments();
        updateDatabaseStatus();
    }
    
    @FXML
    void handleViewDetails(ActionEvent event) {
        Appointment selectedAppointment = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment != null) {
            showAppointmentDetails(selectedAppointment);
        }
    }
    
    private void showAppointmentDetails(Appointment appointment) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Appointment Details");
        alert.setHeaderText("Appointment on " + appointment.getAppointmentDate() + " at " + appointment.getAppointmentTime());
        
        // Create content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Add details
        int row = 0;
        grid.add(new Label("Patient:"), 0, row);
        grid.add(new Label(appointment.getPatientName()), 1, row++);
        
        grid.add(new Label("Doctor:"), 0, row);
        grid.add(new Label(appointment.getDoctorName()), 1, row++);
        
        grid.add(new Label("Department:"), 0, row);
        grid.add(new Label(appointment.getDepartment()), 1, row++);
        
        grid.add(new Label("Type:"), 0, row);
        grid.add(new Label(appointment.getAppointmentType()), 1, row++);
        
        grid.add(new Label("Mode:"), 0, row);
        grid.add(new Label(appointment.getAppointmentMode()), 1, row++);
        
        grid.add(new Label("Status:"), 0, row);
        grid.add(new Label(appointment.getStatus()), 1, row++);
        
        if (appointment.getSymptoms() != null && !appointment.getSymptoms().isEmpty()) {
            grid.add(new Label("Symptoms:"), 0, row);
            grid.add(new Label(appointment.getSymptoms()), 1, row++);
        }
        
        if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
            grid.add(new Label("Notes:"), 0, row);
            grid.add(new Label(appointment.getNotes()), 1, row++);
        }
        
        alert.getDialogPane().setContent(grid);
        alert.showAndWait();
    }
    
    @FXML
    void handleCancelAppointment(ActionEvent event) {
        Appointment selectedAppointment = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Cancel Appointment");
            confirmAlert.setHeaderText("Are you sure you want to cancel this appointment?");
            confirmAlert.setContentText("Appointment on " + selectedAppointment.getAppointmentDate() + 
                                       " at " + selectedAppointment.getAppointmentTime() + 
                                       " with Dr. " + selectedAppointment.getDoctorName());
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = databaseService.cancelAppointment(selectedAppointment.getId());
                if (success) {
                    showAlert("Success", "Appointment cancelled successfully.");
                    loadUserAppointments();
                } else {
                    showAlert("Error", "Failed to cancel appointment. Please try again later.");
                }
            }
        }
    }
    
    @FXML
    void handleLogout(ActionEvent event) {
        try {
            // Navigate back to login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.out.println("Error logging out: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not log out. Please try again.");
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 