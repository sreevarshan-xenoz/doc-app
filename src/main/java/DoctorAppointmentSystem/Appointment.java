package DoctorAppointmentSystem;

import java.util.UUID;

public class Appointment {
    // Patient Details
    private String patientName;
    private String patientId;
    private int age;
    private String gender;
    private String contactNumber;
    private String email;
    
    // Appointment Details
    private String appointmentId;
    private String date;
    private String time;
    private String doctorName;
    private String department;
    private String appointmentType;
    private String status;
    
    // Additional Fields
    private String symptoms;
    private String medicalHistory;
    private String preferredMode;
    private String consultationRoom;
    private String paymentStatus;
    private String notes;
    
    // Metadata
    private int userId;
    
    // Basic constructor for backward compatibility
    public Appointment(String patientName, String date) {
        this.patientName = patientName;
        this.date = date;
        this.appointmentId = generateAppointmentId();
        this.time = "9:00 AM"; // Default time
        this.doctorName = "Dr. Smith"; // Default doctor
        this.department = "General"; // Default department
        this.appointmentType = "Consultation"; // Default type
        this.status = "Scheduled"; // Default status
        this.preferredMode = "In-person"; // Default mode
        this.paymentStatus = "Unpaid"; // Default payment status
    }
    
    // Full constructor
    public Appointment(String patientName, String patientId, int age, String gender, 
                      String contactNumber, String email, String date, String time,
                      String doctorName, String department, String appointmentType,
                      String symptoms, String medicalHistory, String preferredMode,
                      String consultationRoom, String paymentStatus, String notes) {
        this.patientName = patientName;
        this.patientId = patientId;
        this.age = age;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.email = email;
        this.appointmentId = generateAppointmentId();
        this.date = date;
        this.time = time;
        this.doctorName = doctorName;
        this.department = department;
        this.appointmentType = appointmentType;
        this.status = "Scheduled"; // Default value
        this.symptoms = symptoms;
        this.medicalHistory = medicalHistory;
        this.preferredMode = preferredMode;
        this.consultationRoom = consultationRoom;
        this.paymentStatus = paymentStatus;
        this.notes = notes;
    }
    
    // Generate a unique appointment ID
    private String generateAppointmentId() {
        return "APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    // Getters and Setters
    
    // Patient Details
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    public String getPatientId() {
        return patientId;
    }
    
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    // Appointment Details
    public String getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    // Additional Fields
    public String getSymptoms() {
        return symptoms;
    }
    
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
    
    public String getMedicalHistory() {
        return medicalHistory;
    }
    
    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }
    
    public String getPreferredMode() {
        return preferredMode;
    }
    
    public void setPreferredMode(String preferredMode) {
        this.preferredMode = preferredMode;
    }
    
    public String getConsultationRoom() {
        return consultationRoom;
    }
    
    public void setConsultationRoom(String consultationRoom) {
        this.consultationRoom = consultationRoom;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    // Metadata
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s %s - Dr. %s (%s)", 
                            appointmentId, patientName, date, time, doctorName, status);
    }
} 