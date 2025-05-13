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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseService {
    private static DatabaseService instance;
    
    private final String SUPABASE_URL;
    private final String SUPABASE_API_KEY;
    private final String APPOINTMENTS_TABLE = "appointments";
    private final String USERS_TABLE = "users";
    private final String PATIENTS_TABLE = "patients";
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseService.class.getName());
    
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
                            
                            System.out.println("Extracted ID: " + idStr);
                            System.out.println("Extracted role: " + role);
                            
                            if (idStr == null || storedHash == null || salt == null) {
                                System.err.println("Failed to extract required user data from response");
                                return null;
                            }
                            
                            // Verify the password
                            if (!PasswordUtils.verifyPassword(password, storedHash, salt)) {
                                System.out.println("Password verification failed");
                                return null;
                            }
                            
                            int id = Integer.parseInt(idStr);
                            System.out.println("User authenticated successfully - ID: " + id + ", Role: " + role);
                            return new User(id, username, null, role, email); // Don't store the password in memory
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
                "{\"username\":\"%s\",\"password_hash\":\"%s\",\"password_salt\":\"%s\",\"role\":\"%s\",\"email\":\"%s\"}",
                user.getUsername(), passwordHash, salt, user.getRole(), user.getEmail());
            
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

    public Patient getPatientByUserId(int userId) {
        try {
            // Create URL with filter
            String filter = String.format("user_id=eq.%d", userId);
            URL url = new URL(SUPABASE_URL + "/rest/v1/" + PATIENTS_TABLE + "?select=*&" + filter);
            
            System.out.println("Patient fetch URL: " + url.toString());
            
            // Create connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("apikey", SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
            
            // Get response
            int responseCode = conn.getResponseCode();
            System.out.println("Patient fetch response code: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                String jsonStr = response.toString();
                System.out.println("Patient fetch response: " + jsonStr);
                
                // Parse JSON array response
                if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
                    // Empty array means no patient found
                    if (jsonStr.equals("[]")) {
                        System.out.println("No patient found for user ID: " + userId);
                        return null;
                    }
                    
                    jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
                    
                    // If we have a patient object
                    if (!jsonStr.isEmpty()) {
                        try {
                            // Extract patient data
                            int id = Integer.parseInt(extractJsonValue(jsonStr, "id"));
                            String fullName = extractJsonValue(jsonStr, "full_name");
                            String ageStr = extractJsonValue(jsonStr, "age");
                            int age = ageStr != null ? Integer.parseInt(ageStr) : 0;
                            String gender = extractJsonValue(jsonStr, "gender");
                            String contactNumber = extractJsonValue(jsonStr, "contact_number");
                            String email = extractJsonValue(jsonStr, "email");
                            String medicalHistory = extractJsonValue(jsonStr, "medical_history");
                            
                            return new Patient(id, userId, fullName, age, gender, contactNumber, email, medicalHistory);
                        } catch (Exception e) {
                            System.err.println("Error parsing patient data: " + e.getMessage());
                            System.err.println("JSON response: " + jsonStr);
                            return null;
                        }
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching patient: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Patient getPatientById(int patientId) {
        try {
            // Create URL with filter
            String filter = String.format("id=eq.%d", patientId);
            URL url = new URL(SUPABASE_URL + "/rest/v1/" + PATIENTS_TABLE + "?select=*&" + filter);
            
            // Create connection
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
                
                // Parse JSON array response
                if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
                    // Empty array means no patient found
                    if (jsonStr.equals("[]")) {
                        return null;
                    }
                    
                    jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
                    
                    // If we have a patient object
                    if (!jsonStr.isEmpty()) {
                        try {
                            // Extract patient data
                            int id = Integer.parseInt(extractJsonValue(jsonStr, "id"));
                            int userId = Integer.parseInt(extractJsonValue(jsonStr, "user_id"));
                            String fullName = extractJsonValue(jsonStr, "full_name");
                            String ageStr = extractJsonValue(jsonStr, "age");
                            int age = ageStr != null ? Integer.parseInt(ageStr) : 0;
                            String gender = extractJsonValue(jsonStr, "gender");
                            String contactNumber = extractJsonValue(jsonStr, "contact_number");
                            String email = extractJsonValue(jsonStr, "email");
                            String medicalHistory = extractJsonValue(jsonStr, "medical_history");
                            
                            return new Patient(id, userId, fullName, age, gender, contactNumber, email, medicalHistory);
                        } catch (Exception e) {
                            System.err.println("Error parsing patient data: " + e.getMessage());
                            return null;
                        }
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching patient: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean insertPatient(Patient patient) {
        try {
            // Create JSON payload
            String json = String.format(
                "{\"user_id\":%d,\"full_name\":\"%s\",\"age\":%d,\"gender\":\"%s\",\"contact_number\":\"%s\",\"email\":\"%s\",\"medical_history\":\"%s\"}",
                patient.getUserId(), patient.getFullName(), patient.getAge(), patient.getGender(),
                patient.getContactNumber(), patient.getEmail(), patient.getMedicalHistory());
            
            System.out.println("Sending patient data to Supabase: " + json);
            
            // Create connection
            URL url = new URL(SUPABASE_URL + "/rest/v1/" + PATIENTS_TABLE);
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
            System.out.println("Patient insert response code: " + responseCode);
            
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
            System.err.println("Error inserting patient: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePatient(Patient patient) {
        try {
            // Create JSON payload
            String json = String.format(
                "{\"full_name\":\"%s\",\"age\":%d,\"gender\":\"%s\",\"contact_number\":\"%s\",\"email\":\"%s\",\"medical_history\":\"%s\"}",
                patient.getFullName(), patient.getAge(), patient.getGender(),
                patient.getContactNumber(), patient.getEmail(), patient.getMedicalHistory());
            
            System.out.println("Updating patient data in Supabase: " + json);
            
            // Create URL with filter
            String filter = String.format("id=eq.%d", patient.getId());
            URL url = new URL(SUPABASE_URL + "/rest/v1/" + PATIENTS_TABLE + "?" + filter);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PATCH");
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
            System.out.println("Patient update response code: " + responseCode);
            
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
            System.err.println("Error updating patient: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Update the insertAppointment method to handle extended appointment details
    public boolean insertAppointment(Appointment appointment) {
        try {
            // Check if credentials are valid
            if (SUPABASE_URL == null || SUPABASE_URL.isEmpty() || 
                SUPABASE_API_KEY == null || SUPABASE_API_KEY.isEmpty()) {
                System.err.println("Error: Supabase credentials are not properly configured");
                return false;
            }
            
            // Create JSON payload
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            jsonBuilder.append("\"patient_name\":\"").append(appointment.getPatientName()).append("\"");
            jsonBuilder.append(",\"appointment_date\":\"").append(appointment.getDate()).append("\"");
            
            // Add optional fields if they exist
            if (appointment.getPatientId() > 0) {
                jsonBuilder.append(",\"patient_id\":").append(appointment.getPatientId());
            }
            
            if (appointment.getUserId() > 0) {
                jsonBuilder.append(",\"user_id\":").append(appointment.getUserId());
            }
            
            if (appointment.getTime() != null && !appointment.getTime().isEmpty()) {
                jsonBuilder.append(",\"appointment_time\":\"").append(appointment.getTime()).append("\"");
            }
            
            if (appointment.getDoctorName() != null && !appointment.getDoctorName().isEmpty()) {
                jsonBuilder.append(",\"doctor_name\":\"").append(appointment.getDoctorName()).append("\"");
            }
            
            if (appointment.getDepartment() != null && !appointment.getDepartment().isEmpty()) {
                jsonBuilder.append(",\"department\":\"").append(appointment.getDepartment()).append("\"");
            }
            
            if (appointment.getAppointmentType() != null && !appointment.getAppointmentType().isEmpty()) {
                jsonBuilder.append(",\"appointment_type\":\"").append(appointment.getAppointmentType()).append("\"");
            }
            
            if (appointment.getStatus() != null && !appointment.getStatus().isEmpty()) {
                jsonBuilder.append(",\"status\":\"").append(appointment.getStatus()).append("\"");
            }
            
            if (appointment.getSymptoms() != null && !appointment.getSymptoms().isEmpty()) {
                jsonBuilder.append(",\"symptoms\":\"").append(appointment.getSymptoms()).append("\"");
            }
            
            if (appointment.getAppointmentMode() != null && !appointment.getAppointmentMode().isEmpty()) {
                jsonBuilder.append(",\"appointment_mode\":\"").append(appointment.getAppointmentMode()).append("\"");
            }
            
            if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
                jsonBuilder.append(",\"notes\":\"").append(appointment.getNotes()).append("\"");
            }
            
            jsonBuilder.append("}");
            String json = jsonBuilder.toString();
            
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

    // Update the getAllAppointments method to handle extended appointment details
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
                
                String jsonStr = response.toString();
                
                // Parse JSON response (simple parsing without external libraries)
                if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
                    if (jsonStr.length() > 2) { // Not an empty array
                        // Split by objects
                        List<String> jsonObjects = splitJsonArray(jsonStr);
                        
                        for (String jsonObj : jsonObjects) {
                            try {
                                // Extract appointment data
                                String idStr = extractJsonValue(jsonObj, "id");
                                int id = idStr != null ? Integer.parseInt(idStr) : 0;
                                
                                String patientName = extractJsonValue(jsonObj, "patient_name");
                                
                                String patientIdStr = extractJsonValue(jsonObj, "patient_id");
                                int patientId = patientIdStr != null ? Integer.parseInt(patientIdStr) : 0;
                                
                                String userIdStr = extractJsonValue(jsonObj, "user_id");
                                int userId = userIdStr != null ? Integer.parseInt(userIdStr) : 0;
                                
                                String date = extractJsonValue(jsonObj, "appointment_date");
                                String time = extractJsonValue(jsonObj, "appointment_time");
                                String doctorName = extractJsonValue(jsonObj, "doctor_name");
                                String department = extractJsonValue(jsonObj, "department");
                                String appointmentType = extractJsonValue(jsonObj, "appointment_type");
                                String status = extractJsonValue(jsonObj, "status");
                                String symptoms = extractJsonValue(jsonObj, "symptoms");
                                String appointmentMode = extractJsonValue(jsonObj, "appointment_mode");
                                String notes = extractJsonValue(jsonObj, "notes");
                                
                                if (patientName != null && date != null) {
                                    Appointment appointment = new Appointment(
                                        id, patientName, patientId, userId, date, time, doctorName, 
                                        department, appointmentType, status, symptoms, appointmentMode, notes
                                    );
                                    appointments.add(appointment);
                                }
                            } catch (Exception e) {
                                System.err.println("Error parsing appointment: " + e.getMessage());
                            }
                        }
                    }
                }
            } else {
                System.err.println("Failed to fetch appointments: HTTP " + responseCode);
            }
            
        } catch (Exception e) {
            System.err.println("Error fetching appointments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return appointments;
    }

    // Helper method to split JSON array into individual objects
    private List<String> splitJsonArray(String jsonArray) {
        List<String> result = new ArrayList<>();
        if (jsonArray.startsWith("[") && jsonArray.endsWith("]")) {
            jsonArray = jsonArray.substring(1, jsonArray.length() - 1);
            
            int depth = 0;
            int startIndex = 0;
            
            for (int i = 0; i < jsonArray.length(); i++) {
                char c = jsonArray.charAt(i);
                
                if (c == '{') {
                    if (depth == 0) {
                        startIndex = i;
                    }
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        result.add(jsonArray.substring(startIndex, i + 1));
                    }
                }
            }
        }
        return result;
    }

    // Update saveAppointment method to handle new fields
    public boolean saveAppointment(Appointment appointment) {
        try {
            LOGGER.info("Saving appointment to Supabase");
            
            JsonObject appointmentJson = new JsonObject();
            appointmentJson.addProperty("patient_name", appointment.getPatientName());
            appointmentJson.addProperty("appointment_date", appointment.getAppointmentDate());
            appointmentJson.addProperty("appointment_time", appointment.getAppointmentTime());
            appointmentJson.addProperty("doctor_name", appointment.getDoctorName());
            appointmentJson.addProperty("department", appointment.getDepartment());
            appointmentJson.addProperty("appointment_type", appointment.getAppointmentType());
            appointmentJson.addProperty("appointment_mode", appointment.getAppointmentMode());
            appointmentJson.addProperty("symptoms", appointment.getSymptoms());
            appointmentJson.addProperty("notes", appointment.getNotes());
            appointmentJson.addProperty("status", "Scheduled"); // Default status for new appointments
            
            // Add user_id if available
            if (appointment.getUserId() != null && !appointment.getUserId().isEmpty()) {
                appointmentJson.addProperty("user_id", appointment.getUserId());
            }
            
            // Create HTTP request
            URL url = new URL(SUPABASE_URL + "/rest/v1/appointments");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=representation");
            conn.setDoOutput(true);
            
            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = appointmentJson.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Check response
            LOGGER.info("Checking response from Supabase for appointment save");
            int responseCode = conn.getResponseCode();
            LOGGER.info("Response code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                LOGGER.info("Appointment saved successfully");
                
                // Read response to get the created appointment ID
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    // Parse the response to get the ID
                    JsonArray appointmentsArray = JsonParser.parseString(response.toString()).getAsJsonArray();
                    if (appointmentsArray.size() > 0) {
                        JsonObject createdAppointment = appointmentsArray.get(0).getAsJsonObject();
                        String appointmentId = createdAppointment.get("id").getAsString();
                        appointment.setId(appointmentId);
                    }
                }
                
                return true;
            } else {
                LOGGER.severe("Failed to save appointment. Response code: " + responseCode);
                
                // Read error response
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    LOGGER.severe("Error response: " + response.toString());
                }
                
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving appointment", e);
            return false;
        }
    }

    // Add methods for patient profile management
    public boolean savePatientProfile(Patient patient) {
        try {
            LOGGER.info("Saving patient profile to Supabase");
            
            JsonObject patientJson = new JsonObject();
            patientJson.addProperty("user_id", patient.getUserId());
            patientJson.addProperty("full_name", patient.getFullName());
            patientJson.addProperty("date_of_birth", patient.getDateOfBirth());
            patientJson.addProperty("gender", patient.getGender());
            patientJson.addProperty("blood_group", patient.getBloodGroup());
            patientJson.addProperty("email", patient.getEmail());
            patientJson.addProperty("phone_number", patient.getPhoneNumber());
            patientJson.addProperty("address", patient.getAddress());
            patientJson.addProperty("emergency_contact", patient.getEmergencyContact());
            patientJson.addProperty("height", patient.getHeight());
            patientJson.addProperty("weight", patient.getWeight());
            patientJson.addProperty("allergies", patient.getAllergies());
            patientJson.addProperty("current_medications", patient.getCurrentMedications());
            patientJson.addProperty("medical_history", patient.getMedicalHistory());
            patientJson.addProperty("insurance_provider", patient.getInsuranceProvider());
            patientJson.addProperty("policy_number", patient.getPolicyNumber());
            
            URL url;
            HttpURLConnection conn;
            String requestMethod;
            
            // Check if this is an update or new profile
            if (patient.getId() != null && !patient.getId().isEmpty()) {
                // Update existing profile
                LOGGER.info("Updating existing patient profile with ID: " + patient.getId());
                url = new URL(SUPABASE_URL + "/rest/v1/patients?id=eq." + patient.getId());
                requestMethod = "PATCH";
            } else {
                // New profile
                LOGGER.info("Creating new patient profile");
                url = new URL(SUPABASE_URL + "/rest/v1/patients");
                requestMethod = "POST";
            }
            
            // Create HTTP request
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("apikey", SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=representation");
            conn.setDoOutput(true);
            
            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = patientJson.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Check response
            LOGGER.info("Checking response from Supabase for patient profile save");
            int responseCode = conn.getResponseCode();
            LOGGER.info("Response code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                LOGGER.info("Patient profile saved successfully");
                
                // Read response to get the created/updated patient ID
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    // Parse the response to get the ID
                    JsonArray patientsArray = JsonParser.parseString(response.toString()).getAsJsonArray();
                    if (patientsArray.size() > 0) {
                        JsonObject savedPatient = patientsArray.get(0).getAsJsonObject();
                        String patientId = savedPatient.get("id").getAsString();
                        patient.setId(patientId);
                    }
                }
                
                return true;
            } else {
                LOGGER.severe("Failed to save patient profile. Response code: " + responseCode);
                
                // Read error response
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    LOGGER.severe("Error response: " + response.toString());
                }
                
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving patient profile", e);
            return false;
        }
    }

    public Patient getPatientProfile(String userId) {
        try {
            LOGGER.info("Getting patient profile for user ID: " + userId);
            
            // Create HTTP request
            URL url = new URL(SUPABASE_URL + "/rest/v1/patients?user_id=eq." + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
            
            // Check response
            int responseCode = conn.getResponseCode();
            LOGGER.info("Response code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    // Parse response
                    JsonArray patientsArray = JsonParser.parseString(response.toString()).getAsJsonArray();
                    if (patientsArray.size() > 0) {
                        JsonObject patientJson = patientsArray.get(0).getAsJsonObject();
                        
                        Patient patient = new Patient();
                        patient.setId(getStringOrEmpty(patientJson, "id"));
                        patient.setUserId(getStringOrEmpty(patientJson, "user_id"));
                        patient.setFullName(getStringOrEmpty(patientJson, "full_name"));
                        patient.setDateOfBirth(getStringOrEmpty(patientJson, "date_of_birth"));
                        patient.setGender(getStringOrEmpty(patientJson, "gender"));
                        patient.setBloodGroup(getStringOrEmpty(patientJson, "blood_group"));
                        patient.setEmail(getStringOrEmpty(patientJson, "email"));
                        patient.setPhoneNumber(getStringOrEmpty(patientJson, "phone_number"));
                        patient.setAddress(getStringOrEmpty(patientJson, "address"));
                        patient.setEmergencyContact(getStringOrEmpty(patientJson, "emergency_contact"));
                        patient.setHeight(getStringOrEmpty(patientJson, "height"));
                        patient.setWeight(getStringOrEmpty(patientJson, "weight"));
                        patient.setAllergies(getStringOrEmpty(patientJson, "allergies"));
                        patient.setCurrentMedications(getStringOrEmpty(patientJson, "current_medications"));
                        patient.setMedicalHistory(getStringOrEmpty(patientJson, "medical_history"));
                        patient.setInsuranceProvider(getStringOrEmpty(patientJson, "insurance_provider"));
                        patient.setPolicyNumber(getStringOrEmpty(patientJson, "policy_number"));
                        
                        return patient;
                    }
                }
            } else {
                LOGGER.warning("Failed to get patient profile. Response code: " + responseCode);
            }
            
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting patient profile", e);
            return null;
        }
    }

    // Helper method to safely get string values from JsonObject
    private String getStringOrEmpty(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return "";
    }

    // Update getAppointments method to handle new fields
    public List<Appointment> getAppointments(String userId) {
        List<Appointment> appointments = new ArrayList<>();
        
        try {
            LOGGER.info("Getting appointments for user ID: " + userId);
            
            // Create HTTP request
            URL url = new URL(SUPABASE_URL + "/rest/v1/appointments?user_id=eq." + userId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
            
            // Check response
            int responseCode = conn.getResponseCode();
            LOGGER.info("Response code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    // Parse response
                    JsonArray appointmentsArray = JsonParser.parseString(response.toString()).getAsJsonArray();
                    for (JsonElement element : appointmentsArray) {
                        JsonObject appointmentJson = element.getAsJsonObject();
                        
                        Appointment appointment = new Appointment();
                        appointment.setId(getStringOrEmpty(appointmentJson, "id"));
                        appointment.setUserId(getStringOrEmpty(appointmentJson, "user_id"));
                        appointment.setPatientName(getStringOrEmpty(appointmentJson, "patient_name"));
                        appointment.setAppointmentDate(getStringOrEmpty(appointmentJson, "appointment_date"));
                        appointment.setAppointmentTime(getStringOrEmpty(appointmentJson, "appointment_time"));
                        appointment.setDoctorName(getStringOrEmpty(appointmentJson, "doctor_name"));
                        appointment.setDepartment(getStringOrEmpty(appointmentJson, "department"));
                        appointment.setAppointmentType(getStringOrEmpty(appointmentJson, "appointment_type"));
                        appointment.setAppointmentMode(getStringOrEmpty(appointmentJson, "appointment_mode"));
                        appointment.setSymptoms(getStringOrEmpty(appointmentJson, "symptoms"));
                        appointment.setNotes(getStringOrEmpty(appointmentJson, "notes"));
                        appointment.setStatus(getStringOrEmpty(appointmentJson, "status"));
                        appointment.setCreatedAt(getStringOrEmpty(appointmentJson, "created_at"));
                        
                        appointments.add(appointment);
                    }
                }
            } else {
                LOGGER.warning("Failed to get appointments. Response code: " + responseCode);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting appointments", e);
        }
        
        return appointments;
    }

    public boolean cancelAppointment(String appointmentId) {
        try {
            LOGGER.info("Cancelling appointment with ID: " + appointmentId);
            
            // Create JSON with updated status
            JsonObject updateJson = new JsonObject();
            updateJson.addProperty("status", "Cancelled");
            
            // Create HTTP request
            URL url = new URL(SUPABASE_URL + "/rest/v1/appointments?id=eq." + appointmentId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PATCH");
            conn.setRequestProperty("apikey", SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=representation");
            conn.setDoOutput(true);
            
            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = updateJson.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Check response
            LOGGER.info("Checking response from Supabase for appointment cancellation");
            int responseCode = conn.getResponseCode();
            LOGGER.info("Response code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                LOGGER.info("Appointment cancelled successfully");
                return true;
            } else {
                LOGGER.severe("Failed to cancel appointment. Response code: " + responseCode);
                
                // Read error response
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    LOGGER.severe("Error response: " + response.toString());
                }
                
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error cancelling appointment", e);
            return false;
        }
    }
    
    // Add test connection method
    public boolean testConnection() {
        try {
            // Simply try to fetch a small amount of data to test the connection
            URL url = new URL(SUPABASE_URL + "/rest/v1/appointments?limit=1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", SUPABASE_API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_API_KEY);
            
            int responseCode = conn.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Connection test failed", e);
            return false;
        }
    }
} 