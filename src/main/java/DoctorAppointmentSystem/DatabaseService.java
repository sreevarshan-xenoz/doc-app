package DoctorAppointmentSystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DatabaseService {
    private static DatabaseService instance;
    
    private final String SUPABASE_URL;
    private final String SUPABASE_API_KEY;
    private final String APPOINTMENTS_TABLE = "appointments";
    private final String USERS_TABLE = "users";
    
    private DatabaseService(String supabaseUrl, String supabaseApiKey) {
        this.SUPABASE_URL = supabaseUrl;
        this.SUPABASE_API_KEY = supabaseApiKey;
        
        // Debug credentials during initialization
        System.out.println("DatabaseService initialized with:");
        System.out.println("URL: " + supabaseUrl);
        System.out.println("API key length: " + (supabaseApiKey != null ? supabaseApiKey.length() : 0));
    }
    
    public static synchronized DatabaseService getInstance(String supabaseUrl, String supabaseApiKey) {
        if (instance == null) {
            instance = new DatabaseService(supabaseUrl, supabaseApiKey);
        }
        return instance;
    }
    
    public boolean insertAppointment(Appointment appointment, User user) {
        try {
            // Check if credentials are valid
            if (SUPABASE_URL == null || SUPABASE_URL.isEmpty() || 
                SUPABASE_API_KEY == null || SUPABASE_API_KEY.isEmpty()) {
                System.err.println("Error: Supabase credentials are not properly configured");
                return false;
            }
            
            // Create JSON payload
            String json;
            
            if (user != null) {
                json = String.format(
                    "{\"patient_name\":\"%s\",\"appointment_date\":\"%s\",\"user_id\":%d}",
                    appointment.getPatientName(), appointment.getDate(), user.getId());
            } else {
                json = String.format(
                    "{\"patient_name\":\"%s\",\"appointment_date\":\"%s\"}",
                    appointment.getPatientName(), appointment.getDate());
            }
            
            System.out.println("Sending to Supabase: " + json);
            System.out.println("URL: " + SUPABASE_URL + "/rest/v1/" + APPOINTMENTS_TABLE);
            
            // Create connection
            URL url = new URL(SUPABASE_URL + "/rest/v1/" + APPOINTMENTS_TABLE);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("apikey", SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
            conn.setRequestProperty("Prefer", "return=minimal");
            conn.setDoOutput(true);
            
            // Send data
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Get response
            int responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);
            
            // Read error response if available
            if (responseCode >= 400) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println("Error response: " + response.toString());
                }
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println("Success response: " + response.toString());
                }
            }
            
            return responseCode >= 200 && responseCode < 300;
            
        } catch (Exception e) {
            System.err.println("Error inserting appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean insertAppointment(Appointment appointment) {
        return insertAppointment(appointment, null);
    }
    
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        
        try {
            // Create connection
            URL url = new URL(SUPABASE_URL + "/rest/v1/" + APPOINTMENTS_TABLE + "?select=*");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("apikey", SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
            
            // Get response
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // Parse JSON response (simple parsing without external libraries)
                String jsonStr = response.toString();
                
                // Very simple JSON array parser
                jsonStr = jsonStr.trim();
                if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
                    jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
                    
                    // Split by objects
                    if (!jsonStr.isEmpty()) {
                        int depth = 0;
                        int start = 0;
                        
                        for (int i = 0; i < jsonStr.length(); i++) {
                            char c = jsonStr.charAt(i);
                            
                            if (c == '{') {
                                if (depth == 0) {
                                    start = i;
                                }
                                depth++;
                            } else if (c == '}') {
                                depth--;
                                if (depth == 0) {
                                    String obj = jsonStr.substring(start, i + 1);
                                    parseAppointment(obj, appointments);
                                }
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error fetching appointments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return appointments;
    }
    
    private void parseAppointment(String jsonObj, List<Appointment> appointments) {
        try {
            // Simple extraction of values
            String patientName = extractJsonValue(jsonObj, "patient_name");
            String appointmentDate = extractJsonValue(jsonObj, "appointment_date");
            
            if (patientName != null && appointmentDate != null) {
                Appointment appointment = new Appointment(patientName, appointmentDate);
                appointments.add(appointment);
            }
        } catch (Exception e) {
            System.err.println("Error parsing appointment: " + e.getMessage());
        }
    }
    
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int index = json.indexOf(searchKey);
        
        if (index != -1) {
            index += searchKey.length();
            
            // Skip whitespace
            while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
                index++;
            }
            
            if (index < json.length()) {
                if (json.charAt(index) == '"') {
                    // String value
                    int endIndex = json.indexOf('"', index + 1);
                    if (endIndex != -1) {
                        return json.substring(index + 1, endIndex);
                    }
                } else if (Character.isDigit(json.charAt(index)) || json.charAt(index) == '-') {
                    // Numeric value
                    int endIndex = index;
                    while (endIndex < json.length() && 
                          (Character.isDigit(json.charAt(endIndex)) || 
                           json.charAt(endIndex) == '.' || 
                           json.charAt(endIndex) == '-' ||
                           json.charAt(endIndex) == 'e' ||
                           json.charAt(endIndex) == 'E')) {
                        endIndex++;
                    }
                    
                    if (endIndex > index) {
                        // Check if we have a comma or closing brace after
                        if (endIndex == json.length() || 
                            json.charAt(endIndex) == ',' || 
                            json.charAt(endIndex) == '}') {
                            return json.substring(index, endIndex);
                        }
                    }
                } else if (json.charAt(index) == 'n' && 
                          index + 4 <= json.length() && 
                          json.substring(index, index + 4).equals("null")) {
                    // Null value
                    return null;
                }
            }
        }
        
        return null;
    }
    
    public boolean deleteAppointment(String patientName, String date) {
        try {
            // Create connection - using URL parameters for filtering
            String filter = String.format("patient_name=eq.%s&appointment_date=eq.%s", 
                                        patientName, date);
            URL url = new URL(SUPABASE_URL + "/rest/v1/" + APPOINTMENTS_TABLE + "?" + filter);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("apikey", SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
            
            // Get response
            int responseCode = conn.getResponseCode();
            return responseCode == 200 || responseCode == 204;
            
        } catch (Exception e) {
            System.err.println("Error deleting appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public User authenticateUser(String username, String password) {
        try {
            // Check if credentials are valid
            if (SUPABASE_URL == null || SUPABASE_URL.isEmpty() || 
                SUPABASE_API_KEY == null || SUPABASE_API_KEY.isEmpty()) {
                System.err.println("Error: Supabase credentials are not properly configured");
                return null;
            }
            
            System.out.println("Attempting to authenticate user: " + username);
            
            // First, get the user by username to retrieve the salt
            String filter = String.format("username=eq.%s", username);
            URL url = new URL(SUPABASE_URL + "/rest/v1/" + USERS_TABLE + "?select=*&" + filter);
            
            System.out.println("Auth URL: " + url.toString());
            
            // Create connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("apikey", SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
            
            // Get response
            int responseCode = conn.getResponseCode();
            System.out.println("Auth response code: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                String jsonStr = response.toString();
                System.out.println("Auth response: " + jsonStr);
                
                // Parse JSON array response
                if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
                    // Empty array means no user found
                    if (jsonStr.equals("[]")) {
                        System.out.println("No user found with the provided username");
                        return null;
                    }
                    
                    jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
                    
                    // If we have a user object
                    if (!jsonStr.isEmpty()) {
                        try {
                            // Extract user data
                            String idStr = extractJsonValue(jsonStr, "id");
                            String role = extractJsonValue(jsonStr, "role");
                            String email = extractJsonValue(jsonStr, "email");
                            String storedHash = extractJsonValue(jsonStr, "password_hash");
                            String salt = extractJsonValue(jsonStr, "password_salt");
                            String verifiedStr = extractJsonValue(jsonStr, "verified");
                            boolean verified = "true".equalsIgnoreCase(verifiedStr);
                            
                            System.out.println("Extracted ID: " + idStr);
                            System.out.println("Extracted role: " + role);
                            System.out.println("Verified: " + verified);
                            
                            if (idStr == null || storedHash == null || salt == null) {
                                System.err.println("Failed to extract required user data from response");
                                return null;
                            }
                            
                            // Verify the password
                            if (!PasswordUtils.verifyPassword(password, storedHash, salt)) {
                                System.out.println("Password verification failed");
                                return null;
                            }
                            
                            // Check if user is verified
                            if (!verified) {
                                System.out.println("User is not verified");
                                // We'll return the user but with a flag indicating they're not verified
                            }
                            
                            int id = Integer.parseInt(idStr);
                            System.out.println("User authenticated successfully - ID: " + id + ", Role: " + role);
                            return new User(id, username, null, role, email, verified); // Include verification status
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing user ID: " + e.getMessage());
                            System.err.println("JSON response: " + jsonStr);
                            return null;
                        }
                    }
                }
                
                // If we get here, the response format was unexpected
                System.err.println("Unexpected response format: " + jsonStr);
            } else {
                System.out.println("Authentication failed with response code: " + responseCode);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println("Error response: " + response.toString());
                } catch (Exception e) {
                    System.err.println("Failed to read error stream: " + e.getMessage());
                }
            }
            
            // Authentication failed
            return null;
        } catch (Exception e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean registerUser(User user) {
        try {
            // Generate a salt and hash the password
            String salt = PasswordUtils.generateSalt();
            String passwordHash = PasswordUtils.hashPassword(user.getPassword(), salt);
            
            // Create JSON payload with hashed password and salt
            String json = String.format(
                "{\"username\":\"%s\",\"password_hash\":\"%s\",\"password_salt\":\"%s\",\"role\":\"%s\",\"email\":\"%s\",\"verified\":%b}",
                user.getUsername(), passwordHash, salt, user.getRole(), user.getEmail(), user.isVerified());
            
            System.out.println("Registering new user: " + user.getUsername() + " with role: " + user.getRole());
            
            // Create connection
            URL url = new URL(SUPABASE_URL + "/rest/v1/" + USERS_TABLE);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("apikey", SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
            conn.setRequestProperty("Prefer", "return=minimal");
            conn.setDoOutput(true);
            
            // Send data
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Get response
            int responseCode = conn.getResponseCode();
            System.out.println("Register response code: " + responseCode);
            
            if (responseCode >= 400) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println("Error response: " + response.toString());
                }
            }
            
            return responseCode >= 200 && responseCode < 300;
        } catch (Exception e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Get patient-specific appointments
    public List<Appointment> getPatientAppointments(int userId) {
        List<Appointment> appointments = new ArrayList<>();
        
        try {
            // Create connection with filter for user_id
            String filter = String.format("user_id=eq.%d", userId);
            URL url = new URL(SUPABASE_URL + "/rest/v1/" + APPOINTMENTS_TABLE + "?select=*&" + filter);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("apikey", SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
            
            // Get response
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                String jsonStr = response.toString();
                
                // Parse JSON response
                if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
                    if (!jsonStr.equals("[]")) {
                        jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
                        
                        // Split by objects
                        int depth = 0;
                        int start = 0;
                        
                        for (int i = 0; i < jsonStr.length(); i++) {
                            char c = jsonStr.charAt(i);
                            
                            if (c == '{') {
                                if (depth == 0) {
                                    start = i;
                                }
                                depth++;
                            } else if (c == '}') {
                                depth--;
                                if (depth == 0) {
                                    String obj = jsonStr.substring(start, i + 1);
                                    parseAppointment(obj, appointments);
                                }
                            }
                        }
                    }
                }
            } else {
                System.err.println("Error fetching patient appointments: " + responseCode);
            }
            
        } catch (Exception e) {
            System.err.println("Error fetching patient appointments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return appointments;
    }
} 