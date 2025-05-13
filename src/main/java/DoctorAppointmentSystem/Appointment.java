package DoctorAppointmentSystem;

public class Appointment {
    private String patientName;
    private String date;
    
    public Appointment(String patientName, String date) {
        this.patientName = patientName;
        this.date = date;
    }
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    @Override
    public String toString() {
        return patientName + " - " + date;
    }
} 