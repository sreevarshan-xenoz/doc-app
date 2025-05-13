package DoctorAppointmentSystem;

public class User {
    private String userId;  // Changed from int to String to hold UUID
    private String username;
    private String password;
    private String role;
    private String email;
    
    // Constructor for a full user with ID (for logged-in users)
    public User(String userId, String username, String password, String role, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
    }
    
    // Constructor for a user with just username/role (for session tracking)
    public User(String userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
    
    // Simplified constructor for new user registration
    public User(String username, String password, String role, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
    }
    
    // Getters and setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    // Helper methods
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
    
    public boolean isPatient() {
        return "patient".equalsIgnoreCase(role);
    }
    
    public boolean isDoctor() {
        return "doctor".equalsIgnoreCase(role);
    }
    
    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
} 