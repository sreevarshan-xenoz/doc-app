package DoctorAppointmentSystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;

import java.net.URL;
import java.time.format.DateTimeFormatter;
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
        
        // Set the appointments list as the data source for the ListView
        appointmentsListView.setItems(appointments);
        
        // Load appointments from the database
        loadAppointmentsFromDatabase();
        
        // Set up context menu for appointment deletion
        setupContextMenu();
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
        
        // Show context menu on right-click
        appointmentsListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(appointmentsListView, event.getScreenX(), event.getScreenY());
            }
        });
    }
    
    private void loadAppointmentsFromDatabase() {
        try {
            // Clear current appointments
            appointments.clear();
            
            // Load appointments from Supabase
            List<Appointment> dbAppointments = databaseService.getAllAppointments();
            
            // Add to observable list
            appointments.addAll(dbAppointments);
            
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
            
            // Save appointment to Supabase
            boolean success = databaseService.insertAppointment(newAppointment);
            
            if (success) {
                // Add to local list
                appointments.add(newAppointment);
                
                // Clear input fields
                patientNameField.clear();
                appointmentDatePicker.setValue(null);
                
                // Update status
                statusLabel.setText("Appointment booked successfully!");
            } else {
                statusLabel.setText("Failed to save appointment to database");
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
} 