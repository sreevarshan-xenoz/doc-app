package DoctorAppointmentSystem;

import io.github.cdimascio.dotenv.Dotenv;
import io.supabase.BuildConfig;
import io.supabase.GoTrue;
import io.supabase.GoTrueApi;
import io.supabase.GoTrueClient;
import io.supabase.SupabaseClient;
import io.supabase.data.auth.AuthSignInWithPasswordCredentials;
import io.supabase.data.auth.AuthSignUpWithPasswordCredentials;
import io.supabase.data.auth.OAuth;
import io.supabase.data.auth.TokenResponse;
import io.supabase.data.auth.UserAttributes;
import io.supabase.data.auth.UserIdentity;
import io.supabase.data.auth.UserSession;
import io.supabase.data.auth.User;
import io.supabase.errors.SupabaseException;
import io.supabase.exceptions.OAuthException;
import io.supabase.exceptions.RestException;
import io.supabase.functions.Function;
import io.supabase.gotrue.GoTrueClientOptions;
import io.supabase.postgrest.PostgrestDefaultClient;
import io.supabase.postgrest.PostgrestFilterBuilder;
import io.supabase.postgrest.http.HttpURLConnectionImpl;
import io.supabase.postgrest.builder.PostgrestRawBuilder;
import io.supabase.storage.Storage;
import io.supabase.supabase.CreateClientOptions;
import io.supabase.supabase.SupabaseDefault;
import kotlinx.serialization.json.Json;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonObject;
import kotlinx.serialization.json.JsonPrimitive;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class DatabaseService {

    private static DatabaseService instance;
    private String supabaseUrl;
    private String supabaseKey;
    private io.supabase.SupabaseClient client;

    // Private constructor for singleton pattern
    private DatabaseService(String url, String key) {
        this.supabaseUrl = url;
        this.supabaseKey = key;
    }

    // Singleton getter
    public static DatabaseService getInstance(String url, String key) {
        if (instance == null) {
            instance = new DatabaseService(url, key);
        }
        return instance;
    }

    // Method to authenticate a user
    public DoctorAppointmentSystem.User authenticateUser(String username, String password) {
        try {
            // Create HTTP client
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String apiUrl = supabaseUrl + "/auth/v1/token?grant_type=password";

            // Create HTTP POST request
            HttpPost httpPost = new HttpPost(apiUrl);
            httpPost.setHeader("apikey", supabaseKey);
            httpPost.setHeader("Content-Type", "application/json");

            // Create request body with credentials
            JSONObject requestBody = new JSONObject();
            requestBody.put("email", username);
            requestBody.put("password", password);
            StringEntity entity = new StringEntity(requestBody.toString());
            httpPost.setEntity(entity);

            // Execute request
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            // Read response
            String responseBody = EntityUtils.toString(response.getEntity());
            httpClient.close();

            // Check for successful login
            if (statusCode == 200) {
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONObject user = jsonResponse.getJSONObject("user");

                // Get user ID and other details
                String userId = user.getString("id");
                String email = user.getString("email");
                String role = getUserRole(userId);

                return new DoctorAppointmentSystem.User(userId, email, role);
            } else {
                System.err.println("Authentication failed. Status: " + statusCode);
                System.err.println("Response: " + responseBody);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to get user role
    private String getUserRole(String userId) {
        try {
            // Create HTTP client
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String apiUrl = supabaseUrl + "/rest/v1/users?id=eq." + userId;

            // Create HTTP GET request
            HttpGet httpGet = new HttpGet(apiUrl);
            httpGet.setHeader("apikey", supabaseKey);
            httpGet.setHeader("Authorization", "Bearer " + supabaseKey);

            // Execute request
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity());
            httpClient.close();

            // Parse response
            JSONArray jsonArray = new JSONArray(responseBody);
            if (jsonArray.length() > 0) {
                JSONObject user = jsonArray.getJSONObject(0);
                return user.optString("role", "patient"); // Default to patient if role not found
            }
            return "patient"; // Default role
        } catch (Exception e) {
            System.err.println("Error getting user role: " + e.getMessage());
            return "patient"; // Default to patient on error
        }
    }

    // Insert an appointment to the Supabase database
    public boolean insertAppointment(Appointment appointment, DoctorAppointmentSystem.User currentUser) {
        try {
            // Generate a unique appointment ID
            String appointmentId = UUID.randomUUID().toString().substring(0, 8);
            appointment.setAppointmentId(appointmentId);
            
            // Set status to "Scheduled" if not already set
            if (appointment.getStatus() == null || appointment.getStatus().isEmpty()) {
                appointment.setStatus("Scheduled");
            }
            
            // Create HTTP client
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String apiUrl = supabaseUrl + "/rest/v1/appointments";

            // Create HTTP POST request
            HttpPost httpPost = new HttpPost(apiUrl);
            httpPost.setHeader("apikey", supabaseKey);
            httpPost.setHeader("Authorization", "Bearer " + supabaseKey);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Prefer", "return=minimal");

            // Create request body with appointment details
            JSONObject requestBody = new JSONObject();
            requestBody.put("id", appointmentId);
            requestBody.put("patient_name", appointment.getPatientName());
            requestBody.put("patient_id", appointment.getPatientId());
            requestBody.put("age", appointment.getAge());
            requestBody.put("gender", appointment.getGender());
            requestBody.put("contact_number", appointment.getContactNumber());
            requestBody.put("email", appointment.getEmail());
            requestBody.put("date", appointment.getDate());
            requestBody.put("time", appointment.getTime());
            requestBody.put("doctor_name", appointment.getDoctorName());
            requestBody.put("department", appointment.getDepartment());
            requestBody.put("appointment_type", appointment.getAppointmentType());
            requestBody.put("symptoms", appointment.getSymptoms());
            requestBody.put("medical_history", appointment.getMedicalHistory());
            requestBody.put("preferred_mode", appointment.getPreferredMode());
            requestBody.put("consultation_room", appointment.getConsultationRoom());
            requestBody.put("payment_status", appointment.getPaymentStatus());
            requestBody.put("notes", appointment.getNotes());
            requestBody.put("status", appointment.getStatus());
            
            // Add user_id if available
            if (currentUser != null) {
                requestBody.put("user_id", currentUser.getUserId());
            }

            StringEntity entity = new StringEntity(requestBody.toString());
            httpPost.setEntity(entity);

            // Execute request
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            
            // Log the request and response
            System.out.println("API URL: " + apiUrl);
            System.out.println("Request Body: " + requestBody.toString());
            System.out.println("Response Status: " + statusCode);
            
            // Debug response
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("Response Body: " + responseBody);
            
            httpClient.close();

            // Check if request was successful
            return statusCode == 201 || statusCode == 200;
        } catch (Exception e) {
            System.err.println("Error inserting appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Get all appointments from Supabase
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        try {
            // Create HTTP client
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String apiUrl = supabaseUrl + "/rest/v1/appointments?select=*";

            // Create HTTP GET request
            HttpGet httpGet = new HttpGet(apiUrl);
            httpGet.setHeader("apikey", supabaseKey);
            httpGet.setHeader("Authorization", "Bearer " + supabaseKey);

            // Execute request
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity());
            httpClient.close();

            // Parse response
            JSONArray jsonArray = new JSONArray(responseBody);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject appointment = jsonArray.getJSONObject(i);
                
                String id = appointment.optString("id", "");
                String patientName = appointment.optString("patient_name", "");
                String patientId = appointment.optString("patient_id", "");
                int age = appointment.optInt("age", 0);
                String gender = appointment.optString("gender", "");
                String contactNumber = appointment.optString("contact_number", "");
                String email = appointment.optString("email", "");
                String date = appointment.optString("date", "");
                String time = appointment.optString("time", "");
                String doctorName = appointment.optString("doctor_name", "");
                String department = appointment.optString("department", "");
                String appointmentType = appointment.optString("appointment_type", "");
                String symptoms = appointment.optString("symptoms", "");
                String medicalHistory = appointment.optString("medical_history", "");
                String preferredMode = appointment.optString("preferred_mode", "");
                String consultationRoom = appointment.optString("consultation_room", "");
                String paymentStatus = appointment.optString("payment_status", "");
                String notes = appointment.optString("notes", "");
                String status = appointment.optString("status", "Scheduled");
                
                Appointment appt = new Appointment(
                    patientName, patientId, age, gender, contactNumber, email,
                    date, time, doctorName, department, appointmentType,
                    symptoms, medicalHistory, preferredMode, consultationRoom,
                    paymentStatus, notes
                );
                
                appt.setAppointmentId(id);
                appt.setStatus(status);
                appointments.add(appt);
            }
        } catch (Exception e) {
            System.err.println("Error getting appointments: " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }

    // Delete an appointment
    public boolean deleteAppointment(String patientName, String date) {
        try {
            // Create HTTP client
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String apiUrl = supabaseUrl + "/rest/v1/appointments?patient_name=eq." + 
                            patientName.replace(" ", "%20") + "&date=eq." + date;

            // Create HTTP DELETE request
            HttpDelete httpDelete = new HttpDelete(apiUrl);
            httpDelete.setHeader("apikey", supabaseKey);
            httpDelete.setHeader("Authorization", "Bearer " + supabaseKey);

            // Execute request
            CloseableHttpResponse response = httpClient.execute(httpDelete);
            int statusCode = response.getStatusLine().getStatusCode();
            httpClient.close();

            // Check if request was successful
            return statusCode == 204 || statusCode == 200;
        } catch (Exception e) {
            System.err.println("Error deleting appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Update an appointment status
    public boolean updateAppointmentStatus(Appointment appointment, String newStatus) {
        try {
            // Create HTTP client
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String apiUrl = supabaseUrl + "/rest/v1/appointments?id=eq." + appointment.getAppointmentId();

            // Create HTTP POST request (using PATCH method)
            HttpPost httpPost = new HttpPost(apiUrl);
            httpPost.setHeader("apikey", supabaseKey);
            httpPost.setHeader("Authorization", "Bearer " + supabaseKey);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Prefer", "return=minimal");
            httpPost.setHeader("X-HTTP-Method-Override", "PATCH"); // Emulate PATCH

            // Create request body with status update
            JSONObject requestBody = new JSONObject();
            requestBody.put("status", newStatus);
            StringEntity entity = new StringEntity(requestBody.toString());
            httpPost.setEntity(entity);

            // Execute request
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            httpClient.close();

            // Check if request was successful
            return statusCode == 204 || statusCode == 200;
        } catch (Exception e) {
            System.err.println("Error updating appointment status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Update an appointment with all fields
    public boolean updateAppointment(Appointment appointment) {
        try {
            // Create HTTP client
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String apiUrl = supabaseUrl + "/rest/v1/appointments?id=eq." + appointment.getAppointmentId();

            // Create HTTP POST request (using PATCH method)
            HttpPost httpPost = new HttpPost(apiUrl);
            httpPost.setHeader("apikey", supabaseKey);
            httpPost.setHeader("Authorization", "Bearer " + supabaseKey);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Prefer", "return=minimal");
            httpPost.setHeader("X-HTTP-Method-Override", "PATCH"); // Emulate PATCH

            // Create request body with all appointment details
            JSONObject requestBody = new JSONObject();
            requestBody.put("patient_name", appointment.getPatientName());
            requestBody.put("patient_id", appointment.getPatientId());
            requestBody.put("age", appointment.getAge());
            requestBody.put("gender", appointment.getGender());
            requestBody.put("contact_number", appointment.getContactNumber());
            requestBody.put("email", appointment.getEmail());
            requestBody.put("date", appointment.getDate());
            requestBody.put("time", appointment.getTime());
            requestBody.put("doctor_name", appointment.getDoctorName());
            requestBody.put("department", appointment.getDepartment());
            requestBody.put("appointment_type", appointment.getAppointmentType());
            requestBody.put("symptoms", appointment.getSymptoms());
            requestBody.put("medical_history", appointment.getMedicalHistory());
            requestBody.put("preferred_mode", appointment.getPreferredMode());
            requestBody.put("consultation_room", appointment.getConsultationRoom());
            requestBody.put("payment_status", appointment.getPaymentStatus());
            requestBody.put("notes", appointment.getNotes());
            requestBody.put("status", appointment.getStatus());
            
            StringEntity entity = new StringEntity(requestBody.toString());
            httpPost.setEntity(entity);

            // Execute request
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            httpClient.close();

            // Check if request was successful
            return statusCode == 204 || statusCode == 200;
        } catch (Exception e) {
            System.err.println("Error updating appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 