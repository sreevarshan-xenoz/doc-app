package DoctorAppointmentSystem;

import io.github.supabase.SupabaseClient;
import io.github.supabase.annotations.SupabaseHttp;
import io.github.supabase.gotrue.GoTrue;
import io.github.supabase.postgrest.PostgrestDefaultClient;
import io.github.supabase.postgrest.model.PostgrestResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseService {
    private static DatabaseService instance;
    private SupabaseClient supabaseClient;
    
    private final String SUPABASE_URL;
    private final String SUPABASE_API_KEY;
    private final String APPOINTMENTS_TABLE = "appointments";
    
    private DatabaseService(String supabaseUrl, String supabaseApiKey) {
        this.SUPABASE_URL = supabaseUrl;
        this.SUPABASE_API_KEY = supabaseApiKey;
        initialize();
    }
    
    public static synchronized DatabaseService getInstance(String supabaseUrl, String supabaseApiKey) {
        if (instance == null) {
            instance = new DatabaseService(supabaseUrl, supabaseApiKey);
        }
        return instance;
    }
    
    private void initialize() {
        try {
            supabaseClient = new SupabaseClient(SUPABASE_URL, SUPABASE_API_KEY);
        } catch (Exception e) {
            System.err.println("Error initializing Supabase client: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean insertAppointment(Appointment appointment) {
        try {
            Map<String, Object> appointmentData = new HashMap<>();
            appointmentData.put("patient_name", appointment.getPatientName());
            appointmentData.put("appointment_date", appointment.getDate());
            
            PostgrestDefaultClient postgrest = supabaseClient.getPostgrest();
            @SupabaseHttp
            PostgrestResponse<JSONObject> response = postgrest.from(APPOINTMENTS_TABLE)
                    .insert(new JSONObject(appointmentData).toString())
                    .executeAndGetSingle();
            
            return response.getData() != null;
        } catch (Exception e) {
            System.err.println("Error inserting appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        
        try {
            PostgrestDefaultClient postgrest = supabaseClient.getPostgrest();
            @SupabaseHttp
            PostgrestResponse<JSONArray> response = postgrest.from(APPOINTMENTS_TABLE)
                    .select("*")
                    .executeAndGetList();
            
            JSONArray jsonArray = response.getData();
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String patientName = obj.getString("patient_name");
                    String appointmentDate = obj.getString("appointment_date");
                    
                    Appointment appointment = new Appointment(patientName, appointmentDate);
                    appointments.add(appointment);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching appointments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return appointments;
    }
    
    public boolean deleteAppointment(String patientName, String date) {
        try {
            PostgrestDefaultClient postgrest = supabaseClient.getPostgrest();
            @SupabaseHttp
            PostgrestResponse<JSONArray> response = postgrest.from(APPOINTMENTS_TABLE)
                    .delete()
                    .eq("patient_name", patientName)
                    .eq("appointment_date", date)
                    .executeAndGetList();
            
            return response.getData() != null;
        } catch (Exception e) {
            System.err.println("Error deleting appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Method for user authentication with Supabase
    public boolean authenticateUser(String email, String password) {
        try {
            GoTrue auth = supabaseClient.getAuth();
            auth.signInWithEmail(email, password);
            return auth.getUser() != null;
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 