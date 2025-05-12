module DoctorAppointmentSystem {
    requires javafx.controls;
    requires javafx.fxml;
    
    opens DoctorAppointmentSystem to javafx.fxml;
    exports DoctorAppointmentSystem;
} 