package DoctorAppointmentSystem;

import java.io.Serializable;

public class Appointment implements Serializable {
    private String id;
    private String userId;
    private String patientName;
    private String appointmentDate;
    private String appointmentTime;
    private String doctorName;
    private String department;
    private String appointmentType;
    private String appointmentMode;
    private String symptoms;
    private String notes;
    private String status;
    private String createdAt;
    
    public Appointment() {
        // Default constructor
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    public String getAppointmentDate() {
        return appointmentDate;
    }
    
    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    
    public String getAppointmentTime() {
        return appointmentTime;
    }
    
    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }
    
    public String getDoctorName() {
        return doctorName;
    }
    
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getAppointmentType() {
        return appointmentType;
    }
    
    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }
    
    public String getAppointmentMode() {
        return appointmentMode;
    }
    
    public void setAppointmentMode(String appointmentMode) {
        this.appointmentMode = appointmentMode;
    }
    
    public String getSymptoms() {
        return symptoms;
    }
    
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(patientName).append(" - ").append(appointmentDate);
        
        if (appointmentTime != null && !appointmentTime.isEmpty()) {
            sb.append(" ").append(appointmentTime);
        }
        
        if (doctorName != null && !doctorName.isEmpty()) {
            sb.append(" with Dr. ").append(doctorName);
        }
        
        if (status != null && !status.isEmpty() && !status.equals("Scheduled")) {
            sb.append(" (").append(status).append(")");
        }
        
        return sb.toString();
    }
} 