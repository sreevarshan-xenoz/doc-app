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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.control.Tab;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Insets;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

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
    private StackPane calendarContainer;
    
    @FXML
    private StackPane analyticsContainer;
    
    @FXML
    private Tab analyticsTab;
    
    @FXML
    private BorderPane rootPane;
    
    // Observable list to store appointments
    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    
    // Database service
    private DatabaseService databaseService;
    
    private CalendarView calendarView;
    
    private AdminDashboardView adminDashboardView;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize database service
        databaseService = DatabaseService.getInstance(
            Config.SUPABASE_URL,
            Config.SUPABASE_API_KEY
        );
        
        // Set the appointments list as the data source for the ListView
        appointmentsListView.setItems(appointments);
        
        // Initialize Calendar View
        initializeCalendarView();
        
        // Initialize Admin Dashboard View
        initializeAdminDashboardView();
        
        // Configure UI based on user role
        configureUIForUserRole();
        
        // Apply responsive design
        setupResponsiveDesign();
        
        // Load appointments from the database
        loadAppointmentsFromDatabase();
        
        // Set up context menu for appointment deletion
        setupContextMenu();
    }
    
    private void initializeAdminDashboardView() {
        adminDashboardView = new AdminDashboardView();
        analyticsContainer.getChildren().add(adminDashboardView);
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
                
                // Hide analytics tab for patients
                analyticsTab.setDisable(true);
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
    
    private void initializeCalendarView() {
        calendarView = new CalendarView();
        calendarContainer.getChildren().add(calendarView);
        
        // Set date selection handler
        calendarView.setOnDateSelected(this::handleDateSelected);
    }
    
    private void handleDateSelected(LocalDate date) {
        // Set the selected date in the date picker
        appointmentDatePicker.setValue(date);
        
        // Filter appointments for the selected date
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Highlight appointments for the selected date
        appointmentsListView.getItems().forEach(appointment -> {
            if (appointment.getDate().equals(dateStr)) {
                appointmentsListView.getSelectionModel().select(appointment);
                appointmentsListView.scrollTo(appointment);
            }
        });
        
        // Show a notification of how many appointments are on this day
        long count = appointments.stream()
                .filter(a -> a.getDate().equals(dateStr))
                .count();
        
        if (count > 0) {
            statusLabel.setText(count + " appointment(s) scheduled for " + dateStr);
        } else {
            statusLabel.setText("No appointments scheduled for " + dateStr);
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
            
            // Update the calendar view with the appointments
            calendarView.setAppointments(appointments);
            
            // Update admin dashboard with appointments
            adminDashboardView.updateDashboard(appointments);
            
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
    
    private void setupResponsiveDesign() {
        // Get the screen size
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        
        // Calculate appropriate sizes based on screen width
        if (rootPane.getScene() != null) {
            rootPane.getScene().widthProperty().addListener((obs, oldVal, newVal) -> {
                adjustLayoutForWidth(newVal.doubleValue());
            });
            
            // Initial adjustment
            rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    adjustLayoutForWidth(newScene.getWidth());
                    newScene.getWindow().setOnShown(e -> adjustLayoutForWidth(newScene.getWidth()));
                }
            });
        }
    }
    
    private void adjustLayoutForWidth(double width) {
        // For smaller screens
        if (width < 800) {
            // Adjust the booking form width
            BorderPane.setMargin(rootPane.getLeft(), new Insets(10, 10, 10, 10));
            
            // Make calendar cells smaller
            if (calendarView != null) {
                calendarView.setStyle("-fx-font-size: 10px;");
            }
            
        } else {
            // Default margins for larger screens
            BorderPane.setMargin(rootPane.getLeft(), new Insets(20, 0, 20, 20));
            
            // Reset calendar cell size
            if (calendarView != null) {
                calendarView.setStyle("-fx-font-size: 12px;");
            }
        }
    }
} 