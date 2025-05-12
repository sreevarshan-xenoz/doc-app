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

import java.net.URL;
import java.time.format.DateTimeFormatter;
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
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set the appointments list as the data source for the ListView
        appointmentsListView.setItems(appointments);
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
            
            // Create new appointment and add to the list
            Appointment newAppointment = new Appointment(patientName, formattedDate);
            appointments.add(newAppointment);
            
            // Clear input fields
            patientNameField.clear();
            appointmentDatePicker.setValue(null);
            
            // Update status
            statusLabel.setText("Appointment booked successfully!");
            
        } catch (Exception e) {
            statusLabel.setText("Error booking appointment: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 