package DoctorAppointmentSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
    // Default values
    public static String SUPABASE_URL = "https://your-supabase-project-url.supabase.co";
    public static String SUPABASE_API_KEY = "your-supabase-api-key";
    
    // Default admin credentials (for demo purposes)
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin";
    
    // Current logged in user
    private static User currentUser;
    
    static {
        loadEnvVariables();
    }
    
    private static void loadEnvVariables() {
        try {
            // First check if .env file exists
            Path envPath = Paths.get(".env");
            System.out.println("Looking for .env file at: " + envPath.toAbsolutePath());
            
            if (Files.exists(envPath)) {
                Properties envProps = new Properties();
                try (FileInputStream input = new FileInputStream(".env")) {
                    envProps.load(input);
                    
                    // Get Supabase credentials from .env file
                    if (envProps.containsKey("SUPABASE_URL")) {
                        SUPABASE_URL = envProps.getProperty("SUPABASE_URL").trim();
                    }
                    
                    if (envProps.containsKey("SUPABASE_API_KEY")) {
                        SUPABASE_API_KEY = envProps.getProperty("SUPABASE_API_KEY").trim();
                    }
                    
                    System.out.println("Loaded Supabase credentials from .env file");
                    System.out.println("URL: " + SUPABASE_URL);
                    System.out.println("API key length: " + SUPABASE_API_KEY.length());
                }
            } else {
                // If .env file doesn't exist, try to load from environment variables
                String envUrl = System.getenv("SUPABASE_URL");
                String envKey = System.getenv("SUPABASE_API_KEY");
                
                if (envUrl != null && !envUrl.isEmpty()) {
                    SUPABASE_URL = envUrl.trim();
                }
                
                if (envKey != null && !envKey.isEmpty()) {
                    SUPABASE_API_KEY = envKey.trim();
                }
                
                System.out.println("Tried loading Supabase credentials from environment variables");
            }
            
            // Validate credentials
            if (SUPABASE_URL.equals("https://your-supabase-project-url.supabase.co") ||
                SUPABASE_API_KEY.equals("your-supabase-api-key")) {
                System.err.println("Warning: Using default Supabase credentials. Please update your .env file.");
            }
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error loading configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Methods to handle current user
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static boolean isPatient() {
        return currentUser != null && currentUser.isPatient();
    }
} 