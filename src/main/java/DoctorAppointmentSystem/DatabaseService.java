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
    
    public boolean insertAppointment(Appointment appointment) {
        try {
            // Check if credentials are valid
            if (SUPABASE_URL == null || SUPABASE_URL.isEmpty() || 
                SUPABASE_API_KEY == null || SUPABASE_API_KEY.isEmpty()) {
                System.err.println("Error: Supabase credentials are not properly configured");
                return false;
            }
            
            // Create JSON payload
            String json = String.format("{\"patient_name\":\"%s\",\"appointment_date\":\"%s\"}",
                appointment.getPatientName(), appointment.getDate());
            
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
            
            if (index < json.length() && json.charAt(index) == '"') {
                // String value
                int endIndex = json.indexOf('"', index + 1);
                if (endIndex != -1) {
                    return json.substring(index + 1, endIndex);
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
    
    // Simple authentication - just check local credentials for now
    public boolean authenticateUser(String email, String password) {
        // For simplicity, use the local auth instead of Supabase Auth
        return email.equals(Config.ADMIN_USERNAME) && password.equals(Config.ADMIN_PASSWORD);
    }
} 